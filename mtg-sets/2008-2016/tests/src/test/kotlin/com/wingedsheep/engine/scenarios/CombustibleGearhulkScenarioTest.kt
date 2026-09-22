package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Combustible Gearhulk (KLD #112) — {4}{R}{R} Artifact Creature — Construct, 6/6.
 *
 * "First strike
 *  When this creature enters, target opponent may have you draw three cards. If the player
 *  doesn't, you mill three cards, then this creature deals damage to that player equal to the
 *  total mana value of those cards."
 *
 * Covers both branches of the target opponent's decision.
 */
class CombustibleGearhulkScenarioTest : ScenarioTestBase() {

    init {
        context("Combustible Gearhulk") {

            test("if the opponent declines, controller mills three and opponent takes damage equal to their mana value") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Combustible Gearhulk")
                    .withLandsOnBattlefield(1, "Mountain", 6)
                    // Library top-to-bottom order isn't guaranteed by this fixture, but three
                    // fixed-mana-value cards let us assert the resulting damage regardless of
                    // ordering, since all three always get milled.
                    .withCardInLibrary(1, "Mountain") // mv 0
                    .withCardInLibrary(1, "Mountain") // mv 0
                    .withCardInLibrary(1, "Mountain") // mv 0
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castSpell(1, "Combustible Gearhulk").error shouldBe null
                game.resolveStack()

                withClue("the may-decide gate needs the target opponent's decision") {
                    game.hasPendingDecision() shouldBe true
                }
                game.answerYesNo(false).error shouldBe null
                game.resolveStack()

                withClue("three Mountains (mv 0 each) were milled, so zero damage is dealt") {
                    game.getLifeTotal(2) shouldBe 20
                    game.graveyardSize(1) shouldBe 3
                }
            }

            test("if the opponent agrees, the controller draws three cards and no damage is dealt") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Combustible Gearhulk")
                    .withLandsOnBattlefield(1, "Mountain", 6)
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val handBefore = game.handSize(1)
                game.castSpell(1, "Combustible Gearhulk").error shouldBe null
                game.resolveStack()

                game.answerYesNo(true).error shouldBe null
                game.resolveStack()

                withClue("controller drew three cards instead of milling") {
                    // hand lost Combustible Gearhulk (-1), gained 3 from the draw => net +2
                    game.handSize(1) shouldBe handBefore - 1 + 3
                    game.getLifeTotal(2) shouldBe 20
                }
            }
        }
    }
}
