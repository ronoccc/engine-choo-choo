package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Scenario test for Jinnie Fay, Jetmir's Second (SNC #195, {R/G}{G}{G/W}, Legendary Creature —
 * Elf Druid, 3/3).
 *
 *   If you would create one or more tokens, you may instead create that many 2/2 green Cat
 *   creature tokens with haste or that many 3/1 green Dog creature tokens with vigilance.
 *
 * Uses Centaur Glade ({2}{G}{G}: Create a 3/3 green Centaur creature token.) as the token-
 * creating effect Jinnie Fay's replacement intercepts — a plain [com.wingedsheep.sdk.scripting.effects.CreateTokenEffect]
 * with no riders of its own, so the substitute tokens' characteristics are cleanly attributable
 * to the chosen [com.wingedsheep.sdk.scripting.AlternateTokenTemplate].
 */
class JinnieFayJetmirsSecondScenarioTest : ScenarioTestBase() {

    private fun buildGame() = scenario()
        .withPlayers("Player1", "Player2")
        .withCardOnBattlefield(1, "Jinnie Fay, Jetmir's Second")
        .withCardOnBattlefield(1, "Centaur Glade")
        .withLandsOnBattlefield(1, "Forest", 10)
        .withActivePlayer(1)
        .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
        .build()

    private fun activateCentaurGlade(game: TestGame) {
        val glade = game.findPermanent("Centaur Glade")!!
        val ability = cardRegistry.getCard("Centaur Glade")!!.script.activatedAbilities.first()
        val result = game.execute(
            ActivateAbility(playerId = game.player1Id, sourceId = glade, abilityId = ability.id)
        )
        withClue("Activating Centaur Glade should succeed: ${result.error}") {
            result.error shouldBe null
        }
        game.resolveStack()
    }

    init {
        context("Jinnie Fay, Jetmir's Second") {

            test("choosing the Cat template replaces the Centaur token with a 2/2 haste Cat") {
                val game = buildGame()
                activateCentaurGlade(game)

                val decision = game.getPendingDecision()
                withClue("Token creation should pause for a choice; got $decision") {
                    decision.shouldBeInstanceOf<ChooseOptionDecision>()
                }
                decision as ChooseOptionDecision
                withClue("Decline, Cat, or Dog") { decision.options.size shouldBe 3 }

                game.submitDecision(OptionChosenResponse(decision.id, 1))
                game.resolveStack()

                withClue("No Centaur token should be created when the Cat template is chosen") {
                    game.findPermanents("Centaur Token").size shouldBe 0
                }
                val cats = game.findPermanents("Cat Token")
                cats.size shouldBe 1
                val cat = cats.first()
                val projected = game.state.projectedState
                withClue("Cat template: 2/2 with haste") {
                    projected.getPower(cat) shouldBe 2
                    projected.getToughness(cat) shouldBe 2
                    projected.hasKeyword(cat, Keyword.HASTE) shouldBe true
                    projected.hasKeyword(cat, Keyword.VIGILANCE) shouldBe false
                }
            }

            test("choosing the Dog template replaces the Centaur token with a 3/1 vigilance Dog") {
                val game = buildGame()
                activateCentaurGlade(game)

                val decision = game.getPendingDecision() as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(decision.id, 2))
                game.resolveStack()

                game.findPermanents("Centaur Token").size shouldBe 0
                val dogs = game.findPermanents("Dog Token")
                dogs.size shouldBe 1
                val dog = dogs.first()
                val projected = game.state.projectedState
                withClue("Dog template: 3/1 with vigilance") {
                    projected.getPower(dog) shouldBe 3
                    projected.getToughness(dog) shouldBe 1
                    projected.hasKeyword(dog, Keyword.VIGILANCE) shouldBe true
                    projected.hasKeyword(dog, Keyword.HASTE) shouldBe false
                }
            }

            test("declining creates the original Centaur token unaffected") {
                val game = buildGame()
                activateCentaurGlade(game)

                val decision = game.getPendingDecision() as ChooseOptionDecision
                decision.options[0] shouldBe "Create the original token"
                game.submitDecision(OptionChosenResponse(decision.id, 0))
                game.resolveStack()

                game.findPermanents("Cat Token").size shouldBe 0
                game.findPermanents("Dog Token").size shouldBe 0
                withClue("Declining leaves the plain 3/3 green Centaur token") {
                    val centaurs = game.findPermanents("Centaur Token")
                    centaurs.size shouldBe 1
                    game.state.projectedState.getPower(centaurs.first()) shouldBe 3
                    game.state.projectedState.getToughness(centaurs.first()) shouldBe 3
                }
            }

            test("the choice is per token-creation event, not fixed for the rest of the turn") {
                // Unlike Mirrormind Crown's "the first time each turn", Jinnie Fay's replacement
                // has no once-per-turn restriction: it offers the choice again on the very next
                // qualifying token-creation event, and the controller may answer differently.
                val game = buildGame()

                activateCentaurGlade(game)
                val firstDecision = game.getPendingDecision() as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(firstDecision.id, 1)) // Cat
                game.resolveStack()

                activateCentaurGlade(game)
                val secondDecision = game.getPendingDecision()
                withClue("The replacement is offered again for the second, separate token-creation event") {
                    secondDecision.shouldBeInstanceOf<ChooseOptionDecision>()
                }
                game.submitDecision(OptionChosenResponse((secondDecision as ChooseOptionDecision).id, 2)) // Dog
                game.resolveStack()

                withClue("One Cat from the first event, one Dog from the second, no Centaurs") {
                    game.findPermanents("Cat Token").size shouldBe 1
                    game.findPermanents("Dog Token").size shouldBe 1
                    game.findPermanents("Centaur Token").size shouldBe 0
                }
            }
        }
    }
}
