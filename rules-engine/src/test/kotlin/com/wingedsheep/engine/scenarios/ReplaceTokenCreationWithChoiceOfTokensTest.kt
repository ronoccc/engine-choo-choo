package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.handlers.effects.token.TokenCreationReplacementHelper
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.scripting.AlternateTokenTemplate
import com.wingedsheep.sdk.scripting.ReplaceTokenCreationWithChoiceOfTokens
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Engine-level coverage for [ReplaceTokenCreationWithChoiceOfTokens] — the general "choose one
 * of several named alternate token templates" replacement effect built for Jinnie Fay, Jetmir's
 * Second: "If you would create one or more tokens, you may instead create that many 2/2 green
 * Cat creature tokens with haste or that many 3/1 green Dog creature tokens with vigilance."
 *
 * A test-only card exercises the mechanic in isolation from the real card, mirroring the
 * pattern in [DrawAmountReplacementChoiceTest]. Its `{0}:` ability creates two **tapped** 1/1
 * white Soldier tokens — tapped is deliberately part of the *original* effect so every test can
 * assert the printed ruling's "anything else specified in the effect creating the tokens (such
 * as tapped, ...) still applies" against something observable.
 */
class ReplaceTokenCreationWithChoiceOfTokensTest : ScenarioTestBase() {

    private val catTemplate = AlternateTokenTemplate(
        power = 2, toughness = 2, colors = setOf(Color.GREEN),
        creatureTypes = setOf("Cat"), keywords = setOf(Keyword.HASTE)
    )
    private val dogTemplate = AlternateTokenTemplate(
        power = 3, toughness = 1, colors = setOf(Color.GREEN),
        creatureTypes = setOf("Dog"), keywords = setOf(Keyword.VIGILANCE)
    )

    private val testChooser = card("Test Token Chooser") {
        manaCost = "{1}{G}"
        colorIdentity = "G"
        typeLine = "Creature — Elf"
        power = 1
        toughness = 1
        oracleText = "If you would create one or more tokens, you may instead create that many " +
            "2/2 green Cat creature tokens with haste or that many 3/1 green Dog creature " +
            "tokens with vigilance.\n{0}: Create two tapped 1/1 white Soldier creature tokens."

        replacementEffect(
            ReplaceTokenCreationWithChoiceOfTokens(templates = listOf(catTemplate, dogTemplate))
        )

        activatedAbility {
            cost = Costs.Mana("{0}")
            effect = CreateTokenEffect(
                count = DynamicAmount.Fixed(2),
                power = 1,
                toughness = 1,
                colors = setOf(Color.WHITE),
                creatureTypes = setOf("Soldier"),
                tapped = true,
            )
            description = "Create two tapped 1/1 white Soldier creature tokens."
        }
    }

