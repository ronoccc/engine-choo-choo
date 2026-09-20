package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.GameConfig
import com.wingedsheep.engine.core.GameInitializer
import com.wingedsheep.engine.core.PlayerConfig
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.combat.AttackingComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * End-to-end coverage for the Myriad keyword (CR 702.116a) through the real attack-trigger flow —
 * [com.wingedsheep.engine.handlers.effects.token.MyriadTokenChooser] and
 * [com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect.myriadPerOpponent] — using
 * Conclave Evangelist (Ravnica: Clue Edition). Every test is a multiplayer pod ([setupPod]) since
 * Myriad is a genuine no-op in 1v1 (the ruling this session confirms).
 */
class MyriadScenarioTest : FunSpec({

    fun setupPod(playerCount: Int): Pair<GameTestDriver, List<EntityId>> {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        val deck = Deck.of("Plains" to 40)
        val init = GameInitializer(driver.cardRegistry).initializeGame(
            GameConfig(
                players = (1..playerCount).map { PlayerConfig("Player $it", deck, 20) },
                skipMulligans = true,
                startingPlayerIndex = 0
            )
        )
        driver.replaceState(init.state)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return driver to init.playerIds
    }

    fun evangelistCopies(driver: GameTestDriver, controller: EntityId): List<EntityId> =
        driver.state.getBattlefield(controller).filter {
            driver.state.getEntity(it)?.get<CardComponent>()?.name == "Conclave Evangelist"
        }

    /**
     * Pass priority (never auto-resolving anything) until either a decision appears or the stack
     * empties. [GameTestDriver.bothPass] only performs up to two passes, which is exactly enough to
     * resolve the top of the stack in a 1v1 game but not in a 3+ player pod, where every player must
     * pass in a row.
     */
    fun advanceUntilDecision(driver: GameTestDriver, maxPasses: Int = 20) {
        var passes = 0
        while (driver.pendingDecision == null && driver.state.stack.isNotEmpty() && passes++ < maxPasses) {
            driver.bothPass()
        }
    }

    test("1v1: attacking creates no tokens and asks no decisions") {
        val (driver, players) = setupPod(2)
        val me = players[0]
        val opponent = players[1]
        val evangelist = driver.putCreatureOnBattlefield(me, "Conclave Evangelist")
        driver.removeSummoningSickness(evangelist)
        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)

        driver.declareAttackers(me, listOf(evangelist), defendingPlayer = opponent)
        advanceUntilDecision(driver) // let the Attacks trigger resolve

        driver.pendingDecision.shouldBeNull()
        evangelistCopies(driver, me) shouldHaveSize 1 // just the original attacker
    }

    test("3-player pod: the non-defending opponent gets an independent decision; declining makes no token") {
        val (driver, players) = setupPod(3)
        val me = players[0]
        val defender = players[1]
        val evangelist = driver.putCreatureOnBattlefield(me, "Conclave Evangelist")
        driver.removeSummoningSickness(evangelist)
        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)

        driver.declareAttackers(me, listOf(evangelist), defendingPlayer = defender)
        advanceUntilDecision(driver)

        val ask = driver.pendingDecision as? YesNoDecision
        ask.shouldNotBeNull()
        ask.playerId shouldBe me

        driver.submitYesNo(me, choice = false)
        driver.pendingDecision.shouldBeNull()
        evangelistCopies(driver, me) shouldHaveSize 1
    }

    test("accepting without a planeswalker directly attacks the opponent") {
        val (driver, players) = setupPod(3)
        val me = players[0]
        val defender = players[1]
        val other = players[2]
        val evangelist = driver.putCreatureOnBattlefield(me, "Conclave Evangelist")
        driver.removeSummoningSickness(evangelist)
        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)

        driver.declareAttackers(me, listOf(evangelist), defendingPlayer = defender)
        advanceUntilDecision(driver)
        driver.submitYesNo(me, choice = true)

        // No planeswalker to choose between — no second decision, the token attacks directly.
        driver.pendingDecision.shouldBeNull()
        val copies = evangelistCopies(driver, me)
        copies shouldHaveSize 2
        val token = copies.first { it != evangelist }
        driver.state.getEntity(token)?.get<AttackingComponent>()?.defenderId shouldBe other
        driver.state.getEntity(token)?.has<TappedComponent>() shouldBe true
    }

    test("accepting when the opponent controls a planeswalker offers a choice between them") {
        val (driver, players) = setupPod(3)
        val me = players[0]
        val defender = players[1]
        val other = players[2]
        val evangelist = driver.putCreatureOnBattlefield(me, "Conclave Evangelist")
        driver.removeSummoningSickness(evangelist)
        val planeswalker = driver.putPermanentOnBattlefield(other, "Liliana of the Veil")
        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)

        driver.declareAttackers(me, listOf(evangelist), defendingPlayer = defender)
        advanceUntilDecision(driver)
        driver.submitYesNo(me, choice = true)

        val choice = driver.pendingDecision as? ChooseTargetsDecision
        choice.shouldNotBeNull()
        choice.playerId shouldBe me
        choice.legalTargets[0] shouldBe listOf(other, planeswalker)

        driver.submitTargetSelection(me, listOf(planeswalker))
        driver.pendingDecision.shouldBeNull()
        val token = evangelistCopies(driver, me).first { it != evangelist }
        driver.state.getEntity(token)?.get<AttackingComponent>()?.defenderId shouldBe planeswalker
    }

    test("the myriad token is exiled at end of combat, and never re-triggers myriad off its own attack") {
        val (driver, players) = setupPod(3)
        val me = players[0]
        val defender = players[1]
        val other = players[2]
        val evangelist = driver.putCreatureOnBattlefield(me, "Conclave Evangelist")
        driver.removeSummoningSickness(evangelist)
        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)

        driver.declareAttackers(me, listOf(evangelist), defendingPlayer = defender)
        advanceUntilDecision(driver)
        driver.submitYesNo(me, choice = true)
        driver.pendingDecision.shouldBeNull() // the token's own attack never re-asks Myriad

        val myriadToken = evangelistCopies(driver, me).first { it != evangelist }
        driver.state.getEntity(myriadToken)?.get<AttackingComponent>()?.defenderId shouldBe other

        // Drain the rest of combat (declare blockers, damage, end of combat — Conclave Evangelist's
        // own "deals combat damage, create a token copy" ability fires along the way for both
        // unblocked attackers, but takes no decisions, so this never pauses on anything myriad-related).
        repeat(10) {
            if (driver.pendingDecision != null) driver.autoResolveDecision() else driver.bothPass()
        }

        // Exiled at end of combat (CR 702.116a) — gone regardless of whatever other tokens
        // Conclave Evangelist's combat-damage ability created along the way.
        driver.state.getEntity(myriadToken).shouldBeNull()
    }
})
