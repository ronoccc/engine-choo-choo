package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Mechanized Production (AER #38) — {2}{U}{U} Enchantment — Aura.
 *
 * "Enchant artifact you control
 *  At the beginning of your upkeep, create a token that's a copy of enchanted artifact. Then if
 *  you control eight or more artifacts with the same name as one another, you win the game."
 */
class MechanizedProductionScenarioTest : ScenarioTestBase() {

    init {
        context("Mechanized Production") {

            test("at your upkeep, creates a token copy of the enchanted artifact") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Ornithopter")
                    .withCardAttachedTo(1, "Mechanized Production", "Ornithopter")
                    .withActivePlayer(2)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val ornithoptersBefore = game.findPermanents("Ornithopter").size
                game.passUntilPhase(Phase.ENDING, Step.END)
                game.passUntilPhase(Phase.BEGINNING, Step.UPKEEP)
                game.resolveStack()

                withClue("a copy of Ornithopter was created") {
                    game.findPermanents("Ornithopter").size shouldBe ornithoptersBefore + 1
                }
                withClue("only one copy exists so far, far short of the eight needed to win") {
                    game.state.gameOver shouldBe false
                }
            }

            test("controlling eight or more same-named artifacts at upkeep wins the game") {
                val builder = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Ornithopter")
                var b = builder
                // Seven more Ornithopters already on the battlefield; the upkeep trigger's token
                // copy makes eight.
                repeat(6) { b = b.withCardOnBattlefield(1, "Ornithopter") }
                val game = b
                    .withCardAttachedTo(1, "Mechanized Production", "Ornithopter")
                    .withActivePlayer(2)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.passUntilPhase(Phase.ENDING, Step.END)
                game.passUntilPhase(Phase.BEGINNING, Step.UPKEEP)
                game.resolveStack()

                withClue("eight same-named artifacts (Ornithopter) triggers the win condition") {
                    game.state.gameOver shouldBe true
                    game.state.winnerId shouldBe game.player1Id
                }
            }
        }
    }
}
