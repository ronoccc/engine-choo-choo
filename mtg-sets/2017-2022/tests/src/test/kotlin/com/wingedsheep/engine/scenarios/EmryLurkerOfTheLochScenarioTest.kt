package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Emry, Lurker of the Loch (ELD #43) — {1}{U} Legendary Creature — Merfolk
 * Wizard, 1/2.
 *
 * "Affinity for artifacts
 *  When Emry enters, mill four cards.
 *  {T}: Choose target artifact card in your graveyard. You may cast that card this turn."
 */
class EmryLurkerOfTheLochScenarioTest : ScenarioTestBase() {

    init {
        context("Emry, Lurker of the Loch") {

            test("entering mills four cards") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Emry, Lurker of the Loch")
                    .withLandsOnBattlefield(1, "Island", 2)
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val graveyardBefore = game.graveyardSize(1)
                game.castSpell(1, "Emry, Lurker of the Loch").error shouldBe null
                game.resolveStack()

                withClue("four cards were milled") {
                    game.graveyardSize(1) shouldBe graveyardBefore + 4
                }
            }

            test("the tap ability grants permission to cast the chosen artifact card this turn") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Emry, Lurker of the Loch", summoningSickness = false)
                    .withCardInGraveyard(1, "Sol Ring")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val def = cardRegistry.getCard("Emry, Lurker of the Loch")!!
                val abilityId = def.activatedAbilities.first().id
                val emryId = game.findPermanent("Emry, Lurker of the Loch")!!
                val solRingId = game.findCardsInGraveyard(1, "Sol Ring").first()

                val result = game.execute(
                    com.wingedsheep.engine.core.ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = emryId,
                        abilityId = abilityId,
                        targets = listOf(
                            com.wingedsheep.engine.state.components.stack.ChosenTarget.Card(
                                cardId = solRingId,
                                ownerId = game.player1Id,
                                zone = com.wingedsheep.sdk.core.Zone.GRAVEYARD
                            )
                        )
                    )
                )
                result.error shouldBe null
                game.resolveStack()

                withClue("Sol Ring can now be cast from the graveyard") {
                    game.getLegalActions(1).any {
                        val action = it.action
                        action is com.wingedsheep.engine.core.CastSpell && action.cardId == solRingId
                    } shouldBe true
                }
            }
        }
    }
}
