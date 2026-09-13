package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.handlers.ConditionEvaluator
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.ZoneTransitionService
import com.wingedsheep.engine.handlers.effects.token.CreateTokenExecutor
import com.wingedsheep.engine.state.components.player.PermanentsEnteredUnderControlThisTurnComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for the "created a token this turn" tracker (`TurnTracker.TOKENS_CREATED`), which backs
 * Idol of Oblivion's "{T}: Draw a card. Activate only if you created a token this turn."
 *
 * Backed by the same per-player [PermanentsEnteredUnderControlThisTurnComponent] entry log every
 * other "an X entered the battlefield under your control this turn" reader uses — token creation
 * routes through the same `BattlefieldEntry.place` / `PermanentEntryTracker.record` chokepoint as
 * every other battlefield entry, so no separate tracking site was added. `countTokens()` reads
 * the `isToken` flag stamped on each entry, and `TurnTracker.TOKENS_CREATED` /
 * `Conditions.YouCreatedATokenThisTurn` wrap it — cleared at cleanup by `CleanupPhaseManager`
 * exactly like [PermanentsEnteredUnderControlThisTurnComponent] already is.
 *
 * Per the printed Idol of Oblivion ruling (2019-08-23): "You can activate Idol of Oblivion's
 * first ability even if the token you've created was created before Idol of Oblivion entered the
 * battlefield or if the token has left the battlefield" — this is turn *history*, not a live
 * board count, which several tests below pin down directly.
 */
class TokensCreatedTrackerTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.initMirrorMatch(
            deck = Deck.of(
                "Forest" to 10,
                "Plains" to 10,
                "Grizzly Bears" to 10,
            ),
            skipMulligans = true
        )
        return driver
    }

    fun GameTestDriver.tokensCreatedCount(playerId: EntityId): Int =
        state.getEntity(playerId)
            ?.get<PermanentsEnteredUnderControlThisTurnComponent>()?.countTokens() ?: 0

    fun GameTestDriver.evalCreatedTokenThisTurn(): Boolean {
        val controller = activePlayer!!
        val context = EffectContext(
            sourceId = null,
            controllerId = controller,
            targets = emptyList(),
            xValue = 0
        )
        return ConditionEvaluator().evaluate(state, Conditions.YouCreatedATokenThisTurn, context)
    }

    /** Create [count] 1/1 colorless Servo tokens under [controllerId]'s control. */
    fun GameTestDriver.createTokens(controllerId: EntityId, count: Int = 1): List<EntityId> {
        val effect = CreateTokenEffect(
            power = 1,
            toughness = 1,
            colors = emptySet(),
            creatureTypes = setOf("Servo"),
            count = com.wingedsheep.sdk.scripting.values.DynamicAmount.Fixed(count)
        )
        val context = EffectContext(sourceId = null, controllerId = controllerId, targets = emptyList(), xValue = 0)
        val result = CreateTokenExecutor().execute(state, effect, context)
        replaceState(result.state)
        return result.updatedCollections[com.wingedsheep.sdk.scripting.effects.CREATED_TOKENS].orEmpty()
    }

    fun GameTestDriver.move(entityId: EntityId, destination: Zone) {
        val result = ZoneTransitionService.moveToZone(
            state = state,
            entityId = entityId,
            destinationZone = destination
        )
        replaceState(result.state)
    }

    test("starts at zero and condition is false") {
        val driver = createDriver()
        val player = driver.activePlayer!!
        driver.tokensCreatedCount(player) shouldBe 0
        driver.evalCreatedTokenThisTurn() shouldBe false
    }

    test("creating a token counts, and the condition becomes true") {
        val driver = createDriver()
        val player = driver.activePlayer!!

        driver.createTokens(player)

        driver.tokensCreatedCount(player) shouldBe 1
        driver.evalCreatedTokenThisTurn() shouldBe true
    }

    test("counts accumulate across a multi-token creation") {
        val driver = createDriver()
        val player = driver.activePlayer!!

        driver.createTokens(player, count = 3)

        driver.tokensCreatedCount(player) shouldBe 3
    }

    test("counts accumulate across separate creation events") {
        val driver = createDriver()
        val player = driver.activePlayer!!

        driver.createTokens(player, count = 1)
        driver.createTokens(player, count = 2)

        driver.tokensCreatedCount(player) shouldBe 3
    }

    test("a nontoken permanent entering the battlefield does NOT count") {
        val driver = createDriver()
        val player = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        driver.putCreatureOnBattlefield(player, "Grizzly Bears")

        driver.tokensCreatedCount(player) shouldBe 0
        driver.evalCreatedTokenThisTurn() shouldBe false
    }

    test("condition stays true after the token has left the battlefield") {
        val driver = createDriver()
        val player = driver.activePlayer!!

        val tokens = driver.createTokens(player)
        driver.evalCreatedTokenThisTurn() shouldBe true

        driver.move(tokens.single(), Zone.GRAVEYARD)

        driver.tokensCreatedCount(player) shouldBe 1
        driver.evalCreatedTokenThisTurn() shouldBe true
    }

    test("count is keyed on controller — an opponent's tokens do not satisfy your condition") {
        val driver = createDriver()
        val player = driver.activePlayer!!
        val opponent = driver.getOpponent(player)

        driver.createTokens(opponent)

        driver.tokensCreatedCount(opponent) shouldBe 1
        driver.tokensCreatedCount(player) shouldBe 0
        driver.evalCreatedTokenThisTurn() shouldBe false
    }

    test("count resets at end of turn") {
        val driver = createDriver()
        val player = driver.activePlayer!!
        val opponent = driver.getOpponent(player)

        driver.createTokens(player)
        driver.tokensCreatedCount(player) shouldBe 1

        driver.passPriorityUntil(Step.END, maxPasses = 200)
        driver.bothPass()
        driver.activePlayer shouldBe opponent

        driver.tokensCreatedCount(player) shouldBe 0
    }
})
