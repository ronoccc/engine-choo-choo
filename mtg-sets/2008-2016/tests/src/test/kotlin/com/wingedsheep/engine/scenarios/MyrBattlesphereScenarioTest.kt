package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Myr Battlesphere (SOM #180) — {7} Artifact Creature — Myr Construct, 4/7.
 *
 * "When this creature enters, create four 1/1 colorless Myr artifact creature tokens.
 *  Whenever this creature attacks, you may tap X untapped Myr you control. If you do, this
 *  creature gets +X/+0 until end of turn and deals X damage to the player or planeswalker it's
 *  attacking."
 */
class MyrBattlesphereScenarioTest : ScenarioTestBase() {

    init {
        context("Myr Battlesphere") {

            test("entering creates four 1/1 Myr tokens") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Myr Battlesphere")
                    .withLandsOnBattlefield(1, "Wastes", 7)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castSpell(1, "Myr Battlesphere").error shouldBe null
                game.resolveStack()

                withClue("four 1/1 Myr tokens were created") {
                    game.findPermanents("Myr Token").size shouldBe 4
                }
            }

            test("attacking and tapping a Myr pumps power and deals bonus damage to the defending player") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Myr Battlesphere", summoningSickness = false)
                    .withCardOnBattlefield(1, "Palladium Myr", summoningSickness = false)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val lifeBefore = game.getLifeTotal(2)
                game.advanceToPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                game.declareAttackers(mapOf("Myr Battlesphere" to 2)).error shouldBe null
                game.resolveStack()

                withClue("the attack trigger prompts to select untapped Myr to tap") {
                    game.hasPendingDecision() shouldBe true
                }
                game.selectCards(listOf(game.findPermanent("Palladium Myr")!!)).error shouldBe null
                game.resolveStack()

                withClue("tapping 1 Myr means X=1: the trigger's own damage (before combat damage is even dealt) is 1") {
                    game.getLifeTotal(2) shouldBe lifeBefore - 1
                }
            }
        }
    }
}
