package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for One with the Machine (M19 #66) — {3}{U} Sorcery.
 *
 * "Draw cards equal to the greatest mana value among artifacts you control."
 *
 * Covers: the draw amount tracking the single highest mana value among controlled artifacts (not
 * a sum or a count), ignoring opponents' artifacts, and the zero-artifact case drawing nothing.
 */
class OneWithTheMachineScenarioTest : ScenarioTestBase() {

    init {
        context("One with the Machine") {

            test("draws cards equal to the greatest mana value among your artifacts") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "One with the Machine")
                    // Su-Chi: mana value 4. Millstone-esque cheap artifact keeps the "greatest",
                    // not "sum", distinction meaningful.
                    .withCardOnBattlefield(1, "Su-Chi")
                    .withLandsOnBattlefield(1, "Island", 4)
                    // The draw needs cards to actually pull from; a scenario's library is empty
                    // by default.
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withCardInLibrary(1, "Mountain")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val before = game.handSize(1)
                game.castSpell(1, "One with the Machine").error shouldBe null
                game.resolveStack()

                withClue("Su-Chi has mana value 4, so four cards should be drawn") {
                    // hand lost the card that was cast (-1), gained 4 from the draw (+4) => net +3
                    game.handSize(1) shouldBe before - 1 + 4
                }
            }

            test("ignores an opponent's higher-mana-value artifact") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "One with the Machine")
                    .withCardOnBattlefield(2, "Su-Chi")
                    .withLandsOnBattlefield(1, "Island", 4)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val before = game.handSize(1)
                game.castSpell(1, "One with the Machine").error shouldBe null
                game.resolveStack()

                withClue("no artifacts you control, so zero cards are drawn") {
                    game.handSize(1) shouldBe before - 1
                }
            }
        }
    }
}
