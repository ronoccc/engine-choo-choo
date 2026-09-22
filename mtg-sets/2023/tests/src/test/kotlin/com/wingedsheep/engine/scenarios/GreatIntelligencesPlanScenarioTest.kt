package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Great Intelligence's Plan (WHO #133) — {4}{U}{B} Sorcery.
 *
 * "Draw three cards. Then target opponent faces a villainous choice — They discard three cards,
 *  or you may cast a spell from your hand without paying its mana cost."
 */
class GreatIntelligencesPlanScenarioTest : ScenarioTestBase() {

    init {
        context("Great Intelligence's Plan") {

            test("draws three cards, then the target opponent chooses to discard three") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Great Intelligence's Plan")
                    .withLandsOnBattlefield(1, "Island", 3)
                    .withLandsOnBattlefield(1, "Swamp", 3)
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInHand(2, "Hill Giant")
                    .withCardInHand(2, "Grizzly Bears")
                    .withCardInHand(2, "Mountain")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val handBefore1 = game.handSize(1)
                game.castSpellTargetingPlayer(1, "Great Intelligence's Plan", 2).error shouldBe null
                game.resolveStack()

                withClue("three cards were drawn (net +2: -1 for the spell cast, +3 drawn)") {
                    game.handSize(1) shouldBe handBefore1 - 1 + 3
                }

                withClue("the targeted opponent faces the villainous choice") {
                    game.hasPendingDecision() shouldBe true
                }
                val decision = game.state.pendingDecision as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(decision.id, 0)).error shouldBe null
                game.resolveStack()

                withClue("Player2 discarded three cards, emptying their hand") {
                    game.handSize(2) shouldBe 0
                }
            }
        }
    }
}
