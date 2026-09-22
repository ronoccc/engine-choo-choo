package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario tests for Desynchronization (ACR #16) — {2}{U}{U} Instant.
 *
 * "Return each nonland permanent that's not historic to its owner's hand. (Artifacts,
 *  legendaries, and Sagas are historic.)"
 */
class DesynchronizationScenarioTest : ScenarioTestBase() {

    init {
        context("Desynchronization") {

            test("bounces non-historic nonland permanents on both sides, leaving artifacts, legendaries and lands") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Desynchronization")
                    .withLandsOnBattlefield(1, "Island", 4)
                    .withCardOnBattlefield(1, "Grizzly Bears") // vanilla, not historic
                    .withCardOnBattlefield(1, "Ornithopter") // artifact, historic
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.castSpell(1, "Desynchronization").error shouldBe null
                game.resolveStack()

                withClue("both Grizzly Bears were bounced; Ornithopter (artifact, historic) stayed") {
                    game.findAllPermanents("Grizzly Bears").size shouldBe 0
                    game.findPermanent("Ornithopter") shouldNotBe null
                    game.isInHand(1, "Grizzly Bears") shouldBe true
                    game.isInHand(2, "Grizzly Bears") shouldBe true
                }
                withClue("lands are untouched") {
                    game.findAllPermanents("Island").size shouldBe 4
                }
            }
        }
    }
}
