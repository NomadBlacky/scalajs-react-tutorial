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

@react class Board extends StatelessComponent {
  case class Props(squares: Vector[Option[String]], onClick: Int => Unit)

  def renderSquare(i: Int): ReactElement = Square(props.squares(i), () => props.onClick(i))

  def render(): ReactElement = {
    div()(
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

@react class Game extends Component {
  type Props = Unit
  case class State(history: Vector[Vector[Option[String]]], stepNumber: Int, xIsNext: Boolean)

  def initialState: State = State(Vector(Vector.fill(9)(None)), 0, true)

  private val css = AppCSS

  def handleClick(i: Int): Unit = {
    val history = state.history.slice(0, state.stepNumber + 1)
    val current = history.last
    if (Utils.calculateWinner(current).isDefined || current(i).isDefined) ()
    else
      setState(
        state.copy(
          history = history :+ current.updated(i, if (state.xIsNext) Some("X") else Some("O")),
          stepNumber = history.length,
          xIsNext = !state.xIsNext
        )
      )
  }

  def jumpTo(step: Int): Unit = setState { s =>
    s.copy(
      stepNumber = step,
      xIsNext = (step % 2) == 0
    )
  }

  def render(): ReactElement = {
    val current = state.history(state.stepNumber)
    val winner  = Utils.calculateWinner(current)
    val status = winner match {
      case Some(winner) => s"Winner: $winner"
      case None         => s"Next player: ${if (state.xIsNext) "X" else "O"}"
    }

    val moves = state.history.zipWithIndex.map {
      case (step, move) =>
        val desc = if (move == 0) "Go to game start" else s"Go to move #$move"
        li(key := move.toString)(
          button(onClick := (_ => jumpTo(move)))(desc)
        )
    }

    div(className := "game")(
      div(className := "game-board")(
        Board(squares = current, onClick = handleClick)
      ),
      div(className := "game-info")(
        div(status),
        div(moves)
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
