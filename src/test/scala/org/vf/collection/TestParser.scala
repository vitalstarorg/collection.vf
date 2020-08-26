package org.vf.collection

import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner

import org.vf.collection.NodeParser.readToken

@RunWith(classOf[JUnitRunner])
class TestParser extends FunSuite with BeforeAndAfterAll {
  test("Simple LL(1) parser test") {
    var token: Token = readToken("1234")
    assertEquals("1234", token.toString)
    token = readToken(",1234abcd")
    assertTrue(token.isSeparator)
    token = readToken(")123")
    assertTrue(token.isRParen)
    token = readToken("#1234abcd")
    assertTrue(token.isError)
    assertEquals("unrecognized character '#'", token.toString)

    token = readToken("abcABC")
    assertEquals("abcABC", token.toString)

    // preceeding space will be skipped, but will stop number parsing
    token = readToken("  abcd efgh")
    assertEquals("abcd", token.toString)

    // any non-alphabet will stop text parsing
    token = readToken("ABCD#1234")
    assertEquals("ABCD", token.toString)

    // Node(A,B,C)
    token = readToken("")
    assertTrue(token.isEOF)

    token = readToken("(A,B,C)")
    assertTrue(token.isNode)
    assertTrue(token.getNode.isDefined)
    var node = token.getNode
    var str = node.asString
    assertEquals("(A,B,C)", str)

    // Node(A)
    token = readToken("(A)")
    assertTrue(token.isNode)
    assertEquals("(A)", token.getNode.asString)

    // 2-level tree
    // Node(A,,C)
    token = readToken("(A,,C)")
    assertTrue(token.isNode)
    node = token.getNode
    str = node.asString
    assertEquals("(A,,C)", token.getNode.asString)

    // Node(A,,(C)) == Node(A,,C)
    token = readToken("(A,,(C))")
    assertEquals("(A,,C)", token.getNode.asString)

    // Node(A,B,)
    token = readToken("(A,B,)")
    assertEquals("(A,B,)", token.getNode.asString)

    // Node(A,(B),) == Node(A,B,)
    token = readToken("(A,(B),)")
    assertEquals("(A,B,)", token.getNode.asString)

    // Node(A,,) == Node(A)
    token = readToken("(A,,)")
    assertEquals("(A)", token.getNode.asString)

    // 3-level tree
    token = readToken("(A,(B,D,E),C)")
    assertEquals("(A,(B,D,E),C)", token.getNode.asString)
    token = readToken("(A,(B,D,E),(C,,F))")
    assertEquals("(A,(B,D,E),(C,,F))", token.getNode.asString)

    // all-level tree
    token = readToken("(A,(B,D,(E,,(G,H,))),(C,,F))")
    assertEquals("(A,(B,D,(E,,(G,H,))),(C,,F))", token.getNode.asString)

    token = readToken("( A)")
    assertEquals("(A)", token.getNode.asString)

    // all-level tree, put some extra space shouldn't affect the result
    token = readToken(" ( A , (  B , D ,( E, ,(G,H, ))),(C, ,F) )  ")
    assertEquals("(A,(B,D,(E,,(G,H,))),(C,,F))", token.getNode.asString)
  }

  test("Parsing undefined construction") {
    var token = readToken("()")
    var node = token.getNode
    var str = node.asString
    assertEquals("()", token.getNode.asString)

    token = readToken("(,,)")
    assertEquals("()", token.getNode.asString)
    token = readToken("(A,B,())")
    assertEquals("(A,B,)", token.getNode.asString)
    token = readToken("(A,(),C)")
    assertEquals("(A,,C)", token.getNode.asString)
    token = readToken("(A,(),())")
    assertEquals("(A)", token.getNode.asString)
    token = readToken("(A,(B,(),E),C)")
    assertEquals("(A,(B,,E),C)", token.getNode.asString)
  }

  test("Error construction") {
    var token: Token = readToken("#")
    assertEquals("unrecognized character '#'", token.toString)
    token = readToken("(")
    assertEquals("node expects 1st is a text, but EOF", token.toString)
    token = readToken("(*")
    assertEquals("node expects 1st is a text, but unrecognized character '*'", token.toString)
    token = readToken("(A1")
    assertEquals("node expects separator after 1st element, but EOF", token.toString)
    token = readToken("(A1*")
    assertEquals("node expects separator after 1st element, but unrecognized character '*'", token.toString)
    token = readToken("(A1,")
    assertEquals("node expects 2nd is a text or a node, but EOF", token.toString)
    token = readToken("(A1,B1")
    assertEquals("node expects separator after 2nd element, but EOF", token.toString)
    token = readToken("(A1,)")
    assertEquals("node expects 2nd is a text or a node, but )", token.toString)
    token = readToken("(A1,*")
    assertEquals("node expects 2nd is a text or a node, but unrecognized character '*'", token.toString)
    token = readToken("(A1,B1,")
    assertEquals("node expects 3rd is a text or a node, but EOF", token.toString)
    token = readToken("(A1,B1,*")
    assertEquals("node expects 3rd is a text or a node, but unrecognized character '*'", token.toString)
    token = readToken("(A1,,")
    assertEquals("node expects 3rd is a text or a node, but EOF", token.toString)
    token = readToken("(A1,B1,C1")
    assertEquals("node expects ')' after 3rd element, but EOF", token.toString)

    val str: String = null
    token = readToken(str)
    assertEquals("null string is found", token.toString)
  }
}