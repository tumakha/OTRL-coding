import Exercise3.addOne
import Exercise4.g
import Exercise4.g2
import org.scalatest.Inside
import org.scalatest.flatspec._
import org.scalatest.matchers._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * @author Yuriy Tumakha
 */
class ExercisesSpec extends AnyFlatSpec with should.Matchers with Inside {

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

}
