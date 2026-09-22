package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for This Is How It Ends (WHO #70) — {3}{B} Instant.
 *
 * "Target creature's owner shuffles it into their library, then faces a villainous choice —
 *  They lose 5 life, or they shuffle another creature they own into their library."
 */
class ThisIsHowItEndsScenarioTest : ScenarioTestBase() {

    init {
        context("This Is How It Ends") {

            test("shuffles the target creature away, then the target's owner chooses to lose 5 life") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "This Is How It Ends")
                    .withLandsOnBattlefield(1, "Swamp", 4)
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val bears = game.findPermanent("Grizzly Bears")!!
                val lifeBefore = game.getLifeTotal(2)
                game.castSpell(1, "This Is How It Ends", targetId = bears).error shouldBe null
                game.resolveStack()

                withClue("Grizzly Bears was shuffled into its owner's library") {
                    game.findPermanent("Grizzly Bears") shouldBe null
                }

                withClue("the target's owner (Player2) faces the villainous choice") {
                    game.hasPendingDecision() shouldBe true
                }
                val decision = game.state.pendingDecision as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(decision.id, 0)).error shouldBe null
                game.resolveStack()

                withClue("choosing to lose 5 life reduces Player2 from 20 to 15") {
                    game.getLifeTotal(2) shouldBe lifeBefore - 5
                }
            }
        }
    }
}
