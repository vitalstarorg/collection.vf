# collection.vf
We create this Scala collection library with VF framework for educational purpose.
  
We assume minimal programming experiences.  As long as you know how to use a 
terminal, you should be able to learn something, enjoy!

# _Make the Code Talk_
We are developing this project using Test Driven Development (TDD), so all the development is driven by defining 
or prototyping using unit tests first.  

We also code in the way to _make it talks_ i.e. intuitively, that the design
idea in the code that talk back to you. The methods should guide your expression and thinking process on how to use the 
code.  Therefore, the _unit test also talks_ so that they explain the design and usage by itself. There is always a room
for improvement, at least this is our intention.  Please feel free to suggestion changes.

Since we focus more on the design, instead of leveraging unique programming language feature, so we limit our use of 
basic Scala object oriented and functional features; therefore the same design can be reimplemented in other languages e.g. Java,
Python or C++.

Please refer to all unit tests for latest development and technical details.

- [TestNode.scala](https://github.com/vitalstarorg/collection.vf/blob/master/src/test/scala/org/vf/collection/TestNode.scala)
shows how the `Node` should behave.
-  [TestParser](https://github.com/vitalstarorg/collection.vf/blob/master/src/test/scala/org/vf/collection/TestParser.scala) 
shows different parsing scenarios.

# Tryout
Try the following, you should see something like below.  
```sbtshell
$ git clone git@github.com:vitalstarorg/collection.vf.git
$ cd collection.vf
$ sbt test
.
.
.
[info] ScalaTest
[info] Run completed in 385 milliseconds.
[info] Total number of tests run: 5
[info] Suites: completed 2, aborted 0
[info] Tests: succeeded 5, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 5, Failed 0, Errors 0, Passed 5
[success] Total time: 15 s, completed Aug 26, 2020 1:51:10 PM
```
If you see `Failed 0`, you are ready to go.
```sbtshell
$ sbt console
```
Entering into _sbt console_, the console will load the Scala packages in this project.  It is very similar to Python or Ruby 
and other scripting languages. Since this console will instantly compile what you typed directly into JVM bytecode, so
it works almost the same as the code running in unit test environment. This is an helpful for debugging as it reduces the
editing, compilation, and test cycle, probably by 10x.

# org.vf.collection.Node
## Basic Features
```sbtshell
scala> import org.vf.collection.Node
import org.vf.collection.Node

scala> var root = Node.create("(4,(7,10,(2,,(6,2,))),(9,,6))")
root: org.vf.collection.NodeLike = Node(4)

scala> root.printTree
res0: String =
"    4
   / \
  7  9
 / \  \
10 2  6
    \
    6
   /
  2
"

scala> root.left
res1: org.vf.collection.NodeLike = Node(7)

scala> root.right
res2: org.vf.collection.NodeLike = Node(9)

scala> root.asString
res3: String = (4,(7,10,(2,,(6,2,))),(9,,6))
```
- Line #1: import the `Node` library.
- Line #2: create a tree from a description, and return the first `Node` of the tree i.e. `root`
- Line #3: print the tree structure in compact form to help visualize the structure
- Line #4-5: illustrate how to access the left and right children of `root`.  The left is `Node(7)` and the left is `Node(9)`.
- Line #6: serialize the tree in string representation. This representation can be used to reconstruct the tree.
## Node.create
### Create leaf node
This method is to parse a string that represent of a tree.  Let's show a few cases below.
```sbtshell
scala> root = Node.create("(4)")
root: org.vf.collection.NodeLike = Node(4)

scala> root.left
res4: org.vf.collection.NodeLike = Node()

scala> root.right
res5: org.vf.collection.NodeLike = Node()

scala> root.printTree
res6: String =
"4
"
```
Above create a single node, named with `4`.  This is a leaf node i.e. it has no children.  It is represented by `Node()` object, called `undefined` node.  Let's examine
a bit more of this object.
```sbtshell
scala> root.isDefined
res7: Boolean = true

scala> root.left.isDefined
res8: Boolean = false
``` 
Method `isDefined` or `notDefined` can be used to test whether it is `undefined`.
  
One usual practice is using `null`, but using an object `undefined` instead of `null` will help to write more elegant 
code as there is no need to write special handling logic in case of leaf node.

### Create `undefined` node
```sbtshell
scala> var node = Node.create("()")
node: org.vf.collection.NodeLike = Node()

scala> node.notDefined
res9: Boolean = true
```
`"()"` will create this `undefined` node.
```sbtshell
scala> node == root.left
res10: Boolean = true

scala> node.hashCode
res11: Int = 1450787378

scala> root.left.hashCode
res12: Int = 1450787378
```
All `undefined` node is the same object instance. In case of feature enhancement, this will help us to handle error
logic consistently.
### Create a small tree
```sbtshell
scala> node = Node.create("(A1,B1,B2)")
node: org.vf.collection.NodeLike = Node(A)

scala> node.printTree
res23: String =
"  A1
 / \
B1 B2
"
```
### Create a bigger tree
```sbtshell
scala> node = Node.create("(A1,B1,(B2, C1, C2))")
node: org.vf.collection.NodeLike = Node(A1)

scala> node.printTree
res24: String =
"  A1
 / \
B1 B2
  / \
 C1 C2
```
Besides left and right of a node can be names, but they can also be nodes.  This is the way we build build a bigger 
tree.  Please note that the `printTree` algorithm making
`B2` closer to `B1` because `B1` has no children, so `C1` can take up its space to make the tree more compact.
### Create a tree with unbalanced children
```sbtshell
scala> node = Node.create("(A1,(B1,C1,),)")
node: org.vf.collection.NodeLike = Node(A1)

scala> node.printTree
res25: String =
"    A1
   /
  B1
 /
C1
"
```
### Erroneous construction
```sbtshell
scala> node = Node.create("(A1(B1,C1,),)")
Error: node expects separator after 1st element, but Node(B1).
node: org.vf.collection.NodeLike = Node()

scala> node.isDefined
res26: Boolean = false
```
After we remove the first separator `,` from the construction, it errors out the problem.  In this case, `undefined` is returned.
