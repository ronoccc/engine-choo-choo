package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Filters
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CompositeEffect
import com.wingedsheep.sdk.scripting.effects.ForEachInGroupEffect
import com.wingedsheep.sdk.scripting.effects.GrantKeywordEffect
import com.wingedsheep.sdk.scripting.effects.ModifyStatsEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * [com.wingedsheep.sdk.dsl.GroupPatterns.pumpAndGrantToAll] (the `DynamicAmount` overload) —
 * the "evaluate once, broadcast to a snapshotted group" primitive behind Overwhelming Stampede
 * and any future Overrun-style anthem whose X reads the whole battlefield rather than one entity.
 *
 * CR 611.2c: a continuous effect's value derived from game state (here, "the greatest power
 * among creatures you control") is determined once, as the spell resolves, and doesn't change
 * even as the effect it creates changes that same game state. These tests pin two failure modes:
 *
 *  1. A naive `ForEachInGroup(ModifyStats(aggregateAmount))` re-evaluates the aggregate on every
 *     iteration against a battlefield the earlier iterations have already pumped, so later group
 *     members get a bigger bonus than earlier ones (reproduced directly below with the *naive*
 *     composition, for contrast, before proving the real primitive avoids it).
 *  2. The group itself must still be a resolution-time snapshot — a creature that enters after
 *     the effect resolves gets nothing, frozen amount or not.
 */
