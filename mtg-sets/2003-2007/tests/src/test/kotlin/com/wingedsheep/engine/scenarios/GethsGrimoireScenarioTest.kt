package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.handlers.continuations.entityIdToChosenTarget
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Geth's Grimoire.
 *
 * Geth's Grimoire ({4}): Artifact — Book
 * "Whenever an opponent discards a card, you may draw a card."
 */
class GethsGrimoireScenarioTest : ScenarioTestBase() {

    init {
        context("Geth's Grimoire's discard trigger") {

            test("may draw a card when an opponent discards, if the controller accepts") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Geth's Grimoire")
                    .withCardInHand(1, "Mind Rot")
                    .withCardInHand(2, "Grizzly Bears")
                    .withCardInHand(2, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Swamp", 3)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val beforeHand = game.handSize(1)

                val mindRot = game.findCardsInHand(1, "Mind Rot").single()
                val cast = game.execute(
                    CastSpell(
                        playerId = game.player1Id,
                        cardId = mindRot,
                        targets = listOf(entityIdToChosenTarget(game.state, game.player2Id)),
                    )
                )
                withClue("Mind Rot cast: ${cast.error}") { cast.error shouldBe null }
                game.resolveStack()

                // Mind Rot discards two cards at once, so Geth's Grimoire's per-card trigger
                // fires twice. Accept the optional draw on the first resolution.
                game.answerYesNo(true)
                game.resolveStack()

                withClue("Player1 spent Mind Rot (-1) then drew from the first discard trigger (+1)") {
                    game.handSize(1) shouldBe beforeHand
                }

                // Decline the second trigger (from the second discarded card).
                game.answerYesNo(false)
                game.resolveStack()

                withClue("Declining the second trigger leaves hand size unchanged") {
                    game.handSize(1) shouldBe beforeHand
                }
            }

            test("does not trigger when the controller discards, only when an opponent does") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Geth's Grimoire")
                    .withCardInHand(2, "Mind Rot")
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInHand(1, "Grizzly Bears")
                    .withLandsOnBattlefield(2, "Swamp", 3)
                    .withActivePlayer(2)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val beforeHand = game.handSize(1)

                val mindRot = game.findCardsInHand(2, "Mind Rot").single()
                val cast = game.execute(
                    CastSpell(
                        playerId = game.player2Id,
                        cardId = mindRot,
                        targets = listOf(entityIdToChosenTarget(game.state, game.player1Id)),
                    )
                )
                withClue("Mind Rot cast: ${cast.error}") { cast.error shouldBe null }
                game.resolveStack()

                withClue("Grimoire's controller discarding their own cards is not 'an opponent' discarding") {
                    game.state.pendingDecision shouldBe null
                }
                withClue("No draw happened, so Player1's hand only shrank from discarding") {
                    game.handSize(1) shouldBe beforeHand - 2
                }
            }
        }
    }
}
