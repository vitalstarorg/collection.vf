package org.vitalstar.collection

import scala.collection.mutable
import scala.util.matching.Regex

/*
Simple LL(1) parser for parsing Node construction string
*/
trait Token {
  def tname: String
  def isNode: Boolean = false
  def getNode: Node = Node()
  def isNumber: Boolean = false
  def isSeparator: Boolean = false
  def isRParen: Boolean = false
  def isText: Boolean = false
  def getText: String = ""
  def isError: Boolean = false
  def isEOF: Boolean = false
}

object NodeParser {
  val sep_t: SeparatorT = SeparatorT()
  val rparen_t: RParenT = RParenT()
  val rNumber: Regex = "[0-9]".r
  val rText: Regex = "[a-zA-Z0-9]".r

  case class TextT(sText: String = "") extends Token {
    def tname = "Text"
    override def isText: Boolean = true
    override def getText: String = sText
    override def toString: String = { String.format("%s", sText) }
  }

  case class NodeT(node: Node = Node()) extends Token {
    def tname = "Node"
    override def isNode: Boolean = true
    override def getNode: Node = node
    override def toString: String = {
      if (node.isDefined) {
        String.format("%s", node.toString())
      } else {
        "<null>"
      }
    }
  }

  case class SeparatorT() extends Token {
    def tname = "Separator"
    override def isSeparator: Boolean = true
    override def toString: String = ","
  }

  case class RParenT() extends Token {
    def tname = "RParen"
    override def isRParen: Boolean = true
    override def toString: String = ")"
  }

  case class ErrorT(msg: String = "") extends Token {
    def tname = "Error"
    override def isError: Boolean = true
    override def toString: String = { String.format("%s", msg) }
  }

  case class EOF() extends Token {
    def tname = "EOF"
    override def isEOF: Boolean = true
    override def toString: String = "EOF"
  }

  def readToken(code: String): Token = {
    val parser = new NodeParser(code)
    if (parser.token == null || !parser.isError) parser.readToken()
    parser.token
  }

}

class NodeParser {
  import NodeParser._
  val st: mutable.Stack[Char] = mutable.Stack[Char]()
  var token: Token = _

  def this(code: String) = {
    this()
    if (code == null) {
      token = ErrorT("null string is found")
    } else {
      code.reverse.foreach(e => st.push(e))
    }
  }

  override def toString: String = if (token == null) "" else token.toString
  def isNode: Boolean = token.isNode
  def isNumber: Boolean = token.isNumber
  def isSeparator: Boolean = token.isSeparator
  def isRParen: Boolean = token.isRParen
  def isText: Boolean = token.isText
  def isError: Boolean = token.isError
  def isEOF: Boolean = token.isEOF
  def getNode: Node = token.getNode
  def getText: String = token.getText

  def readToken(): Unit = {
    token = if (st.isEmpty) {
              EOF()
            } else {
              while (st.top == ' ') st.pop()  // skip space
              val c = st.top
              c match {
                case '(' => st.pop(); readNode(); token
                case ')' => st.pop(); rparen_t
                case ',' => st.pop(); sep_t
                case rText() => readText(); token
                case _ => st.pop(); ErrorT(String.format("unrecognized character '%s'", c.toString))
              }
            }
  }

  // Read text according to "[a-zA-Z0-9]".r
  def readText(): Unit = {
    val text = new StringBuilder
    while (st.top == ' ') st.pop()  // skip preceeding space
    while(st.nonEmpty &&
      rText.pattern.matcher(st.top.toString).matches()) {
      text.append(st.pop())
    }
    token = TextT(text.toString())
  }

  // Read the name part of a node
  def readName(): String = {
    readToken()
    val n1 = token
    if (isRParen) {
      token = NodeT(Node()) // () -> undefined
    } else {
      if (!isText) {
        token = ErrorT(String.format("node expects 1st is a text, but %s", n1.toString))
      } else {
        readToken()
        val s1 = token
        if (isRParen || !isSeparator) {
          if (!isRParen) { // "(n1?
            token = ErrorT(String.format("node expects separator after 1st element, but %s", s1.toString))
          } else {
            val node = new Node(n1.toString)
            token = NodeT(node)
          }
        }
      }
    }
    n1.toString
  }

  // Read the left child of a node
  def readLeft(): Node = {
    var left: Node = Node()
    readToken()
    val n2 = token
    if (isSeparator) st.push(',') // push back if ',' i.e. left is undefined
    if (!n2.isText && !n2.isNode && !n2.isSeparator) {
      token = ErrorT(String.format("node expects 2nd is a text or a node, but %s", n2.toString))
    } else { // "(n1,n2" or "(n1,(...)" or "(n1,,"
      readToken()
      val s2 = token
      if (!s2.isSeparator) {
        token = ErrorT(String.format("node expects separator after 2nd element, but %s", s2.toString))
      } else { // "(n1,n2," or "(n1,(...)," or "(n1,,"
        if (!n2.isSeparator) { // "(n1,n2" or "(n1,(...)"
          left = if (!n2.isNode) { // "(n1,(...),"
            new Node(n2.getText)
          } else { // "(n1,n2,"
            n2.getNode
          }
        }
      }
    }
    left
  }

  // Read the right child of a node
  def readRight(): Node = {
    var right: Node = Node()
    readToken()
    val n3 = token
    if (n3.isRParen) st.push(')') // push back if ')' i.e. right is undefined
    if (!n3.isText && !n3.isNode && !n3.isRParen) {
      token = ErrorT(String.format("node expects 3rd is a text or a node, but %s", n3.toString))
    } else { // "(n1,n2,n3" or "(n1,(...),(...)" or "(n1,(...),)" or "(n1,,)"
      readToken()
      val s3 = token
      if (!s3.isRParen) {
        token = ErrorT(String.format("node expects ')' after 3rd element, but %s", s3.toString))
      } else {
        if (!n3.isRParen) {
          right = if (!n3.isNode) {
            new Node(n3.getText)
          } else {
            n3.getNode
          }
        }
      }
    }
    right
  }

  // Read a node of following possibilities:
  // "(n1,n2,n3)" or "(n1,,)" or "(n1,(...),(...))" or "(n1,(...),)" or "(n1,,(...))" etc
  def readNode(): Unit = {
    var ret: Token = null
    var left: Node = Node()
    var right: Node = Node()
    val name: String = readName()
    if (token.isNode || token.isError) {
      ret = token
    } else {
      left = readLeft()
      if (token.isError) {
        ret = token
      } else {
        right = readRight()
        if (token.isError) {
          ret = token
        }
      }
    }
    if (ret == null) {
      val node = new Node(name, left, right)
      token = NodeT(node)
    } else {
      token = ret
    }
  }
}