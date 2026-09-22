package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Ruin Grinder (C21 #57) — Artifact Creature — Construct, 7/4.
 *
 * "Menace
 *  When this creature dies, each player may discard their hand and draw seven cards.
 *  Mountaincycling {2}"
 *
 * The dies trigger is a per-player optional wheel ([com.wingedsheep.sdk.scripting.effects.ForEachPlayerEffect]
 * over `Player.Each`, each iteration wrapped in `MayEffect`) — the case that matters is one player
 * accepting and the other declining, so the hand/library swap must be per-player, not shared or
 * all-or-nothing. Mountaincycling is plain [KeywordAbility.typecycling] filtered to "Mountain".
 */
class RuinGrinderScenarioTest : ScenarioTestBase() {

    init {
        context("Ruin Grinder") {

            test("has menace and correct stats") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Ruin Grinder")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val grinder = game.findPermanent("Ruin Grinder")!!
                val card = game.state.getEntity(grinder)?.get<CardComponent>()
                withClue("7/4 with Menace") {
                    card?.baseStats?.basePower shouldBe 7
                    card?.baseStats?.baseToughness shouldBe 4
                    card?.baseKeywords?.contains(com.wingedsheep.sdk.core.Keyword.MENACE) shouldBe true
                }
            }

            test("dying lets each player independently choose to wheel their hand") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Ruin Grinder")
                    .withCardInHand(1, "Stoke the Flames")
                    .withLandsOnBattlefield(1, "Mountain", 4)
                    // Controller's hand before the wheel (excludes the burn spell about to be cast).
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInHand(1, "Craw Wurm")
                    // Opponent's hand, which should be untouched when they decline.
                    .withCardInHand(2, "Grizzly Bears")
                    .withCardInLibrary(1, "Forest")
                    .withCardInLibrary(1, "Forest")
                    .withCardInLibrary(1, "Forest")
                    .withCardInLibrary(1, "Forest")
                    .withCardInLibrary(1, "Forest")
                    .withCardInLibrary(1, "Forest")
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val grinder = game.findPermanent("Ruin Grinder")!!
                game.castSpell(1, "Stoke the Flames", targetId = grinder).error shouldBe null
                game.resolveStack()

                withClue("4 damage from Stoke the Flames kills the 7/4 Ruin Grinder") {
                    game.findPermanent("Ruin Grinder") shouldBe null
                    game.isInGraveyard(1, "Ruin Grinder") shouldBe true
                }

                // Two sequential MayEffect prompts fire in APNAP order: the controller (active
                // player) first, then the opponent.
                var guard = 0
                var answeredController = false
                var answeredOpponent = false
                while (game.hasPendingDecision() && guard++ < 10) {
                    val decision = game.getPendingDecision()
                    if (decision is YesNoDecision) {
                        if (!answeredController) {
                            game.answerYesNo(true) // controller: wheel their hand
                            answeredController = true
                        } else if (!answeredOpponent) {
                            game.answerYesNo(false) // opponent: decline
                            answeredOpponent = true
                        } else {
                            game.answerYesNo(false)
                        }
                    } else {
                        game.skipSelection()
                    }
                    game.resolveStack()
                }

                withClue("the controller discarded their old hand and drew seven new cards") {
                    game.isInGraveyard(1, "Grizzly Bears") shouldBe true
                    game.isInGraveyard(1, "Craw Wurm") shouldBe true
                    game.handSize(1) shouldBe 7
                }
                withClue("the opponent declined, so their hand is untouched") {
                    game.isInHand(2, "Grizzly Bears") shouldBe true
                    game.handSize(2) shouldBe 1
                }
            }

            test("Mountaincycling {2} discards it and fetches only a Mountain card") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Ruin Grinder")
                    .withLandsOnBattlefield(1, "Plains", 2)
                    .withCardInLibrary(1, "Mountain")
                    // Not a Mountain card — must not be offered.
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                game.typecycleCard(1, "Ruin Grinder").error shouldBe null

                withClue("Mountaincycling discards the card as a cost") {
                    game.isInGraveyard(1, "Ruin Grinder") shouldBe true
                    game.isInHand(1, "Ruin Grinder") shouldBe false
                }

                val decision = game.getPendingDecision()
                withClue("typecycling raises a library search") {
                    (decision is SelectCardsDecision) shouldBe true
                }
                val options = (decision as SelectCardsDecision).options
                withClue("only Mountain cards are offered") {
                    options.map { game.state.getEntity(it)?.get<CardComponent>()?.name }
                        .shouldContainExactlyInAnyOrder("Mountain")
                }
                game.selectCards(listOf(options.single())).error shouldBe null

                withClue("the Mountain went to hand, not the battlefield") {
                    game.isInHand(1, "Mountain") shouldBe true
                    game.isOnBattlefield("Mountain") shouldBe false
                }
            }

            test("Mountaincycling is unaffordable on one land") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Ruin Grinder")
                    .withLandsOnBattlefield(1, "Plains", 1)
                    .withCardInLibrary(1, "Mountain")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                withClue("Mountaincycling {2} needs two mana") {
                    game.typecycleCard(1, "Ruin Grinder").error shouldNotBe null
                }
                withClue("the card stayed in hand") {
                    game.isInHand(1, "Ruin Grinder") shouldBe true
                }
            }
        }
    }
}
