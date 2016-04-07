// AUTHOR1: Martin Hahner - mhah@itu.dk
// AUTHOR2: Simon Buus Jense - sije@itu.dk
// Group number: 22

package fpinscala.laziness
import scala.language.higherKinds

import org.scalatest.FlatSpec
import org.scalatest.prop.Checkers
import org.scalacheck._
import org.scalacheck.Prop._
import Arbitrary.arbitrary

// If you comment out all the import lines below, then you test the Scala
// Standard Library implementation of Streams. Interestingly, the standard
// library streams are stricter than those from the book, so some laziness tests
// fail on them :)

import stream00._    // uncomment to test the book solution
// import stream01._ // uncomment to test the broken headOption implementation
// import stream02._ // uncomment to test another version that breaks headOption


class StreamSpecMhahSije extends FlatSpec with Checkers {

  import Stream._

  // a few unused examples how to generate random things
  val evenInteger = Arbitrary.arbitrary[Int] suchThat (_ % 2 == 0)

  implicit lazy val arbBool: Arbitrary[Boolean] = Arbitrary(Gen.oneOf(true, false))


  // An example generator of random finite non-empty streams
  def list2stream[A] (la :List[A]): Stream[A] = la.foldRight (empty[A]) (cons[A](_,_))

  // In ScalaTest we use the check method to switch to ScalaCheck's internal DSL
  def genNonEmptyStream[A] (implicit arbA :Arbitrary[A]) :Gen[Stream[A]] =
    for { la <- arbitrary[List[A]] suchThat (_.nonEmpty)}
    yield list2stream (la)

  // generator of a random Int in the same manner
  def genInt[Int]() =
    for {
      x <- Gen.choose(1, 100)
    } yield x


  ////////////////////////
  behavior of "headOption"
  ////////////////////////

  // a scenario test:

  it should "return None on an empty Stream (01)" in {
    assert(empty.headOption == None)
  }
  
  // a property test:
  
  it should "return the head of the stream packaged in Some (02)" in check {
    // the implict makes the generator available in the context
    implicit def arbIntStream = Arbitrary[Stream[Int]] (genNonEmptyStream[Int])
    ("singleton" |:
      Prop.forAll { (n :Int) => cons (n,empty).headOption == Some (n) } ) &&
    ("random" |:
      Prop.forAll { (s :Stream[Int]) => s.headOption != None } )

  }

  // a scenario test:

  it should "not force the tail of the stream (03)" in {
    
    val s = cons(0, cons(throw new RuntimeException("this has been forced"), empty))

    try { 

      s.headOption
      // just try to call headOption
      // if no Exception is thrown, everything is fine
     
     } catch {
     
      // if there is anything thrown, then the tail was forced
      case _ : Throwable => assert(false)
     
     }
  }


  //////////////////
  behavior of "take"
  //////////////////

  // a scenario test:

    it should "not force any heads nor any tails of the Stream it manipulates (04)" in {
    
    val s = cons(throw new RuntimeException("this has been forced"), 
            cons(throw new RuntimeException("this has been forced"), empty))

    try { 

      s.take(1)
      // just try to call take
      // if no Exception is thrown, everything is fine
     
     } catch {
     
      // if there is anything thrown, then something was forced
      case _ : Throwable => assert(false)
     
     }
  }

  // a scenario test:

  it should "not force (n+1)st head ever (05)" in {
    
    val s = cons(0,
            cons(1,  
            cons(throw new RuntimeException("this has been forced"), empty)))

    try { 

      s.take(2).toList
      // just try to call take and force all its elements
      // if no Exception is thrown, everything is fine
     
     } catch {
     
      // if there is anything thrown, then some element after the n'th was forced
      case _ : Throwable => assert(false)
     
     }
  }

  // a property test:

  it should "s.take(n).take(n) == s.take(n) for any Stream s and any n (06)" in check {

    implicit def arbIntStream = Arbitrary[Stream[Int]] (genNonEmptyStream[Int])

    ("idempotency" |: 
      Prop.forAll { (s :Stream[Int], n:Int) => s.take(n).take(n).toList == s.take(n).toList } )
  }


  //////////////////
  behavior of "drop"
  //////////////////

  // a property test:

  it should "s.drop(n).drop(m) == s.drop(n+m) for any n, m (07)" in check {

    ("additivity" |: Prop.forAll(genNonEmptyStream[Int], genInt[Int], genInt[Int]) {
      (s :Stream[Int], m:Int, n:Int) => (s.drop(n).drop(m).toList == s.drop(n+m).toList) } )
    
  }

  // a scenario test:

  it should "not force any of the dropped elements heads (08)" in {
    
    val s = cons(throw new RuntimeException("this has been forced"), 
            cons(throw new RuntimeException("this has been forced"), empty))

    try { 

      s.drop(1)
      // just try to call drop
      // if no Exception is thrown, everything is fine
     
     } catch {
     
      // if there is anything thrown, then an element was forced
      case _ : Throwable => assert(false)
     
     }
  }

  // a scenario test:

  it should "(08) should hold even if we force some stuff in the tail (09)" in {
    
    val s = cons(throw new RuntimeException("this has been forced"), 
            cons(1,
            cons(2,
            cons(3,
            cons(4,
            cons(5, empty))))))

    try { 

      s.drop(1).toList
      // just try to call drop and force the tail
      // if no Exception is thrown, everything is fine
     
     } catch {
     
      // if there is anything thrown, then an element was forced
      case _ : Throwable => assert(false)
     
     }
  }


  /////////////////
  behavior of "map"
  /////////////////

  // TODO


  ////////////////////
  behavior of "append"
  ////////////////////

  // TODO

}