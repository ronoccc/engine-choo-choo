package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Metalwork Colossus (KLD #222) — {11} Artifact Creature — Construct, 10/10.
 *
 * "This spell costs {X} less to cast, where X is the total mana value of noncreature artifacts
 *  you control.
 *  Sacrifice two artifacts: Return this card from your graveyard to your hand."
 */
class MetalworkColossusScenarioTest : ScenarioTestBase() {

    init {
        context("Metalwork Colossus") {

            test("costs {X} less where X is the total mana value of noncreature artifacts controlled") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Metalwork Colossus")
                    // Two noncreature artifacts summing mana value 8: Sol Ring (1) is a creature?
                    // no, Sol Ring is noncreature; use two Ornithopters instead since those are
                    // creatures. Pick clearly-noncreature artifacts from the fixture set.
                    .withCardOnBattlefield(1, "Sol Ring") // {1}, noncreature artifact, mv 1
                    .withLandsOnBattlefield(1, "Wastes", 10) // 11 - 1 = 10 needed
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castSpell(1, "Metalwork Colossus").error shouldBe null

                withClue("Sol Ring (mv 1) reduces the {11} cost by {1}, affordable with 10 lands") {
                    game.state.stack.isNotEmpty() shouldBe true
                }
            }

            test("Sacrifice two artifacts: returns the card from graveyard to hand") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInGraveyard(1, "Metalwork Colossus")
                    .withCardOnBattlefield(1, "Sol Ring", summoningSickness = false)
                    .withCardOnBattlefield(1, "Ornithopter", summoningSickness = false)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val def = cardRegistry.getCard("Metalwork Colossus")!!
                val abilityId = def.activatedAbilities.first().id
                val colossusId = game.findCardsInGraveyard(1, "Metalwork Colossus").first()

                val result = game.execute(
                    com.wingedsheep.engine.core.ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = colossusId,
                        abilityId = abilityId,
                        costPayment = com.wingedsheep.sdk.scripting.AdditionalCostPayment(
                            sacrificedPermanents = listOf(
                                game.findPermanent("Sol Ring")!!,
                                game.findPermanent("Ornithopter")!!
                            )
                        )
                    )
                )
                result.error shouldBe null
                game.resolveStack()

                withClue("both artifacts were sacrificed and Metalwork Colossus returned to hand") {
                    game.isInGraveyard(1, "Sol Ring") shouldBe true
                    game.isInGraveyard(1, "Ornithopter") shouldBe true
                    game.isInHand(1, "Metalwork Colossus") shouldBe true
                }
            }
        }
    }
}
