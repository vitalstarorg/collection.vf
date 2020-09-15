package org.vitalstar.collection

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/*
Print the tree in compact way.

The meaning of compact:
Taking the following tree as an example
     Z0
   /    \
  A1    A2
 / \   / \
B1 B2 B3 B4
     / \
    C1 C2
The tree from Z, A & B are well balanced, this is the most compact form.
If B2 is missing, A2 can be more closer to A1.  Similarly because B2 has
no right child, B3 can be closer to B2 and its left child C1 can use the
space for B2 right child.

The algorithm for the compact print (a-kind-of DFS):
Thinking of the left margin is the ground, the gravity works side way to
the left.  The tree can be considered as a stack of lists laying on top
of each other while maintaing a straightline.  The list has flexibility
of being pushed to the right in case of meeting some support.  We lay the
foundation layer by layer from the left.  For example, the tree above will
be stacked in the following order like the following (bottom first):

      B4
=========^^=
         C2
=========^^=
   A2 B3 C1
======^^====
      B2
======^^====
Z0 A1 B1
============ (the ground)

 */
object NodePrinter {
  def printTree(node: NodeLike): String = {
    (new NodePrinter).printTree(node)
  }
}

/*
  leftStepsFromParent & rightStepsFromParent defines the spacing of the children.
  Make sure the children has enough space to print.
 */
