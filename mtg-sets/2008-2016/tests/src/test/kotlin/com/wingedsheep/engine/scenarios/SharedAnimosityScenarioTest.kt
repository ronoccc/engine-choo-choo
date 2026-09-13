package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Shared Animosity (MOR) — {2}{R} Enchantment.
 *
 * "Whenever a creature you control attacks, it gets +1/+0 until end of turn for each other
 * attacking creature that shares a creature type with it."
 *
 * Per the 2008-04-01 ruling, this counts *creatures*, not creature *types* — a creature with
 * several shared types with several different attackers still counts each qualifying attacker
 * exactly once, never once per shared type. Each attacking creature you control triggers this
 * separately, and the amount is a fresh [com.wingedsheep.sdk.scripting.values.DynamicAmount
 * .AggregateBattlefield] evaluation at that trigger's own resolution — not a value snapshotted
 * when the trigger went on the stack — so a creature that leaves combat between two of these
 * triggers resolving changes the bonus for whichever haven't resolved yet.
 *
 * Test creatures are plain vanillas chosen for their creature types: Willow Elf (Elf), Elvish
 * Warrior (Elf Warrior — the dual-type creature), Savaen Elves (Elf), Erg Raiders (Human
 * Warrior), and Grizzly Bears (Bear, sharing nothing with the others).
 */
class SharedAnimosityScenarioTest : ScenarioTestBase() {

    init {
        context("Shared Animosity") {

            test("a lone attacker gets no bonus") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Shared Animosity")
                    .withCardOnBattlefield(1, "Willow Elf") // 1/1 Elf
                    .withActivePlayer(1)
                    .inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    .build()

                game.declareAttackers(mapOf("Willow Elf" to 2)).error shouldBe null
                game.resolveStack()

                val elf = game.getClientState(1).cards.values.find { it.name == "Willow Elf" }
                withClue("no other attacker shares a type with it, so no bonus applies") {
                    elf!!.power shouldBe 1
                    elf.toughness shouldBe 1
                }
            }

            test("two attackers sharing a creature type each get +1/+0") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Shared Animosity")
                    .withCardOnBattlefield(1, "Willow Elf") // 1/1 Elf
                    .withCardOnBattlefield(1, "Elvish Warrior") // 2/3 Elf Warrior
                    .withActivePlayer(1)
                    .inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    .build()

                game.declareAttackers(mapOf("Willow Elf" to 2, "Elvish Warrior" to 2)).error shouldBe null
                game.resolveStack()

                val clientState = game.getClientState(1)
                val elf = clientState.cards.values.find { it.name == "Willow Elf" }
                val warrior = clientState.cards.values.find { it.name == "Elvish Warrior" }
                withClue("each is the other's one qualifying attacker") {
                    elf!!.power shouldBe 2 // 1 base + 1
                    elf.toughness shouldBe 1
                    warrior!!.power shouldBe 3 // 2 base + 1
                    warrior.toughness shouldBe 3
                }
            }

            test("three attackers sharing a creature type each get +2/+0") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Shared Animosity")
                    .withCardOnBattlefield(1, "Willow Elf") // 1/1 Elf
                    .withCardOnBattlefield(1, "Elvish Warrior") // 2/3 Elf Warrior
                    .withCardOnBattlefield(1, "Savaen Elves") // 1/1 Elf
                    .withActivePlayer(1)
                    .inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    .build()

                game.declareAttackers(
                    mapOf("Willow Elf" to 2, "Elvish Warrior" to 2, "Savaen Elves" to 2)
                ).error shouldBe null
                game.resolveStack()

                val clientState = game.getClientState(1)
                val elf = clientState.cards.values.find { it.name == "Willow Elf" }
                val warrior = clientState.cards.values.find { it.name == "Elvish Warrior" }
                val savaen = clientState.cards.values.find { it.name == "Savaen Elves" }
                withClue("each of the three other Elves counts two other qualifying attackers") {
                    elf!!.power shouldBe 3 // 1 base + 2
                    warrior!!.power shouldBe 4 // 2 base + 2
                    savaen!!.power shouldBe 3 // 1 base + 2
                }
            }

            test("a creature sharing multiple types counts each qualifying attacker once, unrelated attackers get nothing") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Shared Animosity")
                    .withCardOnBattlefield(1, "Elvish Warrior") // 2/3 Elf Warrior — shares with both below
                    .withCardOnBattlefield(1, "Willow Elf") // 1/1 Elf — shares only Elf
                    .withCardOnBattlefield(1, "Erg Raiders") // 2/3 Human Warrior — shares only Warrior
                    .withCardOnBattlefield(1, "Grizzly Bears") // 2/2 Bear — shares nothing
                    .withActivePlayer(1)
                    .inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    .build()

                game.declareAttackers(
                    mapOf(
                        "Elvish Warrior" to 2,
                        "Willow Elf" to 2,
                        "Erg Raiders" to 2,
                        "Grizzly Bears" to 2,
                    )
                ).error shouldBe null
                game.resolveStack()

                val clientState = game.getClientState(1)
                val warrior = clientState.cards.values.find { it.name == "Elvish Warrior" }
                val elf = clientState.cards.values.find { it.name == "Willow Elf" }
                val raiders = clientState.cards.values.find { it.name == "Erg Raiders" }
                val bears = clientState.cards.values.find { it.name == "Grizzly Bears" }
                withClue("Elvish Warrior's two types each pick up one other attacker, not a double count") {
                    warrior!!.power shouldBe 4 // 2 base + 2 (Willow Elf via Elf, Erg Raiders via Warrior)
                }
                withClue("Willow Elf only shares Elf with Elvish Warrior") {
                    elf!!.power shouldBe 2 // 1 base + 1
                }
                withClue("Erg Raiders only shares Warrior with Elvish Warrior") {
                    raiders!!.power shouldBe 3 // 2 base + 1
                }
                withClue("Grizzly Bears shares a creature type with nobody") {
                    bears!!.power shouldBe 2 // 2 base + 0
                }
            }

            test("an attacker destroyed before triggers resolve lowers the bonus for the survivors") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Shared Animosity")
                    .withCardOnBattlefield(1, "Willow Elf") // 1/1 Elf
                    .withCardOnBattlefield(1, "Elvish Warrior") // 2/3 Elf Warrior
                    .withCardOnBattlefield(1, "Savaen Elves") // 1/1 Elf
                    .withCardInHand(1, "Terminate")
                    .withLandsOnBattlefield(1, "Swamp", 1)
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withActivePlayer(1)
                    .inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS)
                    .build()

                // All three Elves attack — three separate "attacks" triggers go on the stack, each
                // computed against a 3-attacker board if left alone.
                game.declareAttackers(
                    mapOf("Willow Elf" to 2, "Elvish Warrior" to 2, "Savaen Elves" to 2)
                ).error shouldBe null

                // Before any of those triggers resolve, destroy Savaen Elves in response.
                game.castSpell(1, "Terminate", game.findPermanent("Savaen Elves")!!).error shouldBe null
                game.resolveStack()

                game.isInGraveyard(1, "Savaen Elves") shouldBe true

                val clientState = game.getClientState(1)
                val elf = clientState.cards.values.find { it.name == "Willow Elf" }
                val warrior = clientState.cards.values.find { it.name == "Elvish Warrior" }
                withClue("with Savaen Elves gone before resolution, only one other Elf remains for each") {
                    elf!!.power shouldBe 2 // 1 base + 1, not +2
                    warrior!!.power shouldBe 3 // 2 base + 1, not +2
                }
            }
        }
    }
}
