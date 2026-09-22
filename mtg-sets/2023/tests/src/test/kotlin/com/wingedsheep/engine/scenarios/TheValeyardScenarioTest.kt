package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for The Valeyard (WHO #165) — {2}{U}{B}{R} Legendary Creature — Time Lord Noble,
 * 4/5.
 *
 * "If an opponent would face a villainous choice, they face that choice an additional time.
 *  (They can make the same or different choices.)
 *  While voting, you may vote an additional time."
 *
 * Only the first ability (implemented as the [com.wingedsheep.sdk.scripting.VillainousChoiceExtraForOpponents]
 * marker) is covered — the voting ability is an acknowledged unimplemented TODO (no vote mechanic
 * exists in this engine at all).
 */
class TheValeyardScenarioTest : ScenarioTestBase() {

    init {
        context("The Valeyard") {

            test("an opponent facing a villainous choice faces it twice while The Valeyard is out") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "The Valeyard", summoningSickness = false)
                    .withCardInHand(1, "This Is How It Ends")
                    .withLandsOnBattlefield(1, "Swamp", 4)
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val bears = game.findPermanent("Grizzly Bears")!!
                val lifeBefore = game.getLifeTotal(2)
                game.castSpell(1, "This Is How It Ends", targetId = bears).error shouldBe null
                game.resolveStack()

                withClue("first villainous choice: Player2 (an opponent of The Valeyard's controller) chooses to lose 5 life") {
                    game.hasPendingDecision() shouldBe true
                }
                var decision = game.state.pendingDecision as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(decision.id, 0)).error shouldBe null
                game.resolveStack()

                withClue("The Valeyard doubles it: Player2 faces the villainous choice a second time") {
                    game.hasPendingDecision() shouldBe true
                }
                decision = game.state.pendingDecision as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(decision.id, 0)).error shouldBe null
                game.resolveStack()

                withClue("losing 5 life twice: 20 -> 10") {
                    game.getLifeTotal(2) shouldBe lifeBefore - 10
                }
            }
        }
    }
}
