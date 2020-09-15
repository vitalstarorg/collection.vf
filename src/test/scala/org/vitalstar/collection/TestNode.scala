package org.vitalstar.collection

import org.junit.runner.RunWith
import org.junit.Assert._
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestNode extends FunSuite with BeforeAndAfterAll {
  test("Smoke test") {
    // Default construction
    val n1 = Node("1")
    val n2 = Node("2")
    val n3 = Node("3", n1, n2)
    assertEquals("Node(1)", n1.toString)
    assertEquals("Node(2)", n2.toString)
    assertEquals("Node(3)", n3.toString)

    // Undefined
    val ndef = Node()
    assertTrue(ndef.notDefined)
    assertTrue(n1.isDefined)

    // Parsing
    var node = Node.create("()")
    assertTrue(node.notDefined)
    node = Node.create(null)
    assertTrue(node.notDefined)

    // Identity
    assertTrue(n1.equals(n1))
    assertTrue(n1 == n1)
    assertFalse(n1.equals(n2))
    assertFalse(n1.equals(n3))
    assertTrue(ndef.equals(ndef))
  }

  test("Test printTree") {
    var node = Node.create("(00,(A1,(B1,C1,C2),(B2,C3,C4)),(A2,(B3,C5,C6),(B4,C7,C8)))")
    assertEquals("(00,(A1,(B1,C1,C2),(B2,C3,C4)),(A2,(B3,C5,C6),(B4,C7,C8)))",node.asString)

    // Test: printing a totally balance tree
    var ptree1 =
      """           00
        |      /          \
        |     A1          A2
        |   /    \      /    \
        |  B1    B2    B3    B4
        | / \   / \   / \   / \
        |C1 C2 C3 C4 C5 C6 C7 C8
        |""".stripMargin
    var ptree = node.printTree
    assertEquals(ptree1, ptree)

    // Test: C1 will take space under B2
    ptree1 =
      """     Z0
        |   /    \
        |  A1    A2
        | / \   / \
        |B1 B2 B3 B4
        |     / \
        |    C1 C2
        |""".stripMargin
    node = Node.create("(Z0,(A1,B1,B2),(A2,(B3,C1,C2),B4))")
    ptree = node.printTree
    assertEquals(ptree1, ptree)

    // Test the deeper branching than the first branch
    ptree1 =
      """     00
        |    /  \
        |   A1  A2
        |     /    \
        |    B1    B2
        |   / \   / \
        |  C1 C2 C3 C4
        | / \   /
        |D1 D2 D3
        |     / \
        |    E1 E2
        |""".stripMargin
    node = Node.create("(00,A1,(A2,(B1,(C1,D1,D2),C2),(B2,(C3,(D3,E1,E2),),C4)))")
    ptree = node.printTree
    assertEquals(ptree1, ptree)

    // Test printer spacing adjustment leftSteps & rightSteps to be 3
    ptree1 =
      """         Z0
        |    /          \
        |   A1          A2
        | /    \      /    \
        |B1    B2    B3    B4
        |          /    \
        |         C1    C2
        |""".stripMargin
    node = Node.create("(Z0,(A1,B1,B2),(A2,(B3,C1,C2),B4))")
    var printer = new NodePrinter(3,3)
    ptree = printer.printTree(node)
    assertEquals(ptree1, ptree)

    // Test tree adjustment for longer names
    ptree1 =
      """        Z0
        |    /       \
        |   A1       A2
        | /   \     / \
        |B111 B222 B3 B4
        |         / \
        |        C1 C2
        |""".stripMargin
    node = Node.create("(Z0,(A1,B111,B222),(A2,(B3,C1,C2),B4))")
    ptree = node.printTree
    assertEquals(ptree1, ptree)

    // Test the effect of longer words B111, B222, C111 pushing the tree
    // The A1 and B3 have bigger gaps.
    ptree1 =
      """        Z0
        |    /        \
        |   A1        A2
        | /   \      / \
        |B111 B222  B3 B4
        |         /   \
        |        C111 C2
        |""".stripMargin
    node = Node.create("(Z0,(A1,B111,B222),(A2,(B3,C111,C2),B4))")
    ptree = node.printTree
    assertEquals(ptree1, ptree)

    // Test node with duplicated name
    ptree1 =
      """    4
        |   / \
        |  7  9
        | / \  \
        |10 2  6
        |    \
        |    6
        |   /
        |  2
        |""".stripMargin
    node = Node.create("(4,(7,10,(2,,(6,2,))),(9,,6))")
    ptree = node.printTree
    assertEquals(ptree1, node.printTree)
  }
}