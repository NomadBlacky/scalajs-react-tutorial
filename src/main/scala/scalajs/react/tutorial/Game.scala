package scalajs.react.tutorial

import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@react object Square {
  case class Props(value: Option[String], onClick: () => Unit)
  val component = FunctionalComponent[Props] { props =>
    button(className := "square", onClick := props.onClick)(props.value)
  }
}

@react class Board extends Component {
  type Props = Unit
  case class State(squares: Vector[Option[String]], xIsNext: Boolean)

  def initialState: State = State(Vector.fill(9)(None), xIsNext = true)

  def renderSquare(i: Int): ReactElement =
    Square(
      state.squares(i),
      () =>
        if (Utils.calculateWinner(state.squares).isDefined || state.squares(i).isDefined) ()
        else
          setState(
            state.copy(
              squares = state.squares.updated(i, if (state.xIsNext) Some("X") else Some("O")),
              xIsNext = !state.xIsNext
            )
          )
    )

  def render(): ReactElement = {
    val winner = Utils.calculateWinner(state.squares)
    val status = winner match {
      case Some(winner) => s"Winner: $winner"
      case None         => s"Next player: ${if (state.xIsNext) "X" else "O"}"
    }

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

object Utils {
  def calculateWinner(squares: Vector[Option[String]]): Option[String] = {
    Seq(
      (0, 1, 2),
      (3, 4, 5),
      (6, 7, 8),
      (0, 3, 6),
      (1, 4, 7),
      (2, 5, 8),
      (0, 4, 8),
      (2, 4, 6)
    ).collectFirst {
      case line if aligned(line, squares) => squares(line._1)
    }.flatten
  }

  private def aligned(line: (Int, Int, Int), squares: Vector[Option[String]]): Boolean = {
    val (a, b, c) = line
    val result = for {
      x <- squares(a)
      y <- squares(b)
      z <- squares(c)
      if x == y && y == z
    } yield x
    result.isDefined
  }
}
