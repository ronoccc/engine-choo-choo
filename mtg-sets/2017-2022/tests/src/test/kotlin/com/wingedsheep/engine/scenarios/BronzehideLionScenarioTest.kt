package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.thb.cards.BronzehideLion
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.effects.MoveToZoneEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Bronzehide Lion ({G}{W}) — Theros Beyond Death, Creature — Cat, 3/3.
 *
 * "{G}{W}: This creature gains indestructible until end of turn.
 * When this creature dies, return it to the battlefield. It's an Aura enchantment with enchant
 * creature you control and '{G}{W}: Enchanted creature gains indestructible until end of turn,'
 * and it loses all other abilities."
 *
 * Composes the "returns as an Aura" primitive proven in isolation by
 * `ReturnAsAuraMechanicTest` (rules-engine). These tests are the card-level check: the exact
 * printed abilities line up, and the no-legal-host ruling holds for the real card.
 */
class BronzehideLionScenarioTest : FunSpec({

    val projector = StateProjector()

    // Unconditional removal, so a test can kill Bronzehide Lion regardless of available red mana.
    val unmaking = card("Test Unmaking") {
        manaCost = "{1}{W}"
        typeLine = "Instant"
        spell {
            val t = target("permanent", Targets.Permanent)
            effect = MoveToZoneEffect(t, Zone.GRAVEYARD, byDestruction = true)
        }
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(BronzehideLion, unmaking))
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return driver
    }

    test("dies with a legal creature you control: returns as an Aura enchanting a chosen creature") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val lion = driver.putCreatureOnBattlefield(me, "Bronzehide Lion")
        val host = driver.putCreatureOnBattlefield(me, "Centaur Courser")
        projector.project(driver.state).isCreature(lion) shouldBe true

        val kill = driver.putCardInHand(me, "Test Unmaking")
        driver.giveMana(me, Color.WHITE, 2)
        driver.castSpell(me, kill, listOf(lion)).isSuccess shouldBe true
        driver.bothPass() // resolve Unmaking -> Lion dies
        driver.bothPass() // resolve the dies trigger -> pauses for host choice

        driver.isPaused shouldBe true
        driver.pendingDecision.shouldBeInstanceOf<ChooseTargetsDecision>()
        driver.submitTargetSelection(me, listOf(host))
        driver.isPaused shouldBe false

        val returned = driver.findPermanent(me, "Bronzehide Lion")
        returned.shouldNotBeNull()
        val projected = projector.project(driver.state)
        projected.isCreature(returned) shouldBe false
        projected.isAura(returned) shouldBe true
        driver.state.getEntity(returned)?.get<AttachedToComponent>()?.targetId shouldBe host

        // It lost the printed "{G}{W}: This creature gains indestructible" ability and has
        // exactly the new granted one instead.
        val ownActions = driver.legalActions(me).filter {
            it.action is ActivateAbility && (it.action as ActivateAbility).sourceId == returned
        }
        ownActions.size shouldBe 1

        // Activating the granted ability affects the enchanted creature, not the Aura itself.
        driver.giveMana(me, Color.GREEN, 1)
        driver.giveMana(me, Color.WHITE, 1)
        driver.submit(ownActions.first().action as ActivateAbility).isSuccess shouldBe true
        driver.bothPass()
        projector.project(driver.state).hasKeyword(host, "INDESTRUCTIBLE") shouldBe true
    }

    test("dies with no legal creature to enchant: remains in its owner's graveyard (Scryfall ruling)") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        // Bronzehide Lion is the only creature the controller has — it can't enchant itself.
        val lion = driver.putCreatureOnBattlefield(me, "Bronzehide Lion")

        val kill = driver.putCardInHand(me, "Test Unmaking")
        driver.giveMana(me, Color.WHITE, 2)
        driver.castSpell(me, kill, listOf(lion)).isSuccess shouldBe true
        driver.bothPass() // resolve Unmaking -> Lion dies
        driver.bothPass() // resolve the dies trigger -> no legal host, no-op

        driver.isPaused shouldBe false
        driver.findPermanent(me, "Bronzehide Lion").shouldBeNull()
        driver.getGraveyardCardNames(me).toSet() shouldBe setOf("Bronzehide Lion", "Test Unmaking")
    }

    test("the original creature's activated ability still works before it dies") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val lion = driver.putCreatureOnBattlefield(me, "Bronzehide Lion")
        driver.giveMana(me, Color.GREEN, 1)
        driver.giveMana(me, Color.WHITE, 1)

        val action = driver.legalActions(me).firstOrNull {
            it.action is ActivateAbility && (it.action as ActivateAbility).sourceId == lion
        }
        action.shouldNotBeNull()
        driver.submit(action.action).isSuccess shouldBe true
        driver.bothPass()
        projector.project(driver.state).hasKeyword(lion, "INDESTRUCTIBLE") shouldBe true
    }
})
