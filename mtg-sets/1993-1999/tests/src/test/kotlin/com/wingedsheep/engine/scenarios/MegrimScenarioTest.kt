package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.sth.cards.Megrim
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Megrim — {2}{B} Enchantment
 *
 * Whenever an opponent discards a card, this enchantment deals 2 damage to that player.
 */
class MegrimScenarioTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(Megrim)
        return driver
    }

    // Resolve the stack, answering any SelectCardsDecision (the discard choice) as it comes up.
    fun drainStack(driver: GameTestDriver, discardCard: EntityId) {
        var guard = 0
        while (guard++ < 30) {
            when (val decision = driver.pendingDecision) {
                is SelectCardsDecision ->
                    driver.submitCardSelection(decision.playerId, listOf(discardCard))
                null -> {
                    if (driver.getTopOfStack() == null) return
                    driver.bothPass()
                }
                else -> driver.bothPass()
            }
        }
    }

    test("opponent discarding a card deals 2 damage to them") {
        val driver = createDriver()
        // Careful Study is sorcery-speed, so its caster must be the active player in their own
        // main phase with priority — start the opponent (seat 2) as the active player rather
        // than passing priority to them mid-turn, which sorcery-speed casting doesn't allow.
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20, startingPlayer = 1)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val opponent = driver.activePlayer!!
        val me = driver.getOpponent(opponent)

        driver.putPermanentOnBattlefield(me, "Megrim")

        val lifeBefore = driver.getLifeTotal(opponent)

        // Opponent casts Careful Study (draw a card, then discard a card) — they discard their
        // own card, which is what "an opponent discards a card" means from Megrim's perspective.
        val discardCandidate = driver.putCardInHand(opponent, "Lightning Bolt")
        val carefulStudy = driver.putCardInHand(opponent, "Careful Study")
        driver.giveMana(opponent, Color.BLACK, 1)

        driver.submit(
            CastSpell(
                playerId = opponent,
                cardId = carefulStudy,
                paymentStrategy = PaymentStrategy.FromPool
            )
        ).isSuccess shouldBe true

        drainStack(driver, discardCandidate)

        driver.getGraveyardCardNames(opponent).contains("Lightning Bolt") shouldBe true
        driver.getLifeTotal(opponent) shouldBe lifeBefore - 2
        // My life is untouched — the damage goes to "that player" (the discarder), not me.
        driver.getLifeTotal(me) shouldBe 20
    }

    test("your own discard does not trigger Megrim") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = driver.activePlayer!!
        val opponent = driver.getOpponent(me)

        driver.putPermanentOnBattlefield(me, "Megrim")

        val lifeBefore = driver.getLifeTotal(me)

        val discardCandidate = driver.putCardInHand(me, "Lightning Bolt")
        val carefulStudy = driver.putCardInHand(me, "Careful Study")
        driver.giveMana(me, Color.BLACK, 1)

        driver.submit(
            CastSpell(
                playerId = me,
                cardId = carefulStudy,
                paymentStrategy = PaymentStrategy.FromPool
            )
        ).isSuccess shouldBe true

        drainStack(driver, discardCandidate)

        driver.getGraveyardCardNames(me).contains("Lightning Bolt") shouldBe true
        // I discarded my own card, not an opponent's — Megrim (which I control) stays silent.
        driver.getLifeTotal(me) shouldBe lifeBefore
        driver.getLifeTotal(opponent) shouldBe 20
    }
})
