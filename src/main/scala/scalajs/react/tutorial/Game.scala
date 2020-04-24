package scalajs.react.tutorial

import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@react class Square extends Component {
  case class Props(value: Int)
  case class State(value: Option[String])

  def initialState: State = State(None)

  def render(): ReactElement = {
    button(
      className := "square",
      onClick := (_ => setState(State(Some("X"))))
    )(state.value)
  }
}

@react class Board extends StatelessComponent {
  type Props = Unit

  def renderSquare(i: Int): ReactElement = Square()

  def render(): ReactElement = {
    val status = "Next player: X"

    div()(
      div(className := "status")(status),
      div(className := "board-row")(
        renderSquare(0),
        renderSquare(1),
        renderSquare(2)
      ),
      div(className := "board-row")(
        renderSquare(3),
        renderSquare(4),
        renderSquare(5)
      ),
      div(className := "board-row")(
        renderSquare(6),
        renderSquare(7),
        renderSquare(8)
      )
    )
  }
}

@JSImport("resources/App.css", JSImport.Default)
@js.native
object AppCSS extends js.Object

@react class Game extends StatelessComponent {
  type Props = Unit

  private val css = AppCSS

  def render(): ReactElement = {
    div(className := "game")(
      div(className := "game-board")(
        Board()
      ),
      div(className := "game-info")(
        div(), // TODO
        div()  // TODO
      )
    )
  }
}
