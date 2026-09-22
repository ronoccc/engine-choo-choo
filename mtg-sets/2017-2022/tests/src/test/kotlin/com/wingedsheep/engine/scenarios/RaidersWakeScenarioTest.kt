package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Raiders' Wake (XLN #116) — {3}{B} Enchantment.
 *
 * "Whenever an opponent discards a card, that player loses 2 life.
 *  Raid — At the beginning of your end step, if you attacked this turn, target opponent discards
 *  a card."
 *
 * Covers: the discard-triggered life loss firing off any discard (including its own Raid
 * ability), the Raid trigger's intervening-if gating on having attacked this turn, and the trigger
 * simply not firing on an end step where the controller didn't attack.
 */
class RaidersWakeScenarioTest : ScenarioTestBase() {

    init {
        context("Raiders' Wake") {

            test("Raid fires after attacking: opponent discards and loses 2 life from the first ability") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Raiders' Wake")
                    .withCardOnBattlefield(1, "Grizzly Bears", summoningSickness = false)
                    .withCardInHand(2, "Hill Giant") // the only card, so it's the auto-chosen discard
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.advanceToPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                game.declareAttackers(mapOf("Grizzly Bears" to 2)).error shouldBe null
                game.passUntilPhase(Phase.ENDING, Step.END)
                // Raid's target-opponent-discards trigger goes on the stack here; with only one
                // opponent at the table there's exactly one legal target, so the engine
                // auto-resolves it rather than pausing for a decision (unlike an ability with a
                // genuine choice among several legal targets).
                game.resolveStack() // combat damage, then Raid's discard, then the life-loss trigger

                withClue("Player2 discarded their only card") {
                    game.handSize(2) shouldBe 0
                    game.isInGraveyard(2, "Hill Giant") shouldBe true
                }
                withClue("attacking dealt 2 combat damage, and the discard cost 2 more life (20 -> 16)") {
                    game.getLifeTotal(2) shouldBe 16
                }
            }

            test("no Raid trigger on an end step where the controller didn't attack") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Raiders' Wake")
                    .withCardInHand(2, "Hill Giant")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.passUntilPhase(Phase.ENDING, Step.END)
                game.resolveStack()

                withClue("no attack happened, so Raid's intervening-if never fires") {
                    game.hasPendingDecision() shouldBe false
                }
                game.handSize(2) shouldBe 1
                game.getLifeTotal(2) shouldBe 20
            }

            test("the discard-life-loss ability fires off any opponent discard, not just Raid's") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Raiders' Wake")
                    .withCardInHand(1, "Mind Rot") // {2}{B} sorcery: target player discards two cards
                    .withCardInHand(2, "Hill Giant")
                    .withCardInHand(2, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Swamp", 3)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castSpellTargetingPlayer(1, "Mind Rot", 2).error shouldBe null
                game.resolveStack()

                withClue("two discards, each triggering the life-loss ability once, for 4 total") {
                    game.handSize(2) shouldBe 0
                    game.getLifeTotal(2) shouldBe 16
                }
            }
        }
    }
}
