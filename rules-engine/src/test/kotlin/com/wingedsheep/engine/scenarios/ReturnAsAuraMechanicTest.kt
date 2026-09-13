package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.ActivatedAbility
import com.wingedsheep.sdk.scripting.CompositeStaticAbility
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantActivatedAbility
import com.wingedsheep.sdk.scripting.LoseAllAbilities
import com.wingedsheep.sdk.scripting.TransformPermanent
import com.wingedsheep.sdk.scripting.conditions.SourceReturnedAsAura
import com.wingedsheep.sdk.scripting.effects.MoveToZoneEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Scenario tests for the "dies, returns to the battlefield as a different card type" primitive
 * (Bronzehide Lion shape — Theros Beyond Death): "When this creature dies, return it to the
 * battlefield. It's an Aura enchantment with enchant creature you control and '...', and it
 * loses all other abilities."
 *
 * Built from three new/extended pieces exercised together here:
 *  - [Effects.PutOntoBattlefieldAttachedToChosen] with `becomesAuraOnAttach = true` — the
 *    creature is treated as an Aura for attachment-legality purposes even though its printed
 *    type line isn't Aura (CR 303.4f: the controller chooses a legal host as it enters; CR
 *    303.4g: no legal host means it stays in its current zone).
 *  - [SourceReturnedAsAura] gating a [CompositeStaticAbility] bundle ([TransformPermanent] +
 *    [LoseAllAbilities]) plus a separate [GrantActivatedAbility], mirroring the Enduring
 *    mechanic's marker-gated conditional-static shape.
 *  - `UnattachedAurasCheck` reading *projected* type/subtype (not the printed type line) and the
 *    `ReturnedAsAuraComponent` marker's host filter, so CR 704.5m sweeps the returned Aura the
 *    same way it would a printed one — including when its host later leaves.
 *
 * A synthetic "Test Returning Cat" stands in for Bronzehide Lion itself, which is implemented
 * separately (its own scenario test covers the real card's exact oracle text); this file proves
 * the primitive in isolation so a regression here is caught independent of that card.
 */
class ReturnAsAuraMechanicTest : FunSpec({

    val projector = StateProjector()

    // A creature that dies and returns as an Aura granting a different ability, mirroring
    // Bronzehide Lion's shape without depending on that card's own implementation.
    val testReturningCat = card("Test Returning Cat") {
        manaCost = "{G}{W}"
        typeLine = "Creature — Cat"
        power = 3
        toughness = 3
        oracleText = "{G}{W}: This creature gains indestructible until end of turn.\n" +
            "When this creature dies, return it to the battlefield. It's an Aura enchantment " +
            "with enchant creature you control and \"{G}{W}: Enchanted creature gains " +
            "indestructible until end of turn,\" and it loses all other abilities."

        activatedAbility {
            cost = Costs.Mana("{G}{W}")
            effect = Effects.GrantKeyword(Keyword.INDESTRUCTIBLE, EffectTarget.Self)
        }

        triggeredAbility {
            trigger = Triggers.Dies
            effect = Effects.PutOntoBattlefieldAttachedToChosen(
                target = EffectTarget.Self,
                hostFilter = GameObjectFilter.Creature.youControl(),
                becomesAuraOnAttach = true
            )
        }

        staticAbility {
            condition = SourceReturnedAsAura
            ability = CompositeStaticAbility(
                listOf(
                    TransformPermanent(
                        setCardTypes = setOf("ENCHANTMENT"),
                        setSubtypes = setOf("Aura"),
                        filter = GroupFilter.source()
                    ),
                    LoseAllAbilities(filter = GroupFilter.source())
                )
            )
        }

        staticAbility {
            condition = SourceReturnedAsAura
            ability = GrantActivatedAbility(
                ability = ActivatedAbility(
                    cost = Costs.Mana("{G}{W}"),
                    effect = Effects.GrantKeyword(Keyword.INDESTRUCTIBLE, EffectTarget.EnchantedCreature),
                    descriptionOverride = "{G}{W}: Enchanted creature gains indestructible until end of turn."
                ),
                filter = GroupFilter.source()
            )
        }
    }

    // Unconditional removal, so a test can kill any permanent regardless of P/T or protections.
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
        driver.registerCards(TestCards.all + listOf(testReturningCat, unmaking))
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return driver
    }

    test("dies with a legal creature you control: returns as an Aura attached to a chosen host, loses its printed ability, gains the new one") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val cat = driver.putCreatureOnBattlefield(me, "Test Returning Cat")
        val host = driver.putCreatureOnBattlefield(me, "Centaur Courser")
        projector.project(driver.state).isCreature(cat) shouldBe true

        val kill = driver.putCardInHand(me, "Test Unmaking")
        driver.giveMana(me, com.wingedsheep.sdk.core.Color.WHITE, 2)
        driver.castSpell(me, kill, listOf(cat)).isSuccess shouldBe true
        driver.bothPass() // resolve Unmaking -> Cat dies
        driver.bothPass() // resolve the dies trigger -> pauses for host choice

        driver.isPaused shouldBe true
        driver.pendingDecision.shouldBeInstanceOf<ChooseTargetsDecision>()
        driver.submitTargetSelection(me, listOf(host))
        driver.isPaused shouldBe false

        val returned = driver.findPermanent(me, "Test Returning Cat")
        returned.shouldNotBeNull()
        val projected = projector.project(driver.state)
        projected.isCreature(returned) shouldBe false
        projected.isAura(returned) shouldBe true
        driver.state.getEntity(returned)?.get<AttachedToComponent>()?.targetId shouldBe host

        // It lost its printed activated ability and gained exactly the new granted one.
        val ownActions = driver.legalActions(me).filter {
            it.action is ActivateAbility && (it.action as ActivateAbility).sourceId == returned
        }
        ownActions.size shouldBe 1

        // Activating the granted ability affects the enchanted creature, not the Aura itself.
        driver.giveMana(me, com.wingedsheep.sdk.core.Color.GREEN, 1)
        driver.giveMana(me, com.wingedsheep.sdk.core.Color.WHITE, 1)
        val grantedAction = (ownActions.first().action as ActivateAbility)
        driver.submit(grantedAction).isSuccess shouldBe true
        driver.bothPass() // resolve the granted ability off the stack
        projector.project(driver.state).hasKeyword(host, "INDESTRUCTIBLE") shouldBe true
    }

    test("dies with no legal creature to enchant: stays in its owner's graveyard (CR 303.4g)") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        // The Cat is the only creature the controller has — it can't enchant itself.
        val cat = driver.putCreatureOnBattlefield(me, "Test Returning Cat")

        val kill = driver.putCardInHand(me, "Test Unmaking")
        driver.giveMana(me, com.wingedsheep.sdk.core.Color.WHITE, 2)
        driver.castSpell(me, kill, listOf(cat)).isSuccess shouldBe true
        driver.bothPass() // resolve Unmaking -> Cat dies
        driver.bothPass() // resolve the dies trigger -> no legal host, no-op

        driver.isPaused shouldBe false
        driver.findPermanent(me, "Test Returning Cat").shouldBeNull()
        driver.getGraveyardCardNames(me).toSet() shouldBe setOf("Test Returning Cat", "Test Unmaking")
    }

    test("the enchanted creature later dies: the returned Aura is put into the graveyard (CR 704.5m)") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val cat = driver.putCreatureOnBattlefield(me, "Test Returning Cat")
        val host = driver.putCreatureOnBattlefield(me, "Centaur Courser")

        driver.giveMana(me, com.wingedsheep.sdk.core.Color.WHITE, 4)
        val kill1 = driver.putCardInHand(me, "Test Unmaking")
        driver.castSpell(me, kill1, listOf(cat)).isSuccess shouldBe true
        driver.bothPass()
        driver.bothPass()
        driver.submitTargetSelection(me, listOf(host))

        val returned = driver.findPermanent(me, "Test Returning Cat")
        returned.shouldNotBeNull()
        driver.state.getEntity(returned)?.get<AttachedToComponent>()?.targetId shouldBe host

        // Now kill the host. Its enchantment has nothing left to enchant, so 704.5m sends it to
        // the graveyard as a state-based action.
        val kill2 = driver.putCardInHand(me, "Test Unmaking")
        driver.castSpell(me, kill2, listOf(host)).isSuccess shouldBe true
        driver.bothPass()

        driver.findPermanent(me, "Test Returning Cat").shouldBeNull()
        driver.getGraveyardCardNames(me).toSet() shouldBe
            setOf("Test Returning Cat", "Centaur Courser", "Test Unmaking")
    }
})
