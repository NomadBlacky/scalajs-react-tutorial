package scalajs.react.tutorial

import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import typings.materialUiCore.{components, materialUiCoreStrings}

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

case class Location(index: Int) {
  require(0 <= index && index <= 8)

  def getX: Int = index % 3 + 1
  def getY: Int = index / 3 + 1
}

case class HistoryItem(squares: Vector[Marker], location: Option[Location]) {
  def description(stepNumber: Int): String = location match {
    case Some(loc) => s"Go to move #$stepNumber (${loc.getX}, ${loc.getY})"
    case None      => "Go to game start"
  }
}
object HistoryItem {
  def apply(squares: Vector[Marker], location: Location): HistoryItem = HistoryItem(squares, Some(location))
}

@react object Square {
  case class Props(marker: Marker, onClick: () => Unit, background: String)
  val component = FunctionalComponent[Props] { props =>
    button(
      className := "square",
      onClick := props.onClick,
      style := js.Dynamic.literal(background = props.background)
    )(props.marker.value)
  }
}

@react class Board extends StatelessComponent {
  case class Props(squares: Vector[Marker], onClick: Int => Unit, result: GameResult)

  def renderSquare(i: Int, highlight: Boolean): ReactElement = {
    val bg = if (highlight) "yellow" else "inherit"
    Square(props.squares(i), () => props.onClick(i), bg).withKey(i.toString)
  }

  def render(): ReactElement = {
    div()(
      (0 to 2).map { y =>
        div(className := "board-row", key := y.toString)(
          (0 to 2).map { x =>
            val index = y * 3 + x
            val highlight = props.result match {
              case GameResult.Finished(_, lines) => lines.contains(index)
              case _                             => false
            }
            this.renderSquare(index, highlight)
          }
        )
      }
    )
  }
}

sealed abstract class OrderingMode(val value: String, val reversed: Boolean) {
  def toggle: OrderingMode
}
object OrderingMode {
  case object Asc extends OrderingMode("↓", false) {
    override def toggle: OrderingMode = Desc
  }
  case object Desc extends OrderingMode("↑", true) {
    override def toggle: OrderingMode = Asc
  }
}

@react object Ordering {
  case class Props(onClick: () => Unit, mode: OrderingMode)
  val component = FunctionalComponent[Props] { props =>
    components.Button.variant(materialUiCoreStrings.outlined).onClick(_ => props.onClick())(props.mode.value)
  }
}

sealed abstract class GameResult(val isEnded: Boolean)
object GameResult {
  case object InGame                                   extends GameResult(false)
  case class Finished(winner: Marker, lines: Seq[Int]) extends GameResult(true)
  case object Draw                                     extends GameResult(true)
}

@JSImport("resources/App.css", JSImport.Default)
@js.native
object AppCSS extends js.Object

@react class Game extends Component {
  type Props = Unit
  case class State(
      history: Vector[HistoryItem],
      stepNumber: Int,
      next: Marker,
      orderingMode: OrderingMode
  )

  def initialState: State = State(
    history = Vector(HistoryItem(Vector.fill(9)(Marker.Empty), None)),
    stepNumber = 0,
    next = Marker.X,
    orderingMode = OrderingMode.Asc
  )

  private val css = AppCSS

  def handleClick(i: Int): Unit = {
    val history = state.history.slice(0, state.stepNumber + 1)
    val current = history.last
    if (calculateResult(current.squares).isEnded || current.squares(i).nonEmpty) ()
    else
      setState(
        state.copy(
          history = history :+ HistoryItem(current.squares.updated(i, state.next), Location(i)),
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

  def toggleOrdering(): Unit = setState(state.copy(orderingMode = state.orderingMode.toggle))

  def render(): ReactElement = {
    val current = state.history(state.stepNumber)
    val result  = calculateResult(current.squares)
    val status = result match {
      case GameResult.InGame              => s"Next player: ${state.next.value}"
      case GameResult.Finished(winner, _) => s"Winner: $winner"
      case GameResult.Draw                => "Draw"
    }
    val moves = state.history.zipWithIndex.map {
      case (historyItem, stepNumber) =>
        components.ListItem
          .button(true)
          .onClick(_ => jumpTo(stepNumber))
          .selected(state.stepNumber == stepNumber)
          .apply(historyItem.description(stepNumber))
    }

    div(
      h1("tic-tac-toe"),
      div(className := "game")(
        div(className := "game-board")(
          Board(squares = current.squares, onClick = handleClick, result)
        ),
        div(className := "game-info")(
          div(status),
          div()(
            Ordering(onClick = toggleOrdering, mode = state.orderingMode)
          ),
          components.List(if (state.orderingMode.reversed) moves.reverse else moves)
        )
      )
    )
  }

  def calculateResult(squares: Vector[Marker]): GameResult = {
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
    } yield (x, List(a, b, c))
    result match {
      case list @ (mk, _) :: _             => GameResult.Finished(mk, list.flatMap(_._2))
      case _ if squares.forall(_.nonEmpty) => GameResult.Draw
      case _                               => GameResult.InGame
    }
  }
}
