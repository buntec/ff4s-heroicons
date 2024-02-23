package examples.example1

import cats.effect.Concurrent
import cats.syntax.all._

case class State()

sealed trait Action

object Action {

  case object Noop extends Action

}

trait View extends ff4s.heroicons.Heroicons[State, Action] {
  dsl: ff4s.Dsl[State, Action] =>

  import html._
  import heroicons._

  val rowClass = "flex flex-row items-center gap-2"

  val view = div(
    cls := "flex flex-col items-center gap-2",
    span(cls := "text-2xl", "Heroicons"),
    span("outline"),
    div(
      cls := rowClass,
      List(
        outline.`academic-cap`,
        outline.`paper-airplane`,
        outline.`pencil-square`,
        outline.`chevron-right`,
        outline.`puzzle-piece`
      ).map(_.withClass("w-6 h-6"))
    ),
    span("solid"),
    div(
      cls := rowClass,
      List(
        solid.`academic-cap`,
        solid.`paper-airplane`,
        solid.`pencil-square`,
        solid.`chevron-right`,
        solid.`puzzle-piece`
      ).map(_.withClass("w-6 h-6"))
    ),
    span("mini"),
    div(
      cls := rowClass,
      List(
        mini.`academic-cap`,
        mini.`paper-airplane`,
        mini.`pencil-square`,
        mini.`chevron-right`,
        mini.`puzzle-piece`
      ).map(_.withClass("w-5 h-5"))
    ),
    span("micro"),
    div(
      cls := rowClass,
      List(
        micro.`academic-cap`,
        micro.`paper-airplane`,
        micro.`pencil-square`,
        micro.`chevron-right`,
        micro.`puzzle-piece`
      ).map(_.withClass("w-4 h-4"))
    )
  )

}

class App[F[_]: Concurrent] extends ff4s.App[F, State, Action] with View {

  override val store = ff4s.Store[F, State, Action](State())(_ =>
    _ match {
      case Action.Noop => state => state -> none
    }
  )

}
