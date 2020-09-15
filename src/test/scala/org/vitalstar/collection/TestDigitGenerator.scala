package org.vitalstar.collection

import org.junit.Assert.{assertEquals, assertFalse}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestDigitGenerator extends FunSuite with BeforeAndAfterAll {
  test("DigitGenerator.combination Smoke Test") {
    val dg = DigitGenerator.combination(5, 3)
    var l = dg.next()
    assertEquals(List(0, 1, 2), l) // The initial combination is started with a sequence starting from 0 i.e. 0,1,2
    l = dg.next()
    assertEquals(List(0, 1, 3), l) // The last digit will pick all available numbers 3 & 4.
    l = dg.next()
    assertEquals(List(0, 1, 4), l) // Now the last digit has picked up the last available number.
    l = dg.next()
    assertEquals(List(0, 2, 3), l) // The 2nd digit pick the next available number next to 1 is 2.
    l = dg.next()
    assertEquals(List(0, 2, 4), l) // The last digit pick the only available number 4,
    l = dg.next()
    assertEquals(List(0, 3, 4), l) // then 2nd digit need to pick the next available number 3.
    l = dg.next()
    assertEquals(List(1, 2, 3), l) // Both 2nd & last digit has no more number to pick, 1st digit picks next number 1
    // and starting a new sequence starting from 1 i.e. 1,2,3
    l = dg.next()
    assertEquals(List(1, 2, 4), l) // last digit picked the last available number 4,
    l = dg.next()
    assertEquals(List(1, 3, 4), l) // 2nd digit picked the last available number 3.
    l = dg.next()
    assertEquals(List(2, 3, 4), l) // After the first picked the last available number 2, it ends the whole iteration.
    assertFalse(dg.hasNext)
  }

  test("DigitGenerator.permutation Smoke Test") {
    var dg = DigitGenerator.permutation(4, 3)
    var l = dg.next()
    assertEquals(List(0, 1, 2), l) // The initial permutation is started with a sequence starting from 0 i.e. 0,1,2.
    l = dg.next()
    assertEquals(List(0, 1, 3), l) // The last digit picked the last available number.
    l = dg.next()
    assertEquals(List(0, 2, 1), l) // The 2nd digit advanced to next number 2 so leftmost digit restarts its sequence.
    l = dg.next()
    assertEquals(List(0, 2, 3), l) // The last digit picked should be 2 but used already, so it moves to 3.
    l = dg.next()
    assertEquals(List(0, 3, 1), l) // Similarly 2nd digit advanced to next number 3 so leftmost digit restarts.
    l = dg.next()
    assertEquals(List(0, 3, 2), l) // This is the end of the sequence when the first digit is 0.
    l = dg.next()
    assertEquals(List(1, 0, 2), l) // The whole sequence restarts by using 1 as the first digit.
    l = dg.next()
    assertEquals(List(1, 0, 3), l)
    l = dg.next()
    assertEquals(List(1, 2, 0), l)
    l = dg.next()
    assertEquals(List(1, 2, 3), l)
    l = dg.next()
    assertEquals(List(1, 3, 0), l)
    l = dg.next()
    assertEquals(List(1, 3, 2), l)
    l = dg.next()
    assertEquals(List(2, 0, 1), l)
    l = dg.next()
    assertEquals(List(2, 0, 3), l)
    l = dg.next()
    assertEquals(List(2, 1, 0), l)
    l = dg.next()
    assertEquals(List(2, 1, 3), l)
    l = dg.next()
    assertEquals(List(2, 3, 0), l)
    l = dg.next()
    assertEquals(List(2, 3, 1), l)
    l = dg.next()
    assertEquals(List(3, 0, 1), l)
    l = dg.next()
    assertEquals(List(3, 0, 2), l)
    l = dg.next()
    assertEquals(List(3, 1, 0), l)
    l = dg.next()
    assertEquals(List(3, 1, 2), l)
    l = dg.next()
    assertEquals(List(3, 2, 0), l)
    l = dg.next()
    assertEquals(List(3, 2, 1), l) // Until it reaches to the last permutation.
    assertFalse(dg.hasNext) // 4!/(4-3)! = 24

    dg = DigitGenerator.permutation(4, 2)
    assertEquals(12, dg.count(_ => true)) // 4!/(4-2)! = 12
  }

  test("Some applications of digit generator") {
    val chars = List("A", "B", "C")
    val dg = DigitGenerator.permutation(chars.length, 3)
    val seq = dg.map { idx =>
      idx.map { i => chars(i) }
    }

    var l = seq.next()
    assertEquals(List("A", "B", "C"), l)
    l = seq.next()
    assertEquals(List("A", "C", "B"), l)
    l = seq.next()
    assertEquals(List("B", "A", "C"), l)
    l = seq.next()
    assertEquals(List("B", "C", "A"), l)
    l = seq.next()
    assertEquals(List("C", "A", "B"), l)
    l = seq.next()
    assertEquals(List("C", "B", "A"), l)
    assertFalse(seq.hasNext)

    // This serves the same function.
    var itr = DigitGenerator.permutation(chars, 3)
    assertEquals(6, itr.count(_ => true))

    // Performs the same test as permutation
    itr = DigitGenerator.combination(chars,3)
    l = itr.next()
    assertEquals(List("A", "B", "C"), l)
    assertFalse(itr.hasNext)

    // Test out real combinations for a small iterations
    itr = DigitGenerator.combination(chars,2)
    l = itr.next()
    assertEquals(List("A", "B"), l)
    l = itr.next()
    assertEquals(List("A", "C"), l)
    l = itr.next()
    assertEquals(List("B", "C"), l)
    assertFalse(itr.hasNext)
  }

  test("Digit Generator boundaries test") {
    // n = k = 5 for combination
    var dg1 = DigitGenerator.combination(5,5)
    val l = dg1.next()
    assertEquals(List(0,1,2,3,4), l)
    assertFalse(dg1.hasNext)

    // k > n
    dg1 = DigitGenerator.combination(5,6)
    assertFalse(dg1.hasNext)

    // n = k = 5 for permutation
    dg1 = DigitGenerator.permutation(5,5)
    assertEquals(120, dg1.count(_ => true))

    // k > n
    dg1 = DigitGenerator.permutation(5,6)
    assertFalse(dg1.hasNext)
  }
}