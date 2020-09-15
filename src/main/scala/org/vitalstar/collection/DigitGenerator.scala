package org.vitalstar.collection

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

// Define the common behavior for our digit generator
trait DigitGeneratorLike[A] extends Iterator[List[A]] {
  def init(): Unit  // initialization of the generator
  def increment(nDigit: Int): Unit  // logic how to increment to next sequence
  def hasNext: Boolean  // during increment(), it will determine whether it is the end.
  def next(): List[A] // since the main logic is in increment(), the next has been calculated in last call.
}

// Singleton to hold all common class methods
object DigitGenerator {
  def combination(n: Int, k: Int): Iterator[List[Int]] = {
    new DigitCombinator(n, k)
  }

  def combination[A](l: List[A], k: Int): Iterator[List[A]] = {
    val dg = DigitGenerator.combination(l.length, k)
    dg.map{ idx => idx.map{ i => l(i)} }
  }

  def permutation(n: Int, k: Int): Iterator[List[Int]] = {
    new DigitPermutator(n, k)
  }

  def permutation[A](l: List[A], k: Int): Iterator[List[A]] = {
    val dg = DigitGenerator.permutation(l.length, k)
    dg.map { idx => idx.map{ i => l(i)}}
  }
}

/*
 Common instance variables & methods defined in this abstract class.

 digits: keep track of what number being used in each digit position. Index 0 is used for the rightmost digit.
 max: the max of each digit it can go.

 Here we use combination n=5 and k=3 as an example, the first digit sequence will be [0,1,2].  Internally we stored it
 as [2, 1, 0] as we are using the first element as the rightmost digit.
 digits = { 0->2, 1->1, 2->0 }
 max = [4,3,2]

 Please refers to "DigitGenerator.combination Smoke Test" to see the details of the progression.
*/
abstract class DigitGenerator extends DigitGeneratorLike[Int] {
  val digits: mutable.Map[Int, Int] = mutable.Map() // index 0 used for the rightmost digit
  val max: ListBuffer[Int] = ListBuffer[Int]() // index 0 used for the rightmost digit
  var n: Int = 0
  var k: Int = 0
  var _hasNext = false  // let the derived class to decide whether n & k creates a valid sequence.

  def this(n: Int, k: Int) {
    this()
    this.n = n
    this.k = k
    if (n >= k && k > 0) init()
  }

  override def hasNext: Boolean = _hasNext

  override def next(): List[Int] = {
    val ret = (k - 1 to 0 by -1).toList.map{ i => digits(i) }
    increment(0)
    ret
  }

  // Used for debugging to illustrate the internal state of the generator
  override def toString: String = {
    (0 until k).toList.map{ i =>
      "[%d %d]".format(digits(i), max(i))
    }.mkString(" ")
  }
}

/*
Our first Digit Generator is generating combination for all digits sequence {0,1,2 ... n}.
https://en.wikipedia.org/wiki/Combination

For example, for n = 5 & k = 3 i.e. the list will be (0, 1, 2, 3, 4), and the first sequence of digits will be [0,1,2].
We increment the last digit for all available digits, the next two iterations will be [0,1,3], [0,1,4].  Now the 2nd
digit needs to increase to 2 for next iteration, until it exhausts all available numbers.  In this case, the max for
the 2nd digit will be 3 as it needs to leave space for the last digit.  Similarly the max for the 1st digit will be 2.

Whenever a digit advanced to next number, all rightmost digits will need to be reset. In the combination, the direct
rightmost digit will be plus 1 of the current digit as the combination of lower numbers should been appeared in previous
iteration. For example, for [0,1,?] where ? is 2 or 3 or 4.  After these iterations, all combination of sequence begin
with 0 and 1 have been exhausted; therefore, later digit will not need to consider 0 and 1 i.e. rightmost digits must be
bigger than the leftmost.

Please refers to "DigitGenerator.combination Smoke Test" to see the details of the progression.
 */
