import cats.syntax.all._
import cats.effect._
import fs2.Stream
import fs2.io.file.Files
import fs2.io.file.Path

object Heroicons {

  case class Icon(
      variant: Icon.Variant,
      name: String,
      path: Path
  )

  object Icon {

    sealed trait Variant
    case object Outline extends Variant
    case object Solid extends Variant
    case object Mini extends Variant
    case object Micro extends Variant

  }

  def generate(
      svgRootPath: String,
      baseOutputDirectoryPath: String,
      baseOutputPackagePath: String
  ): IO[Unit] = {
    val outFile = Path(baseOutputDirectoryPath) / "Heroicons.scala"
    for {

      icons <- Files[IO]
        .walk(Path(svgRootPath))
        .map { path =>
          path.names.takeRight(3) match {
            case size :: cls :: fileName :: Nil =>
              val s = size.toString
              val c = cls.toString
              val name = fileName.toString.dropRight(4)
              (s, c) match {
                case ("24", "outline") => Icon(Icon.Outline, name, path).some
                case ("24", "solid")   => Icon(Icon.Solid, name, path).some
                case ("20", "solid")   => Icon(Icon.Mini, name, path).some
                case ("16", "solid")   => Icon(Icon.Micro, name, path).some
                case _                 => None
              }
            case _ => None
          }
        }
        .unNone
        .compile
        .toList

      _ <- (Stream(
        s"package $baseOutputPackagePath",
        "",
        "// code generated by `HeroiconsGenerator.scala`",
        "",
        "// format: off",
        "",
        "trait Heroicons[S, A] { self: ff4s.Dsl[S, A] => ",
        "",
        "  object heroicons {",
        ""
      ) ++
        Stream.emits(icons.groupBy(_.variant).toList).flatMap {
          case (variant, icons) =>
            Stream(
              s"    object ${variant.toString.toLowerCase} {",
              ""
            ) ++ Stream
              .emits(icons)
              .evalMap { icon =>
                Files[IO].readUtf8(icon.path).compile.foldMonoid.tupleLeft(icon)
              }
              .flatMap { case (icon, markup) =>
                Stream(
                  "",
                  s"""      lazy val `${icon.name}` = literal(\"\"\"${markup.trim}\"\"\")""",
                  ""
                )
              } ++ Stream("    }", "")
        } ++ Stream("", "  }", "", "}"))
        // .evalTap(line => IO.println(line))
        .through(Files[IO].writeUtf8Lines(outFile))
        .compile
        .drain

    } yield ()
  }

}
