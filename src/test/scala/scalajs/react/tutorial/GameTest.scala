package scalajs.react.tutorial

import slinky.web.ReactDOM
import org.scalajs.dom.document

import org.scalatest.FunSuite

class GameTest extends FunSuite {
  test("Renders without crashing") {
    val div = document.createElement("div")
    ReactDOM.render(Game(), div)
    ReactDOM.unmountComponentAtNode(div)
  }
}