class DigitCombinator(n: Int, k: Int) extends DigitGenerator(n, k) {
  def init(): Unit = {
    (k - 1 to 0 by -1).foreach{ i =>
      digits(i) = k - 1 - i
      max.append(n - k + i)
      // each digit max at the point leaving space for rightmost digits e.g. in 5C3, so the max of the middle digit
      // is 3, not 4 as the max of last digit will be 4.  Similarly the max of the first digit will be 2. Pls refers
      // to the unit test.
    }
    _hasNext = true
  }

  def increment(nDigit: Int): Unit = {
    if (nDigit >= k) {
      _hasNext = false
    } else {
      val digit = digits(nDigit)
      val end = max(nDigit)
      if (digit + 1 > end) {
        increment(nDigit + 1) // increment leftmost digit
      } else {
        digits(nDigit) = digit + 1
        // all rightmost digits need to reset to the beginning
        (0 until nDigit).foreach{ i =>
          digits(nDigit - i - 1) = digit + i + 2
        }
      }
    }
  }
}

/*
This is Digit Generator for permutation of all digits sequence {0,1,2 ... n}.
https://en.wikipedia.org/wiki/Permutation

For example, for n = 4 & k = 3 i.e. the list will be (0, 1, 2, 3), and the first sequence of digits will be [0,1,2].
We increment the last digit for all available digits, the next iteration will be [0,1,3].  Since the last digit has
reached to its max, the 2nd digit needs to go up i.e. [0,2,?].  The the rightmost digits will need to reset to the
beginning. In this case, ? can't be 0 as it is used by the 1st digit. The next available number is 1, so the sequence
should be [0,2,1].  Similarly the nexts will be [0,2,3], [0,3,1], [0,3,2].  Since both 2nd and 3rd digit can't go up,
then the first digit go up by 1 to [1,?,?].  The rightmost digits will need to reset to the beginning.  In this case
the sequence will be [1,0,2].

Not like combination, in permutation leftmost and rightmost have no simple relationship.  So we introduce `pool` to keep
track all numbers have been used in the digits so that when we increment to next number, we avoid using the same number
twice.

Please refers to "DigitGenerator.permutation Smoke Test" to see the details of the progression.
 */
class DigitPermutator(n: Int, k: Int) extends DigitGenerator(n, k) {
  var pool: mutable.HashSet[Int] = _
  override def init(): Unit = {
    pool = mutable.HashSet()
    (k - 1 to 0 by -1).foreach{ i =>
      digits(i) = k - 1 - i // initial sequence of the iteration.
      pool.add(k - 1 - i) // we use pool to eliminate duplicate when we increase digits to next number.
      max.append(n - 1) // since any digit can take all possible numbers, the max for all digits is n - 1.
    }
    _hasNext = true
  }

  def removeNumberFromPool(nDigit: Int): Int = {
    val digit = digits(nDigit)
    pool.remove(digit)
    digit
  }

  def putNumberToPool(nDigit: Int, number: Int): Unit = {
    pool.add(number)
    digits(nDigit) = number
  }

  override def increment(nDigit: Int): Unit = {
    if (nDigit >= k) {
      _hasNext = false
    } else {
      val digit = removeNumberFromPool(nDigit) // free up this digit in case leftmost digits need it.
      var nextDigit = digit
      for(d <- digit + 1 to max(nDigit) if nextDigit == digit) if (!pool(d)) nextDigit = d // Finding next digit
      if (nextDigit == digit) { // found no fit, then the leftmost digit should go up.
        increment(nDigit + 1)
      } else {
        putNumberToPool(nDigit,nextDigit) // put `nextDigit` to the pool
        (0 until nDigit).foreach{ i => // All rightmost digits need to reset to the beginning.
          val leftDigit = nDigit - i - 1
          var nextLeft = -1
          for(d <- 0 to max(leftDigit) if nextLeft == -1) if (!pool(d)) nextLeft = d
          putNumberToPool(leftDigit,nextLeft)
        }
      }
    }
  }
}
