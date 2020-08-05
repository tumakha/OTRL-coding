import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

  def addOneRecur(digits: Seq[Int]): Seq[Int] = {
    def add1(reversed: Seq[Int]): Seq[Int] =
      reversed match {
        case Nil => Seq(1)
        case digit :: tail if digit < 9 => (digit + 1) :: tail
        case _ :: tail => 0 +: add1(tail)
      }

    if (digits.isEmpty) Nil else add1(digits.reverse).reverse
  }

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

  // Trait MyAlg uses higher-kinded type F[_] which allows to use in MyAlg implementation some generic type.
  // For example some Monads like Future[_] or IO[_] can be used as F[_]
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

  import Exercise5.MyAlg
  import cats._
  import cats.implicits._

  class MyProg[F[_] : Monad](myAlg: MyAlg[F]) {

    def checkThenAddIt(someInt: Int): F[Unit] = myAlg.doSomething(someInt) flatMap myAlg.insertItSomewhere

  }

}

/**
 * 7. How would you design a REST API.
 *
 * for an address book? What endpoints will it have (feel free to provide sample curl requests)?
 * How would you handle errors?
 */
object Exercise7 {

  /** Address Book REST API
   *
   * Contact JSON object example:
   * {
   *  "contactId": 1,
   *  "firstname": "Jack",
   *  "lastname": "London",
   *  "email": "jack@email.co.uk",
   *  "phone": "1234567",
   *  "addressLine1": "1 High Street",
   *  "city": "London",
   *  "country": "UK",
   *  "postCode": "WC123AB"
   * }
   *
   * GET /v1/contact?page={page}&size={size} - Get all contacts
   * curl -X GET "https://localhost/v1/contact?page=0&size=20" -H "accept: application/json"
   * returns
   * 200 list of contacts
   * 500 server error
   *
   * GET /v1/contact/{contactId} - Get contact
   * curl -X GET "https://localhost/v1/contact/12001" -H "accept: application/json"
   * returns
   * 200 contact json
   * 404 contact not found
   * 500 server error
   *
   * POST /v1/contact/findByExample - Search contact by properties specified in json object provided in request body
   * curl -X POST "https://localhost/v1/contact/findByExample" -d "{ \"firstname\":\"Jack\", \"city\":"London"}" -H "Content-Type: application/json"
   * returns
   * 200 list of contacts
   * 400 Bad Request
   * 500 server error
   *
   * POST /v1/contact - Create contact
   * curl -X POST "https://localhost/v1/contact" -d "{Contact JSON}" -H "Content-Type: application/json"
   * returns
   * 201 new contact json
   * 400 Bad Request
   * 500 server error
   *
   * PUT /v1/contact/{contactId} - Update all contact properties
   * curl -X PUT "https://localhost/v1/contact/12001" -d "{Contact JSON}" -H "Content-Type: application/json"
   * returns
   * 200 contact json
   * 400 Bad Request
   * 404 contact not found
   * 500 server error
   *
   * PATCH /v1/contact/{contactId} - Update only contact properties specified in request json body
   * curl -X PATCH "https://localhost/v1/contact/12001" -d "{ \"phone\":\"11223344\" }" -H "Content-Type: application/json"
   * returns
   * 200 contact json
   * 400 Bad Request
   * 404 contact not found
   * 500 server error
   *
   * DELETE /v1/contact/{contactId} - Delete contact
   * curl -X DELETE "https://localhost/v1/contact/12001" -H "accept: application/json"
   * returns
   * 200 {"status":"OK"}
   * 404 contact not found
   * 500 server error
   */

}
