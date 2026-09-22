package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Depthshaker Titan (EOC #9) — {5}{R}{R} Artifact Creature — Robot, 5/5.
 *
 * "When this creature enters, any number of target noncreature artifacts you control become 3/3
 *  artifact creatures. Sacrifice them at the beginning of the next end step.
 *  Each artifact creature you control has melee, trample, and haste."
 */
class DepthshakerTitanScenarioTest : ScenarioTestBase() {

    init {
        context("Depthshaker Titan") {

            test("targeted noncreature artifacts become 3/3 creatures, then are sacrificed at the next end step") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Depthshaker Titan")
                    .withCardOnBattlefield(1, "Sol Ring")
                    .withLandsOnBattlefield(1, "Mountain", 7)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val solRing = game.findPermanent("Sol Ring")!!
                game.castSpell(1, "Depthshaker Titan").error shouldBe null
                game.resolveStack()

                withClue("the ETB trigger needs to choose which noncreature artifacts to animate") {
                    game.hasPendingDecision() shouldBe true
                }
                game.selectTargets(listOf(solRing)).error shouldBe null
                game.resolveStack()

                withClue("Sol Ring is now a 3/3 artifact creature") {
                    game.state.projectedState.getPower(solRing) shouldBe 3
                    game.state.projectedState.getToughness(solRing) shouldBe 3
                }

                game.passUntilPhase(Phase.ENDING, Step.END)
                game.resolveStack()

                withClue("Sol Ring was sacrificed at the next end step") {
                    game.isInGraveyard(1, "Sol Ring") shouldBe true
                }
            }

            test("artifact creatures gain haste and can attack the turn they enter") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Depthshaker Titan", summoningSickness = true)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                withClue("Depthshaker Titan is itself an artifact creature, so its own static grants it haste") {
                    game.advanceToPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    game.declareAttackers(mapOf("Depthshaker Titan" to 2)).error shouldBe null
                }
            }
        }
    }
}
