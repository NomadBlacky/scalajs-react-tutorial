package scalajs.react.tutorial

import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed abstract class Marker(val value: String) {
  def isEmpty: Boolean  = this == Marker.Empty
  def nonEmpty: Boolean = !isEmpty
  def getNext: Marker
}
object Marker {
  case object Empty extends Marker("") {
    override def getNext: Marker = throw new UnsupportedOperationException
  }
  case object X extends Marker("X") {
    override def getNext: Marker = O
  }
  case object O extends Marker("O") {
    override def getNext: Marker = X
  }

  def getNext(step: Int): Marker = if (step % 2 == 0) X else O
}

@react object Square {
  case class Props(marker: Marker, onClick: () => Unit)
  val component = FunctionalComponent[Props] { props =>
    button(className := "square", onClick := props.onClick)(props.marker.value)
  }
}

@react class Board extends StatelessComponent {
  case class Props(squares: Vector[Marker], onClick: Int => Unit)

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
  case class State(history: Vector[Vector[Marker]], stepNumber: Int, next: Marker)

  def initialState: State = State(Vector(Vector.fill(9)(Marker.Empty)), 0, Marker.X)

  private val css = AppCSS

  def handleClick(i: Int): Unit = {
    val history = state.history.slice(0, state.stepNumber + 1)
    val current = history.last
    if (Utils.calculateWinner(current).isDefined || current(i).nonEmpty) ()
    else
      setState(
        state.copy(
          history = history :+ current.updated(i, state.next),
          stepNumber = history.length,
          next = state.next.getNext
        )
      )
  }

  def jumpTo(step: Int): Unit = setState { s =>
    s.copy(
      stepNumber = step,
      next = Marker.getNext(step)
    )
  }

  def render(): ReactElement = {
    val current = state.history(state.stepNumber)
    val winner  = Utils.calculateWinner(current)
    val status = winner match {
      case Some(winner) => s"Winner: $winner"
      case None         => s"Next player: ${state.next.value}"
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
  def calculateWinner(squares: Vector[Marker]): Option[Marker] = {
    val lines = Seq(
      (0, 1, 2),
      (3, 4, 5),
      (6, 7, 8),
      (0, 3, 6),
      (1, 4, 7),
      (2, 5, 8),
      (0, 4, 8),
      (2, 4, 6)
    )
    val result = for {
      (a, b, c) <- lines
      (x, y, z) = (squares(a), squares(b), squares(c))
      if x.nonEmpty && x == y && y == z
    } yield x
    result.headOption
  }
}
