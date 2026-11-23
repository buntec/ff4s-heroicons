package ff4s.heroicons

import cats.effect.IO
import cats.effect.Resource
import cats.effect.kernel.Outcome
import cats.syntax.all._
import munit.CatsEffectSuite
import org.scalajs.dom

import scala.concurrent.duration._
import scala.scalajs.js

class HeroiconsSuite extends CatsEffectSuite {

  case class State()
  type Action = Unit

  private object dsl
      extends ff4s.Dsl[State, Action]
      with Heroicons[State, Action]
  import dsl._
  import dsl.html._
  import dsl.heroicons._

  private val window = js.Dynamic.global.window
  private var installedFrameApi = false

  // jsdom only provides requestAnimationFrame in "visual" mode
  override def beforeAll(): Unit =
    if (!window.hasOwnProperty("requestAnimationFrame").asInstanceOf[Boolean]) {
      installedFrameApi = true
      window.requestAnimationFrame = { (cb: js.Function1[Double, Unit]) =>
        js.timers.setTimeout(0)(cb(0.0)): Unit
        0
      }: js.Function1[js.Function1[Double, Unit], Int]
      window.cancelAnimationFrame = { (_: Int) => () }: js.Function1[Int, Unit]
    }

  override def afterAll(): Unit =
    if (installedFrameApi) {
      js.special.delete(window, "requestAnimationFrame")
      js.special.delete(window, "cancelAnimationFrame")
    }

  private val store = ff4s.Store.pure[IO, State, Action](State())((_, s) => s)

  private var counter = 0

  /** Renders `view` into a fresh container, waits for the first render, and
    * runs `body` on the container.
    */
  private def withRender[A](view: V)(body: dom.Element => IO[A]): IO[A] =
    Resource
      .make(IO {
        counter += 1
        val container = dom.document.createElement("div")
        val root = dom.document.createElement("div")
        root.id = s"heroicons-root-$counter"
        container.appendChild(root)
        dom.document.body.appendChild(container)
        container
      })(c => IO(c.remove()))
      .flatMap { container =>
        implicit val s: Resource[IO, ff4s.Store[IO, State, Action]] = store
        view
          .renderReplace[IO](container.firstElementChild.id)
          .background
          .tupleLeft(container)
      }
      .use { case (container, outcome) =>
        val rendered = IO(container.firstElementChild.id.isEmpty)
        def awaitRender: IO[Unit] =
          rendered.ifM(IO.unit, IO.sleep(5.millis) *> awaitRender)
        val renderStopped = outcome.flatMap[A] {
          case Outcome.Errored(e) => IO.raiseError(e)
          case other              =>
            IO.raiseError(new AssertionError(s"rendering stopped: $other"))
        }
        IO.race(
          renderStopped,
          awaitRender.timeout(10.seconds) *> body(container)
        ).map(_.merge)
      }

  private def children(e: dom.Element): List[dom.Element] =
    (0 until e.children.length).map(e.children(_)).toList

  private case class Variant(
      name: String,
      icons: List[(String, V)],
      viewBox: String,
      outlined: Boolean
  )

  private val variants = List(
    Variant("outline", outline.all, "0 0 24 24", outlined = true),
    Variant("solid", solid.all, "0 0 24 24", outlined = false),
    Variant("mini", mini.all, "0 0 20 20", outlined = false),
    Variant("micro", micro.all, "0 0 16 16", outlined = false)
  )

  variants.foreach { variant =>
    test(s"${variant.name}: icon names are distinct and sorted") {
      val names = variant.icons.map(_._1)
      assert(names.nonEmpty)
      assertEquals(names.distinct, names)
      assertEquals(names.sorted, names)
    }

    test(s"${variant.name}: every icon renders as a single svg element") {
      withRender(div(variant.icons.map(_._2))) { container =>
        IO {
          val svgs = children(container.firstElementChild)
          assertEquals(svgs.length, variant.icons.length)
          variant.icons.map(_._1).zip(svgs).foreach { case (name, svg) =>
            val clue = s"${variant.name}.$name"
            assertEquals(svg.tagName, "svg", clue)
            assertEquals(svg.getAttribute("viewBox"), variant.viewBox, clue)
            assertEquals(svg.getAttribute("aria-hidden"), "true", clue)
            if (variant.outlined) {
              assertEquals(svg.getAttribute("fill"), "none", clue)
              assertEquals(svg.getAttribute("stroke"), "currentColor", clue)
            } else assertEquals(svg.getAttribute("fill"), "currentColor", clue)
            assert(
              svg.childElementCount > 0,
              clue
            ) // shapes, e.g. <path> or <rect>
          }
        }
      }
    }
  }

  test("withClass sets the class of the svg element") {
    val view = div(
      outline.`academic-cap`.withClass("w-6 h-6"),
      micro.`x-mark`.withClass("w-4 h-4")
    )
    withRender(view) { container =>
      IO {
        val svgs = children(container.firstElementChild)
        assertEquals(
          svgs.map(_.getAttribute("class")),
          List("w-6 h-6", "w-4 h-4")
        )
      }
    }
  }

  test("all pairs each name with the icon of that name") {
    val all = solid.all.toMap
    assert(all("paper-airplane") eq solid.`paper-airplane`)
    assert(all("x-mark") eq solid.`x-mark`)
  }

}
