package scalajs.react.tutorial

import org.scalajs.dom.document
import org.scalatest.funsuite.AnyFunSuite
import slinky.web.ReactDOM

class GameTest extends AnyFunSuite {
  test("Renders without crashing") {
    val div = document.createElement("div")
    ReactDOM.render(Game(), div)
    ReactDOM.unmountComponentAtNode(div)
  }
}