    init {
        cardRegistry.register(testChooser)

        fun buildGame() = scenario()
            .withPlayers("Player1", "Player2")
            .withCardOnBattlefield(1, "Test Token Chooser")
            .withLandsOnBattlefield(1, "Forest", 2)
            .withActivePlayer(1)
            .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
            .build()

        fun activateCreateTokens(game: TestGame) {
            val source = game.findPermanent("Test Token Chooser")!!
            val ability = cardRegistry.getCard("Test Token Chooser")!!.script.activatedAbilities.first()
            val result = game.execute(
                ActivateAbility(playerId = game.player1Id, sourceId = source, abilityId = ability.id)
            )
            withClue("Activating the token-creating ability should succeed: ${result.error}") {
                result.error shouldBe null
            }
            game.resolveStack()
        }

        context("Jinnie Fay's replacement — choice among named token templates") {

            test("choosing the Cat template substitutes it uniformly, preserving the tapped rider") {
                val game = buildGame()
                activateCreateTokens(game)

                val decision = game.getPendingDecision()
                withClue("Token creation should pause for a ChooseOptionDecision; got $decision") {
                    decision.shouldBeInstanceOf<ChooseOptionDecision>()
                }
                decision as ChooseOptionDecision
                withClue("Options: decline, Cat, Dog") { decision.options.size shouldBe 3 }

                game.submitDecision(OptionChosenResponse(decision.id, 1))
                game.resolveStack()

                withClue("No Soldier tokens — the original creation was replaced") {
                    game.findPermanents("Soldier Token").size shouldBe 0
                }
                val cats = game.findPermanents("Cat Token")
                withClue("Both original tokens become Cat tokens (count preserved uniformly)") {
                    cats.size shouldBe 2
                }
                for (cat in cats) {
                    val projected = game.state.projectedState
                    withClue("Cat template P/T") {
                        projected.getPower(cat) shouldBe 2
                        projected.getToughness(cat) shouldBe 2
                    }
                    withClue("Cat template grants haste, not the Soldier's (none) or Dog's vigilance") {
                        projected.hasKeyword(cat, Keyword.HASTE) shouldBe true
                        projected.hasKeyword(cat, Keyword.VIGILANCE) shouldBe false
                    }
                    withClue("Colors come only from the chosen template") {
                        game.state.getEntity(cat)?.get<CardComponent>()?.colors shouldBe setOf(Color.GREEN)
                    }
                    withClue("The 'tapped' rider from the original CreateTokenEffect still applies") {
                        game.state.getEntity(cat)?.has<TappedComponent>() shouldBe true
                    }
                }
            }

            test("choosing the Dog template selects the other alternative") {
                val game = buildGame()
                activateCreateTokens(game)

                val decision = game.getPendingDecision() as ChooseOptionDecision
                game.submitDecision(OptionChosenResponse(decision.id, 2))
                game.resolveStack()

                withClue("No Cat or Soldier tokens") {
                    game.findPermanents("Cat Token").size shouldBe 0
                    game.findPermanents("Soldier Token").size shouldBe 0
                }
                val dogs = game.findPermanents("Dog Token")
                dogs.size shouldBe 2
                for (dog in dogs) {
                    val projected = game.state.projectedState
                    projected.getPower(dog) shouldBe 3
                    projected.getToughness(dog) shouldBe 1
                    projected.hasKeyword(dog, Keyword.VIGILANCE) shouldBe true
                    projected.hasKeyword(dog, Keyword.HASTE) shouldBe false
                    withClue("The tapped rider still applies to the Dog alternative too") {
                        game.state.getEntity(dog)?.has<TappedComponent>() shouldBe true
                    }
                }
            }

            test("declining leaves the original tokens completely unaffected") {
                val game = buildGame()
                activateCreateTokens(game)

                val decision = game.getPendingDecision() as ChooseOptionDecision
                withClue("Option 0 is always 'create the original tokens'") {
                    decision.options[0] shouldBe "Create the original tokens"
                }
                game.submitDecision(OptionChosenResponse(decision.id, 0))
                game.resolveStack()

                withClue("No Cat or Dog tokens were created") {
                    game.findPermanents("Cat Token").size shouldBe 0
                    game.findPermanents("Dog Token").size shouldBe 0
                }
                val soldiers = game.findPermanents("Soldier Token")
                withClue("The original two tapped Soldier tokens are created unchanged") {
                    soldiers.size shouldBe 2
                    soldiers.all { game.state.getEntity(it)?.has<TappedComponent>() == true } shouldBe true
                }
            }

            test("the source leaving before the choice resolves falls back to the original tokens") {
                val game = buildGame()
                activateCreateTokens(game)

                val decision = game.getPendingDecision() as ChooseOptionDecision
                val source = game.findPermanent("Test Token Chooser")!!

                // Simulate the (otherwise unreachable in normal play — no stack, no priority
                // window between offering and answering this decision) case where the source has
                // since vanished: the resumer must not crash, and must not honor a template that
                // no permanent's ability is still around to grant.
                game.state = game.state.removeEntity(source)

                val answer = game.submitDecision(OptionChosenResponse(decision.id, 1))
                withClue("Resuming after the source vanished should not error: ${answer.error}") {
                    answer.error shouldBe null
                }
                game.resolveStack()

                withClue("No template can be honored once its source is gone — falls back to the original tokens") {
                    game.findPermanents("Cat Token").size shouldBe 0
                    game.findPermanents("Soldier Token").size shouldBe 2
                }
            }
        }

        context("TokenCreationReplacementHelper.resolveChoiceContinuationOption") {
            val templates = listOf(catTemplate, dogTemplate)

            test("index 0 means decline when optional") {
                TokenCreationReplacementHelper.resolveChoiceContinuationOption(
                    templates, optional = true, chosenIndex = 0
                ) shouldBe null
            }

            test("subsequent indices map onto the templates positionally") {
                TokenCreationReplacementHelper.resolveChoiceContinuationOption(
                    templates, optional = true, chosenIndex = 1
                ) shouldBe catTemplate
                TokenCreationReplacementHelper.resolveChoiceContinuationOption(
                    templates, optional = true, chosenIndex = 2
                ) shouldBe dogTemplate
            }

            test("when not optional, index 0 selects the first template directly") {
                TokenCreationReplacementHelper.resolveChoiceContinuationOption(
                    templates, optional = false, chosenIndex = 0
                ) shouldBe catTemplate
            }

            test("an out-of-range index is rejected rather than silently clamped") {
                shouldThrow<IllegalArgumentException> {
                    TokenCreationReplacementHelper.resolveChoiceContinuationOption(
                        templates, optional = true, chosenIndex = 3
                    )
                }
            }
        }
    }
}
