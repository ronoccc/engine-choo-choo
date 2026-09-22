package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Delete (WHO #81) — {X}{R}{R} Sorcery.
 *
 * "Delete deals X damage to each nonartifact creature and each player."
 *
 * Covers: X damage hitting both players, a nonartifact creature dying to it, an artifact creature
 * surviving untouched, and X = 0 being a legal no-op cast.
 */
class DeleteScenarioTest : ScenarioTestBase() {

    init {
        context("Delete") {

            test("deals X damage to each player and each nonartifact creature") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Delete")
                    // Grizzly Bears: 2/2 nonartifact creature, dies to 3 damage.
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Mountain", 5)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castXSpell(1, "Delete", 3).error shouldBe null
                game.resolveStack()

                withClue("caster takes X damage too") {
                    game.getLifeTotal(1) shouldBe 17
                }
                withClue("opponent takes X damage") {
                    game.getLifeTotal(2) shouldBe 17
                }
                withClue("the nonartifact creature dies to 3 damage on a 2/2") {
                    game.isOnBattlefield("Grizzly Bears") shouldBe false
                }
            }

            test("an artifact creature survives untouched") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    // Su-Chi: 3/4 artifact creature.
                    .withCardInHand(1, "Delete")
                    .withCardOnBattlefield(2, "Su-Chi")
                    .withLandsOnBattlefield(1, "Mountain", 8)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castXSpell(1, "Delete", 6).error shouldBe null
                game.resolveStack()

                withClue("Su-Chi is an artifact creature, so it's excluded from the sweep") {
                    game.isOnBattlefield("Su-Chi") shouldBe true
                }
                withClue("players still take the X damage") {
                    game.getLifeTotal(1) shouldBe 14
                    game.getLifeTotal(2) shouldBe 14
                }
            }

            test("X = 0 is a legal cast that deals no damage") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Delete")
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castXSpell(1, "Delete", 0).error shouldBe null
                game.resolveStack()

                game.getLifeTotal(1) shouldBe 20
                game.getLifeTotal(2) shouldBe 20
                game.isOnBattlefield("Grizzly Bears") shouldBe true
            }
        }
    }
}