class GroupPumpFrozenAmountTest : FunSpec({

    val projector = StateProjector()

    // Three creatures of different printed power, so "the greatest power among creatures you
    // control" is unambiguous and changes if a pump is mis-applied mid-loop.
    val onePower = card("Repro One Power") {
        manaCost = "{G}"
        colorIdentity = "G"
        typeLine = "Creature — Bear"
        power = 1
        toughness = 1
        oracleText = ""
    }
    val twoPower = card("Repro Two Power") {
        manaCost = "{G}"
        colorIdentity = "G"
        typeLine = "Creature — Bear"
        power = 2
        toughness = 2
        oracleText = ""
    }
    val threePower = card("Repro Three Power") {
        manaCost = "{G}"
        colorIdentity = "G"
        typeLine = "Creature — Bear"
        power = 3
        toughness = 3
        oracleText = ""
    }

    val maxPowerAmount = DynamicAmounts.battlefield(Player.You, GameObjectFilter.Creature).maxPower()

    // The primitive under test: freeze X once, broadcast to every member.
    val frozenStampede = card("Test Frozen Stampede") {
        manaCost = "{G}"
        colorIdentity = "G"
        typeLine = "Sorcery"
        oracleText = "Creatures you control get +X/+X and gain trample until end of turn, " +
            "where X is the greatest power among creatures you control."
        spell {
            effect = Patterns.Group.pumpAndGrantToAll(
                power = maxPowerAmount,
                toughness = maxPowerAmount,
                keyword = Keyword.TRAMPLE,
                filter = Filters.Group.creaturesYouControl
            )
        }
    }

    // The naive composition the task description warns about: the same DynamicAmount fed
    // straight into ForEachInGroup without freezing it first. Kept here only to prove the
    // failure mode is real and that the primitive above actually avoids it.
    val naiveStampede = card("Test Naive Stampede") {
        manaCost = "{G}"
        colorIdentity = "G"
        typeLine = "Sorcery"
        oracleText = "Naive (buggy) version for contrast: does not freeze X."
        spell {
            effect = ForEachInGroupEffect(
                filter = GroupFilter(GameObjectFilter.Creature.youControl()),
                effect = CompositeEffect(
                    listOf(
                        ModifyStatsEffect(maxPowerAmount, maxPowerAmount, EffectTarget.Self),
                        GrantKeywordEffect(Keyword.TRAMPLE.name, EffectTarget.Self)
                    )
                )
            )
        }
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(onePower, twoPower, threePower, frozenStampede, naiveStampede))
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return driver
    }

    test("naive ForEachInGroup(ModifyStats(aggregateAmount)) snowballs (documents the bug this primitive fixes)") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val small = driver.putCreatureOnBattlefield(me, "Repro One Power")
        val mid = driver.putCreatureOnBattlefield(me, "Repro Two Power")
        val big = driver.putCreatureOnBattlefield(me, "Repro Three Power")

        driver.giveMana(me, Color.GREEN, 1)
        val spell = driver.putCardInHand(me, "Test Naive Stampede")
        driver.castSpell(me, spell)
        driver.bothPass()

        val smallPower = projector.getProjectedPower(driver.state, small)
        val midPower = projector.getProjectedPower(driver.state, mid)
        val bigPower = projector.getProjectedPower(driver.state, big)

        // CR-faithful would be a uniform +3/+3 (4, 5, 6). The naive composition instead lets
        // "greatest power" climb as each creature in the loop gets pumped, so later creatures
        // in iteration order end up bigger than earlier ones.
        (smallPower == 4 && midPower == 5 && bigPower == 6) shouldBe false
    }

    test("frozen pumpAndGrantToAll gives every creature the SAME bonus, not an escalating one") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val small = driver.putCreatureOnBattlefield(me, "Repro One Power")
        val mid = driver.putCreatureOnBattlefield(me, "Repro Two Power")
        val big = driver.putCreatureOnBattlefield(me, "Repro Three Power")

        driver.giveMana(me, Color.GREEN, 1)
        val spell = driver.putCardInHand(me, "Test Frozen Stampede")
        driver.castSpell(me, spell)
        driver.bothPass()

        // X = 3 (the greatest power among creatures you control, checked once at resolution),
        // applied uniformly: 1->4, 2->5, 3->6.
        projector.getProjectedPower(driver.state, small) shouldBe 4
        projector.getProjectedToughness(driver.state, small) shouldBe 4
        projector.getProjectedPower(driver.state, mid) shouldBe 5
        projector.getProjectedToughness(driver.state, mid) shouldBe 5
        projector.getProjectedPower(driver.state, big) shouldBe 6
        projector.getProjectedToughness(driver.state, big) shouldBe 6

        projector.hasProjectedKeyword(driver.state, small, Keyword.TRAMPLE) shouldBe true
        projector.hasProjectedKeyword(driver.state, mid, Keyword.TRAMPLE) shouldBe true
        projector.hasProjectedKeyword(driver.state, big, Keyword.TRAMPLE) shouldBe true
    }

    test("a creature entering after resolution is unaffected (group is snapshotted, not just the amount)") {
        val driver = createDriver()
        val me = driver.activePlayer!!

        val small = driver.putCreatureOnBattlefield(me, "Repro One Power")
        val big = driver.putCreatureOnBattlefield(me, "Repro Three Power")

        driver.giveMana(me, Color.GREEN, 1)
        val spell = driver.putCardInHand(me, "Test Frozen Stampede")
        driver.castSpell(me, spell)
        driver.bothPass()

        // X = 3 at resolution; a creature that enters afterward missed the snapshot entirely.
        val late = driver.putCreatureOnBattlefield(me, "Repro Two Power")

        projector.getProjectedPower(driver.state, small) shouldBe 4
        projector.getProjectedPower(driver.state, big) shouldBe 6
        projector.getProjectedPower(driver.state, late) shouldBe 2
        projector.hasProjectedKeyword(driver.state, late, Keyword.TRAMPLE) shouldBe false
    }

    test("opponent's creatures are untouched") {
        val driver = createDriver()
        val me = driver.activePlayer!!
        val opponent = driver.getOpponent(me)

        driver.putCreatureOnBattlefield(me, "Repro Three Power")
        val theirs = driver.putCreatureOnBattlefield(opponent, "Repro Three Power")

        driver.giveMana(me, Color.GREEN, 1)
        val spell = driver.putCardInHand(me, "Test Frozen Stampede")
        driver.castSpell(me, spell)
        driver.bothPass()

        projector.getProjectedPower(driver.state, theirs) shouldBe 3
        projector.hasProjectedKeyword(driver.state, theirs, Keyword.TRAMPLE) shouldBe false
    }
})
