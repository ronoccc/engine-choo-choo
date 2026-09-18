package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.SpellCopiedEvent
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.engine.state.components.stack.SpellOnStackComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * End-to-end coverage for the Demonstrate keyword (CR 702.144) through the real cast/trigger
 * flow — [com.wingedsheep.engine.handlers.actions.spell.CastSpellHandler]'s synthesized
 * reflexive trigger and [com.wingedsheep.sdk.scripting.effects.StormCopyEffect]'s new
 * `copyController` (unit-tested directly in [StormCopyControllerOverrideTest]) — via the
 * granted form, Silverquill Lecturer's `GrantKeywordToOwnSpells(Keyword.DEMONSTRATE, Creature)`.
 *
 * Silverquill Lecturer is used throughout because a creature spell has no targets, keeping the
 * decision sequence to the two "may copy?" yes/nos alone (no retargeting prompts) — the cleanest
 * way to prove the trigger wiring, the LIFO push order, and the permanent-spell-copy-becomes-
 * token path (CR 706.9) all at once. All games are 2-player, so the opponent choice is forced
 * (no ChooseOptionDecision) per CR 601.6a.
 */
class DemonstrateScenarioTest : FunSpec({

    fun setupWithLecturer(): Pair<GameTestDriver, EntityId> {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true)
        val active = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putCreatureOnBattlefield(active, "Silverquill Lecturer")
        return driver to active
    }

    fun bearsOnBattlefield(driver: GameTestDriver, controller: EntityId): List<EntityId> =
        driver.state.getBattlefield(controller).filter {
            driver.state.getEntity(it)?.get<CardComponent>()?.name == "Grizzly Bears"
        }

    test("declining to copy leaves a single Grizzly Bears") {
        val (driver, active) = setupWithLecturer()
        val bears = driver.putCardInHand(active, "Grizzly Bears")
        driver.giveMana(active, Color.GREEN, 2)

        driver.submit(CastSpell(playerId = active, cardId = bears))
        driver.bothPass() // let the reflexive Demonstrate trigger resolve off the stack
        val ask = driver.pendingDecision as? YesNoDecision
        ask.shouldNotBeNull()
        ask.playerId shouldBe active

        driver.submitYesNo(active, choice = false)
        while (driver.state.stack.isNotEmpty()) driver.bothPass()

        bearsOnBattlefield(driver, active).size shouldBe 1
    }

    test("caster copies alone: a second Grizzly Bears token under the caster's control") {
        val (driver, active) = setupWithLecturer()
        val bears = driver.putCardInHand(active, "Grizzly Bears")
        driver.giveMana(active, Color.GREEN, 2)

        driver.submit(CastSpell(playerId = active, cardId = bears))
        driver.bothPass() // let the reflexive Demonstrate trigger resolve off the stack
        driver.submitYesNo(active, choice = true)

        // 2-player: the sole opponent is forced, so the very next pause is THEIR yes/no.
        val opponentAsk = driver.pendingDecision as? YesNoDecision
        opponentAsk.shouldNotBeNull()
        driver.submitYesNo(opponentAsk.playerId, choice = false)

        while (driver.state.stack.isNotEmpty()) driver.bothPass()

        val mine = bearsOnBattlefield(driver, active)
        mine.size shouldBe 2
        mine.count { driver.state.getEntity(it)?.has<TokenComponent>() == true } shouldBe 1
    }

    test("both copy: LIFO push order and per-copy controllers, each copy becomes a token") {
        val (driver, active) = setupWithLecturer()
        val opponent = driver.getOpponent(active)
        val bears = driver.putCardInHand(active, "Grizzly Bears")
        driver.giveMana(active, Color.GREEN, 2)
        val originalSpellId = bears

        driver.submit(CastSpell(playerId = active, cardId = bears))
        driver.bothPass() // let the reflexive Demonstrate trigger resolve off the stack
        val myAsk = driver.pendingDecision as? YesNoDecision
        myAsk.shouldNotBeNull()
        myAsk.playerId shouldBe active
        val afterMine = driver.submitYesNo(active, choice = true)
        val myCopyId = afterMine.events.filterIsInstance<SpellCopiedEvent>().single().copyEntityId

        val opponentAsk = driver.pendingDecision as? YesNoDecision
        opponentAsk.shouldNotBeNull()
        opponentAsk.playerId shouldBe opponent
        val afterTheirs = driver.submitYesNo(opponent, choice = true)
        val theirCopyId = afterTheirs.events.filterIsInstance<SpellCopiedEvent>().single().copyEntityId

        // CR 608.2b LIFO: opponent's copy is pushed above mine, which is pushed above the
        // original — no explicit reordering needed, this falls out of push order alone.
        val stack = driver.state.stack
        stack.takeLast(3) shouldBe listOf(originalSpellId, myCopyId, theirCopyId)

        // Controllers: the opponent's copy is controlled by the opponent (CR 702.144a "that
        // player copies the spell"), mine and the original stay with me. Still spells on the
        // stack, not yet permanents, so control lives on SpellOnStackComponent.casterId rather
        // than ControllerComponent (which StackResolver only attaches on entering the battlefield).
        driver.state.getEntity(myCopyId)?.get<SpellOnStackComponent>()?.casterId shouldBe active
        driver.state.getEntity(theirCopyId)?.get<SpellOnStackComponent>()?.casterId shouldBe opponent

        while (driver.state.stack.isNotEmpty()) driver.bothPass()

        // Three Grizzly Bears total: the original creature plus two token copies, split
        // across controllers as CR 702.144a dictates.
        bearsOnBattlefield(driver, active).size shouldBe 2
        bearsOnBattlefield(driver, opponent).size shouldBe 1
        bearsOnBattlefield(driver, opponent).all { driver.state.getEntity(it)?.has<TokenComponent>() == true } shouldBe true
    }
})
