package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Davros, Dalek Creator (WHO #1) — {1}{U}{B}{R} Legendary Artifact Creature —
 * Alien Scientist, 3/4.
 *
 * "Menace
 *  At the beginning of your end step, create a 3/3 black Dalek artifact creature token with
 *  menace if an opponent lost 3 or more life this turn. Then each opponent who lost 3 or more
 *  life this turn faces a villainous choice — You draw a card, or that player discards a card."
 */
class DavrosDalekCreatorScenarioTest : ScenarioTestBase() {

    init {
        context("Davros, Dalek Creator") {

            test("if an opponent lost 3+ life this turn, creates a Dalek token and they face a villainous choice") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Davros, Dalek Creator", summoningSickness = false)
                    .withCardOnBattlefield(1, "Hill Giant", summoningSickness = false) // 3/3
                    .withCardInLibrary(1, "Mountain")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val lifeBefore = game.getLifeTotal(2)
                game.advanceToPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                game.declareAttackers(mapOf("Hill Giant" to 2)).error shouldBe null
                game.passUntilPhase(Phase.ENDING, Step.END)
                game.resolveStack()

                withClue("Hill Giant dealt 3 combat damage, satisfying the 3-life-lost gate") {
                    game.getLifeTotal(2) shouldBe lifeBefore - 3
                }
                withClue("a 3/3 Dalek token was created") {
                    game.findAllPermanents("Dalek Token").size shouldBe 1
                }
                // Player2 has no cards, so "that player discards a card" is infeasible and the
                // villainous choice auto-executes its only feasible option ("You draw a card")
                // without pausing for a decision at all (ChooseActionEffectExecutor: a single
                // feasible choice auto-resolves).
                withClue("Player1 drew a card from the auto-resolved villainous choice") {
                    game.handSize(1) shouldBe 1
                }
            }
        }
    }
}
