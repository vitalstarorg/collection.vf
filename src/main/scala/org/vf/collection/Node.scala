package org.vf.collection

// Define the behavior for different kind of Node implementation
trait NodeLike {
  def name: String
  def left: NodeLike
  def right: NodeLike
  def asString: String
    // Deserialize the tree as string that can be used to reconstruct the tree
  def printTree: String
    // pretty print the tree
  def isDefined: Boolean = true
  def notDefined: Boolean = !isDefined
}

/*
All class-wise methods should be defined in object.  Since there might be different
Node implementation, but object Node will provide general Node methods for all
implementation based on trait NodeLike.  As long as a new Node implementation inherits
this behavior, all object Node methods will work.
*/
object Node {
  // Defined an anonymous singletone to simplify return type checking.
  val undefined: Node = new Node("<UND>", null, null) {
    override def isDefined: Boolean = false
    override def asString(withParens: Boolean): String = { if (withParens) "()" else "" }
    override def toString: String = "Node()"
  }
  def apply(): Node = undefined
  def apply(aName: String, left: Node = undefined, right: Node = undefined): Node
        = new Node(aName, left, right)
  def create(nodeString: String): NodeLike = {
    val token = NodeParser.readToken(nodeString)
    if (token.isError) {
      System.err.println(String.format("Error: %s.", token.toString))
    }
    token.getNode
  }
}

/*
This class provides a simple implementation for NodeLike
*/
class Node(val name: String, val left: Node = Node.undefined, val right: Node = Node.undefined)
  extends NodeLike {

  override def printTree: String = NodePrinter.printTree(this)
  override def toString: String = String.format("Node(%s)", name)
  def asString: String = asString(true)
  def asString(withParens: Boolean): String = {
    if (left.notDefined) {
      if (right.notDefined) {
        if (withParens) {
          String.format("(%s)", name)
        } else {
          String.format("%s", name)
          // used when left or right is a leaf Node.
        }
      } else {
        String.format("(%s,,%s)", name, right.asString(false))
      }
    } else {
      if (right.notDefined) {
        String.format("(%s,%s,)", name, left.asString(false))
      } else {
        String.format("(%s,%s,%s)", name, left.asString(false), right.asString(false))
      }
    }
  }
}

