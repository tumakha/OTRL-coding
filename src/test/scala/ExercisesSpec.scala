import java.util.concurrent.atomic.AtomicInteger

import Exercise3.addOne
import Exercise4.g
import Exercise4.g2
import Exercise5.MyAlg
import Exercise6.MyProg
import org.scalatest.Inside
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec._
import org.scalatest.matchers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Yuriy Tumakha
 */
// scalastyle:off magic.number
class ExercisesSpec extends AnyFlatSpec with should.Matchers with Inside with Eventually {

  val timeout: Duration = 10 seconds

  "Exercise1" should "run functions in correct order" in {
    println("No dependencies")
    val fut1 = Exercise1.parallel4
    Await.ready(fut1, timeout)

    println("Strict order")
    val fut2 = Exercise1.strictOrder
    Await.ready(fut2, timeout)

    println("f1 then (f2 or f3) then f4")
    val fut3 = Exercise1.f1_f2orf3_f4
    Await.ready(fut3, timeout)
  }

  "Exercise2" should "implement f3 by composing f1 and f2" in {
    Exercise2.f3(6, 9) shouldBe "15"
    Exercise2.f3(12, -12) shouldBe "0"
  }

  "Exercise3" should "increment by one" in {
    addOne(Nil) shouldBe Nil
    addOne(Seq(0)) shouldBe Seq(1)
    addOne(Seq(1)) shouldBe Seq(2)
    addOne(Seq(9)) shouldBe Seq(1, 0)
    addOne(Seq(1, 2, 3)) shouldBe Seq(1, 2, 4)
    addOne(Seq(9, 9, 9)) shouldBe Seq(1, 0, 0, 0)
    addOne(Seq(9, 9, 0)) shouldBe Seq(9, 9, 1)
    addOne(Seq(9, 9, 1)) shouldBe Seq(9, 9, 2)
    addOne(Seq(3, 9, 9)) shouldBe Seq(4, 0, 0)
    addOne(Seq(1, 0, 0, 9, 9)) shouldBe Seq(1, 0, 1, 0, 0)
  }

  "Exercise4" should "safely handle calling Future" in {
    g(12).unsafeRunSync() shouldBe Right(12)
    g("abc").unsafeRunSync() shouldBe Right("abc")

    val either = g("error").unsafeRunSync()
    inside(either) { case Left(e) =>
      e.getMessage shouldBe "Exception"
    }

    val fut1 = g2(123)
    Await.result(fut1, timeout) shouldBe Success(123)

    val fut2 = g2("error")
    Await.result(fut2, timeout).failed.get.getMessage shouldBe "Exception"
  }

  "Exercise5" should "abstract over higher-kinded type" in {
    val optionAlg = new MyAlg[Option] {
      override def insertItSomewhere(someInt: Int): Option[Unit] = None
      override def doSomething(someInt: Int): Option[Int] = Some(someInt)
    }

    optionAlg.doSomething(123) shouldBe Some(123)
  }

  "Exercise6" should "use higher-kinded type MyAlg from Exercise 5" in {
    import cats.implicits._

    val futureAlg = new MyAlg[Future] {
      val sum = new AtomicInteger

      override def insertItSomewhere(someInt: Int): Future[Unit] = Future {
        sum addAndGet someInt
        println(s"$someInt inserted")
      }

      override def doSomething(someInt: Int): Future[Int] = Future { someInt * someInt }
    }

    val myProg: MyProg[Future] = new MyProg(futureAlg)
    myProg.checkThenAddIt(9)
    myProg.checkThenAddIt(3)

    eventually { futureAlg.sum.intValue shouldBe 90 }
  }

}