case class NodePrinter(leftStepsFromParent: Int = 2, rightStepsFromParent: Int = 1) {
  /*
    Keep track of node properties used in a map.
    level:      Level of the node starting from 0
    min_offset: It is the minimal offset of the node.  In case of meeting a support
                it will be increased.  This will never be reduced due to DFS
    parent:     The parent of the node. We don't assume the NodeLike has a pointer
                to its parent, so we temporarily keep track here.
    isLeft:     It is true if it is a left node, used in printTree()
   */
  case class NodeProperties(level:Int, var min_offset:Int, parent: NodeLike, isLeft: Boolean)

  val nodeprop: mutable.Map[NodeLike, NodeProperties] = mutable.Map[NodeLike, NodeProperties]()
      // Node properties using NodeLike as key instead of its name to allow nodes with duplicated names
  val rows: mutable.Map[Int, ListBuffer[NodeLike]] = mutable.Map[Int, ListBuffer[NodeLike]]()
      // rows of each tree level
  val visit: mutable.Stack[NodeLike] = mutable.Stack[NodeLike]()
      // nodes to be visited

  def rightStepsFromSlibing: Int = {
    leftStepsFromParent + rightStepsFromParent
  }

  // Finding a layer of nodes from the 'root'.
  def leftNodes(root:NodeLike): ListBuffer[NodeLike] = {
    @tailrec
    def leftNodes(node: NodeLike, layer: ListBuffer[NodeLike]): Unit = {
      if (node.isDefined) {
        layer.append(node)
        leftNodes(node.left, layer)
      }
    }
    val nlist = ListBuffer[NodeLike]()
    leftNodes(root, nlist)
    nlist
  }

  /*
   Moving a layer of nodes to the right with 'nspace'.  In order to maintain
   the straightness of the layer, the parents of 'node' will also need to move.
   The amount of move for each parents depends on their slibing node also to
   make sure we maintain the shape of the tree.
   */
  def moveRight(node: NodeLike, nspace: Int): Unit = {
    if (node != null && node.isDefined) {
      // Move this node first
      val nprop = nodeprop(node)
      nprop.min_offset = nprop.min_offset + nspace

      // Move its parents
      val parent = nprop.parent
      if (parent != null && parent.isDefined) {
        val pprop = nodeprop(parent)
        // Finding its children, one of it must be this node itself.
        val leftprop: NodeProperties = nodeprop.getOrElse(parent.left, null)
        val rightprop: NodeProperties = nodeprop.getOrElse(parent.right, null)
        if ((leftprop != null) && (rightprop != null)) {
          // It has both side, then the parent of this 'parent' will move half the step.
          val poffset = ((rightprop.min_offset - leftprop.min_offset)/2f + 0.5).floor.toInt
          if (leftprop.min_offset + poffset > pprop.min_offset) {
            val pspace = leftprop.min_offset + poffset - pprop.min_offset
            moveRight(parent, pspace)
          }
        } else {
          /*
            During the tree construction, the location of a child has not been defined, so
            we just move the same amount to right for this parent.  The process continue
            until it reaches the root.
           */
          moveRight(parent, nspace)
        }
      }
    }
  }

  /*
    Expand a layer of nodes from 'root' node for traversal. For any
    expanded nodes (children) will be pushed in the stack 'visit'.
    We always process the node on the top of visit to perform DFS.
   */
  def expand(root: NodeLike): Unit = {
    expand(root, 0, null)
    while(visit.nonEmpty){
      val top = visit.pop()
      val tprop = nodeprop(top)
      val node = top.right
      expand(node, tprop.level + 1, top)
    }
  }

  def expand(root: NodeLike,
             level: Int,
             parent: NodeLike,
             initialOffset: Int = 0): Unit = {
                // We can set different initialOffset for ease of debugging; otherwise,
                // offset adjustment will be negative most of the time.
    if (root != null && root.isDefined) {
      /* @TODO eliminate the leftNodes()
        since the final algorithm doesn't need to use this leftNodes.
        leftNodes can be absorbed into the main algorithm.
       */
      val layer = leftNodes(root)
      var lastParent = parent

      // Iterate through each nodes in layer
      layer.indices.foreach { i =>
        val n = layer(i)
        val lvl = level + i
        // update the row with each new element in layer
        val row: ListBuffer[NodeLike] = if (rows.contains(lvl)) {
                          rows(lvl)
                        } else {
                          val r = ListBuffer[NodeLike]()
                          rows.update(lvl, r)
                          r
                        }
        visit.push(n)
        if (lastParent == null) {
          nodeprop.update(n, NodeProperties(lvl, initialOffset, lastParent, isLeft = true))
        } else {
          val pProp = nodeprop(lastParent)
          val offsetFromParent =
                    if (i == 0) {
                      pProp.min_offset + rightStepsFromParent
                    } else {
                      pProp.min_offset - leftStepsFromParent
                    }
          val isLeft = if (i == 0) false else true
          var nProp: NodeProperties = null
          if (row.isEmpty) {
            nProp = NodeProperties(
                        pProp.level + 1,
                        offsetFromParent,
                        lastParent,
                        isLeft)
          } else {
            val slibing = row.last
            val pSlibing = nodeprop(slibing)
            val finalSpacing =
                    if (slibing.name.length + 1 > rightStepsFromSlibing)
                      slibing.name.length + 1
                    else
                      rightStepsFromSlibing
            val offsetFromSlibing = pSlibing.min_offset + finalSpacing
            if (offsetFromParent < offsetFromSlibing) {
              // Need to move parents to make space
              val nspace = if (lastParent == pSlibing.parent) {
                /*
                 if this node share the same parent as its slibing, then spacing for the
                 parent (lastParent) can be calculated as follow.
                 @TODO can be generalized using moveRight()
                 */
                ((offsetFromSlibing + pSlibing.min_offset)/2f + 0.5).floor.toInt - pProp.min_offset
              } else {
                /*
                 Since the other child of the parent has not defined yet, so the spacing
                 is the direct difference between its parent and slibing offsets.
                 */
                offsetFromSlibing - offsetFromParent
              }
              moveRight(lastParent, nspace)
              nProp = NodeProperties(
                pProp.level + 1,
                offsetFromSlibing,
                lastParent,
                isLeft)
            } else {
              nProp = NodeProperties(
                pProp.level + 1,
                offsetFromParent,
                lastParent,
                isLeft)
            }
          }
          nodeprop.update(n, nProp)
        }
        row.append(n)
        lastParent = n
      }
    }
  }

  /*
    Align the left margin to 'offset'
   */
  def alignLeftMargin(offset: Int = 0): Unit = {
    val lowestOffset = rows.values.foldLeft(Int.MaxValue){ (acc, e) =>
      val nprop = nodeprop(e.head)
      if (acc > nprop.min_offset) {
        nprop.min_offset
      } else {
        acc
      }
    }
    nodeprop.values.foreach(e => e.min_offset -= lowestOffset - offset )
  }

  // Print the edges
  def printEdges(sb: StringBuilder, row: ListBuffer[NodeLike]): Unit = {
    var currentOffset = 0
    val edgeLeft = "/"
    val edgeRight = "\\"
    row.foreach{ n =>
      val nprop = nodeprop(n)
      if (nprop.isLeft) {
        sb.append(" " * (nprop.min_offset - currentOffset + 1))
        sb.append(edgeLeft)
        currentOffset = nprop.min_offset + 2
      } else {
        sb.append(" " * (nprop.min_offset - currentOffset))
        sb.append(edgeRight)
        currentOffset = nprop.min_offset + 1
      }
    }
    sb.append("\n")
  }

  // Print the node names
  def printNode(sb: StringBuilder, row: ListBuffer[NodeLike]): Unit = {
    var currentOffset = 0
    row.foreach{ n =>
      val nprop = nodeprop(n)
      sb.append(" " * (nprop.min_offset - currentOffset))
      sb.append(n.name)
      currentOffset = nprop.min_offset + n.name.length
    }
    sb.append("\n")
  }

  // Print the tree
  def printTree(root: NodeLike): String = {
    expand(root)
    alignLeftMargin()
    val sb = new StringBuilder
    printNode(sb, rows(0))
    (1 until rows.size).foreach{ i =>
      val row = rows(i)
      printEdges(sb, row)
      printNode(sb, row)
    }
    sb.toString()
  }
}