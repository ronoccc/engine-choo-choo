package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Noxious Gearhulk (KLD #96) — {4}{B}{B} Artifact Creature — Construct, 5/4.
 *
 * "Menace
 *  When this creature enters, you may destroy another target creature. If a creature is
 *  destroyed this way, you gain life equal to its toughness."
 */
class NoxiousGearhulkScenarioTest : ScenarioTestBase() {

    init {
        context("Noxious Gearhulk") {

            test("destroying a target creature gains life equal to its toughness") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Noxious Gearhulk")
                    .withCardOnBattlefield(2, "Grizzly Bears") // 2/2
                    .withLandsOnBattlefield(1, "Swamp", 6)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val lifeBefore = game.getLifeTotal(1)
                game.castSpell(1, "Noxious Gearhulk").error shouldBe null
                game.resolveStack()

                withClue("the optional destroy trigger needs a yes/no, since it may be declined") {
                    game.hasPendingDecision() shouldBe true
                }
                game.answerYesNo(true).error shouldBe null
                game.resolveStack()

                if (game.hasPendingDecision()) {
                    game.selectTargets(listOf(game.findPermanent("Grizzly Bears")!!)).error shouldBe null
                    game.resolveStack()
                }

                withClue("Grizzly Bears (toughness 2) died, gaining its controller 2 life") {
                    game.isInGraveyard(2, "Grizzly Bears") shouldBe true
                    game.getLifeTotal(1) shouldBe lifeBefore + 2
                }
            }

            test("declining the optional trigger destroys nothing and gains no life") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Noxious Gearhulk")
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Swamp", 6)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val lifeBefore = game.getLifeTotal(1)
                game.castSpell(1, "Noxious Gearhulk").error shouldBe null
                game.resolveStack()
                game.answerYesNo(false).error shouldBe null
                game.resolveStack()

                withClue("declined: Grizzly Bears survives and no life is gained") {
                    game.isInGraveyard(2, "Grizzly Bears") shouldBe false
                    game.getLifeTotal(1) shouldBe lifeBefore
                }
            }
        }
    }
}
