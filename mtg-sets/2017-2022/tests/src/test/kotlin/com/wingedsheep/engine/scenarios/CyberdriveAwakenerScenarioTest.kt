package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Cyberdrive Awakener (NEC) — {5}{U} Artifact Creature — Construct, 4/4.
 *
 * "Flying
 *  Other artifact creatures you control have flying.
 *  When this creature enters, each noncreature artifact you control becomes a 4/4 artifact
 *  creature until end of turn."
 */
class CyberdriveAwakenerScenarioTest : ScenarioTestBase() {

    init {
        context("Cyberdrive Awakener") {

            test("grants flying to other artifact creatures you control, not itself") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Cyberdrive Awakener")
                    .withCardOnBattlefield(1, "Ornithopter") // already flies natively; use Palladium Myr instead
                    .withCardOnBattlefield(1, "Palladium Myr")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val myr = game.findPermanent("Palladium Myr")!!
                withClue("Palladium Myr has no flying printed, so the lord static is the only source") {
                    game.state.projectedState.getKeywords(myr).contains(com.wingedsheep.sdk.core.Keyword.FLYING.name) shouldBe true
                }
            }

            test("entering animates noncreature artifacts to 4/4 until end of turn") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Cyberdrive Awakener")
                    .withCardOnBattlefield(1, "Sol Ring") // noncreature artifact
                    .withLandsOnBattlefield(1, "Island", 6)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val solRing = game.findPermanent("Sol Ring")!!
                game.castSpell(1, "Cyberdrive Awakener").error shouldBe null
                game.resolveStack()

                withClue("Sol Ring is now a 4/4 artifact creature") {
                    game.state.projectedState.getPower(solRing) shouldBe 4
                    game.state.projectedState.getToughness(solRing) shouldBe 4
                }
            }
        }
    }
}
