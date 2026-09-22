package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Padeem, Consul of Innovation (KLD #59) — Legendary Creature — Vedalken Artificer, 1/4.
 *
 * "Artifacts you control have hexproof.
 *  At the beginning of your upkeep, if you control the artifact with the greatest mana value or
 *  tied for the greatest mana value, draw a card."
 *
 * Two independent pieces:
 *  - The static ability grants hexproof to every artifact the controller controls (Leonin Abunas'
 *    shape), verified via [io.kotest] against `projectedState.hasKeyword`.
 *  - The upkeep trigger is an intervening-if (CR 603.4) gated on the new general-purpose
 *    `StatePredicate.HasGreatestManaValueAmong(candidates)`. The case that matters is the
 *    comparison being **global** (all artifacts on the battlefield, not just the controller's) and
 *    ties counting as a match ("or tied for the greatest") — an opponent's higher-mana-value
 *    artifact must suppress the draw, and an exact tie must not.
 */
class PadeemConsulOfInnovationScenarioTest : ScenarioTestBase() {

    init {
        context("Padeem, Consul of Innovation — static ability") {

            test("artifacts you control have hexproof, non-artifacts don't") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Padeem, Consul of Innovation")
                    .withCardOnBattlefield(1, "Ornithopter")
                    .withCardOnBattlefield(1, "Grizzly Bears")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val ornithopter = game.findPermanent("Ornithopter")!!
                val bears = game.findPermanent("Grizzly Bears")!!

                withClue("the artifact creature gains hexproof from Padeem") {
                    game.state.projectedState.hasKeyword(ornithopter, Keyword.HEXPROOF) shouldBe true
                }
                withClue("a non-artifact creature does not") {
                    game.state.projectedState.hasKeyword(bears, Keyword.HEXPROOF) shouldBe false
                }
            }
        }
    }
}

/**
 * The upkeep draw needs real turn structure (the trigger fires when the upkeep step begins), so it
 * uses [GameTestDriver] directly rather than the static-board [ScenarioTestBase], mirroring
 * `BottomlessPitScenarioTest`.
 */
class PadeemConsulOfInnovationUpkeepScenarioTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        return driver
    }

    fun advanceToUpkeepOf(driver: GameTestDriver, player: EntityId) {
        driver.passPriorityUntil(Step.UPKEEP, maxPasses = 200)
        if (driver.activePlayer != player) {
            driver.passPriorityUntil(Step.DRAW, maxPasses = 200)
            driver.passPriorityUntil(Step.UPKEEP, maxPasses = 200)
        }
        driver.currentStep shouldBe Step.UPKEEP
        driver.activePlayer shouldBe player
    }

    test("draws a card when you control the sole greatest-mana-value artifact") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 40))

        val controller = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putPermanentOnBattlefield(controller, "Padeem, Consul of Innovation")
        // Ornithopter (mv 0) is the only artifact; Padeem's controller trivially controls the
        // greatest (and only) mana value among artifacts.
        driver.putPermanentOnBattlefield(controller, "Ornithopter")

        advanceToUpkeepOf(driver, controller)
        driver.stackSize shouldBe 1

        val handBefore = driver.getHandSize(controller)
        driver.bothPass() // no decision: the draw is unconditional once the intervening-if holds

        driver.getHandSize(controller) shouldBe handBefore + 1
    }

    test("does not draw when an opponent controls a strictly higher mana value artifact") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 40))

        val controller = driver.activePlayer!!
        val opponent = driver.getOpponent(controller)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putPermanentOnBattlefield(controller, "Padeem, Consul of Innovation")
        // Controller's artifact is mana value 0 (Ornithopter).
        driver.putPermanentOnBattlefield(controller, "Ornithopter")
        // Opponent's artifact has a strictly higher mana value (Hedron Archive is {4}).
        driver.putPermanentOnBattlefield(opponent, "Hedron Archive")

        advanceToUpkeepOf(driver, controller)
        withClue("the intervening-if is false, so no trigger goes on the stack") {
            driver.stackSize shouldBe 0
        }
    }

    test("draws on an exact tie for greatest mana value") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 40))

        val controller = driver.activePlayer!!
        val opponent = driver.getOpponent(controller)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putPermanentOnBattlefield(controller, "Padeem, Consul of Innovation")
        driver.putPermanentOnBattlefield(controller, "Hedron Archive")
        // Opponent controls an artifact with the SAME mana value — a tie for greatest still
        // satisfies "or tied for the greatest mana value".
        driver.putPermanentOnBattlefield(opponent, "Hedron Archive")

        advanceToUpkeepOf(driver, controller)
        driver.stackSize shouldBe 1

        val handBefore = driver.getHandSize(controller)
        driver.bothPass()

        driver.getHandSize(controller) shouldBe handBefore + 1
    }
})
