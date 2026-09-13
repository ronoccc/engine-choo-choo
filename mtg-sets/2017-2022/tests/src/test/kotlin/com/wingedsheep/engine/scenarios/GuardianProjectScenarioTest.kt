package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.SelectManaSourcesDecision
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Scenario tests for Guardian Project (RNA #130).
 *
 * Whenever a nontoken creature you control enters, if it doesn't have the same name as another
 * creature you control or a creature card in your graveyard, draw a card.
 *
 * Covers the new [com.wingedsheep.sdk.scripting.conditions.SameNameAsAnotherControlledPermanentOrGraveyardCard]
 * condition through the one card that needs it: a distinctly-named creature draws, a same-named
 * creature already on the battlefield stops the draw, and a same-named creature card sitting in
 * the graveyard also stops the draw — the two halves the older, battlefield-only
 * `AnotherPermanentWithSameNameAsTarget` couldn't express together.
 */
class GuardianProjectScenarioTest : ScenarioTestBase() {

    init {
        context("Guardian Project") {

            test("draws a card when the entering creature shares a name with nothing you control or in your graveyard") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Guardian Project")
                    .withLandsOnBattlefield(1, "Forest", 2)
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val libraryBefore = game.librarySize(1)

                game.castSpell(1, "Grizzly Bears").error shouldBe null
                if (game.getPendingDecision() is SelectManaSourcesDecision) {
                    game.submitManaSourcesAutoPay()
                }
                game.resolveStack()

                withClue("A uniquely-named creature entering draws a card") {
                    game.librarySize(1) shouldBe (libraryBefore - 1)
                    game.findPermanent("Grizzly Bears") shouldNotBe null
                }
            }

            test("does not draw when another creature you control already has that name") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Guardian Project")
                    .withCardOnBattlefield(1, "Grizzly Bears", summoningSickness = false)
                    .withLandsOnBattlefield(1, "Forest", 2)
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val libraryBefore = game.librarySize(1)

                game.castSpell(1, "Grizzly Bears").error shouldBe null
                if (game.getPendingDecision() is SelectManaSourcesDecision) {
                    game.submitManaSourcesAutoPay()
                }
                game.resolveStack()

                withClue("A second same-named creature entering doesn't draw: it shares a name with the first") {
                    game.librarySize(1) shouldBe libraryBefore
                    game.findPermanents("Grizzly Bears").size shouldBe 2
                }
            }

            test("does not draw when a creature card with that name is in your graveyard") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Guardian Project")
                    .withCardInGraveyard(1, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Forest", 2)
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val libraryBefore = game.librarySize(1)

                game.castSpell(1, "Grizzly Bears").error shouldBe null
                if (game.getPendingDecision() is SelectManaSourcesDecision) {
                    game.submitManaSourcesAutoPay()
                }
                game.resolveStack()

                withClue("A same-named creature card in the graveyard also stops the draw") {
                    game.librarySize(1) shouldBe libraryBefore
                    game.graveyardSize(1) shouldBe 1
                }
            }

            test("draws when the entering creature's name doesn't match a differently-named creature or graveyard card") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Guardian Project")
                    .withCardOnBattlefield(1, "Hill Giant", summoningSickness = false)
                    .withCardInGraveyard(1, "Elvish Mystic")
                    .withLandsOnBattlefield(1, "Forest", 2)
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val libraryBefore = game.librarySize(1)

                game.castSpell(1, "Grizzly Bears").error shouldBe null
                if (game.getPendingDecision() is SelectManaSourcesDecision) {
                    game.submitManaSourcesAutoPay()
                }
                game.resolveStack()

                withClue("Different-named board/graveyard presence doesn't block the draw") {
                    game.librarySize(1) shouldBe (libraryBefore - 1)
                }
            }
        }
    }
}
