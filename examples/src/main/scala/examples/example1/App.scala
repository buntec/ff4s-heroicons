package examples.example1

import cats.effect.Async
import cats.effect.implicits._
import cats.syntax.all._
import fs2.Stream
import org.scalajs.dom

import scala.concurrent.duration.*

import scalajs.js

case class State(
    darkMode: Boolean = false
)

sealed trait Action

object Action {

  case class ModifyState(f: State => State) extends Action

}

trait View { dsl: ff4s.Dsl[State, Action] =>

  import html._

  val view = div("hello")

}

class App[F[_]](implicit val F: Async[F])
    extends ff4s.App[F, State, Action]
    with View {

  override val store = for {

    store <- ff4s.Store[F, State, Action](State())(_ =>
      _ match {
        case Action.ModifyState(f) => state => f(state) -> none
      }
    )

    _ <- Stream
      .fixedDelay(100.millis)
      .zipWithIndex
      .map(_._2)
      .evalMap(i =>
        store.dispatch(Action.ModifyState(_.copy(progress = i.toInt % 100)))
      )
      .compile
      .drain
      .background

    _ <- store.state
      .map(_.darkMode)
      .discrete
      .changes
      .evalMap(darkMode =>
        if (darkMode) {
          F.delay(dom.document.body.className = "dark")
        } else {
          F.delay(dom.document.body.className = "")
        }
      )
      .compile
      .drain
      .background

  } yield store

}
