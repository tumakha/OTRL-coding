import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 1. Given these functions.
 *
 * Write code to execute them when:
 * ​ there are no dependencies between the functions
 * ​ f4 depends on f3 which depends on f2 which depends on f1
 * ​ f4 depends on f3 and f2, and f3 and f2 both depend on f1
 */
object Exercise1 {

  def f1: Future[Unit] = Future { println("f1") }
  def f2: Future[Unit] = Future { println("f2") }
  def f3: Future[Unit] = Future { println("f3") }
  // def f3: Future[Unit] = Future { throw new RuntimeException("f3") }
  def f4: Future[Unit] = Future { println("f4") }

  def parallel4: Future[Seq[Unit]] = Future sequence Seq(f1, f2, f3, f4)

  def strictOrder: Future[Unit] = f1 flatMap (_ => f2) flatMap (_ => f3) flatMap (_ => f4)

  def f1_f2orf3_f4: Future[Unit] = f1 flatMap (_ => Future sequence Seq(f2, f3)) flatMap (_ => f4)

}

/**
 * 2. Given two functions f1 and f2, implement f3 by composing f1 and f2.
 */
object Exercise2 {

  val f1: (Int, Int) => Int = (a, b) => a + b
  val f2: Int => String = _.toString
  val f3: (Int, Int) => String = (f1.tupled andThen f2) (_, _)

}

/**
 * 3. Given a list Seq(1, 2, 3).
 *
 * which represents the number 123, write a function to increment it by one without converting types.
 * Your function should produce the expected result for the following test cases:
 * Nil => Nil
 * Seq(0) => Seq(1)
 * Seq(1, 2, 3) => Seq(1, 2, 4)
 * Seq(9, 9, 9) => Seq(1, 0, 0, 0)
 */
object Exercise3 {

  def addOne(digits: Seq[Int]): Seq[Int] =
    digits.foldRight((Seq[Int](), 1)) {
      case (digit, (seq, carry)) =>
        val previous = if (carry == 1) seq.drop(1) else seq
        val sum = digit + carry
        if (sum == 10) (1 +: 0 +: previous, 1)
        else (sum +: previous, 0)
    }._1

}

/**
 * 4. Given the following function:
 *
 * Write a function `g` that safely handles calling f. The return type of `g` should be such that
 * when f succeeds, g returns something very similar. Feel free to import an external library for the return type of g.
 */
object Exercise4 {

  import cats.effect.{ContextShift, IO}

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def f[A](a: A): Future[A] = Future {
    a match {
      case "error" => throw new RuntimeException("Exception")
      case any => any
    }
  }

  def g[A](a: A): IO[Either[Throwable, A]] = (IO fromFuture IO(f(a))).attempt

  def g2[A](a: A): Future[Try[A]] = f(a) transform (Try(_))

}

/**
 * 5. Explain what the following code means:
 *
 * Mention some advantages of the above code.
 */
object Exercise5 {

  trait MyAlg[F[_]] {
    def insertItSomewhere(someInt: Int): F[Unit]

    def doSomething(someInt: Int): F[Int]
  }

}

/**
 * 6. Given the trait in Q5,
 *
 * create a class `MyProg` abstract in type F that has MyAlg passed to it.
 * Implement the following method in the class:
 *
 * It should pass the result of `doSomething` to `insertItSomewhere`.
 * Feel free to add external imports.
 */
object Exercise6 {

  class MyProg {
    def checkThenAddIt(someInt: Int) = ???
  }

}

/**
 * 7. How would you design a REST API.
 *
 * for an address book? What endpoints will it have (feel free to provide sample curl requests)?
 * How would you handle errors?
 */
object Exercise7 {


}
