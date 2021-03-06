package arrow.data

import arrow.Kind
import arrow.core.*
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.eithert.async.async
import arrow.effects.extensions.io.applicativeError.attempt
import arrow.effects.extensions.io.async.async
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.id.traverse.traverse
import arrow.core.extensions.option.functor.functor
import arrow.data.extensions.eithert.applicative.applicative
import arrow.data.extensions.eithert.semigroupK.semigroupK
import arrow.data.extensions.eithert.traverse.traverse
import arrow.test.UnitSpec
import arrow.test.laws.AsyncLaws
import arrow.test.laws.SemigroupKLaws
import arrow.test.laws.TraverseLaws
import arrow.typeclasses.Eq
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.forAll
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class EitherTTest : UnitSpec() {

  private fun IOEQ(): Eq<Kind<EitherTPartialOf<ForIO, Throwable>, Int>> = Eq { a, b ->
    a.value().attempt().unsafeRunSync() == b.value().attempt().unsafeRunSync()
  }

  private fun IOEitherEQ(): Eq<Kind<EitherTPartialOf<ForIO, Throwable>, Either<Throwable, Int>>> = Eq { a, b ->
    a.value().attempt().unsafeRunSync() == b.value().attempt().unsafeRunSync()
  }

  init {

      testLaws(
        AsyncLaws.laws(EitherT.async(IO.async()), IOEQ(), IOEitherEQ()),
        TraverseLaws.laws(EitherT.traverse<ForId, Int>(Id.traverse()), EitherT.applicative<ForId, Int>(Id.monad()), { EitherT(Id(Right(it))) }, Eq.any()),
        SemigroupKLaws.laws<EitherTPartialOf<ForId, Int>>(
          EitherT.semigroupK(Id.monad()),
          EitherT.applicative(Id.monad()),
          Eq.any())
      )

    "mapLeft should alter left instance only" {
      forAll { i: Int, j: Int ->
        val left: Either<Int, Int> = Left(i)
        val right: Either<Int, Int> = Right(j)
        EitherT(Option(left)).mapLeft(Option.functor()) { it + 1 } == EitherT(Option(Left(i + 1))) &&
          EitherT(Option(right)).mapLeft(Option.functor()) { it + 1 } == EitherT(Option(right)) &&
          EitherT(Option.empty<Either<Int, Int>>()).mapLeft(Option.functor()) { it + 1 } == EitherT(Option.empty<Either<Int, Int>>())
      }
    }

  }
}
