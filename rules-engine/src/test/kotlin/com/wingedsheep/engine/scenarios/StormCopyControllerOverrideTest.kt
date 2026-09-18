package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.TargetFinder
import com.wingedsheep.engine.handlers.effects.stack.StormCopyEffectExecutor
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.ComponentContainer
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.CastChoicesComponent
import com.wingedsheep.engine.state.components.battlefield.ChoiceValue
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.engine.state.components.identity.OwnerComponent
import com.wingedsheep.engine.state.components.identity.CopyOfComponent
import com.wingedsheep.engine.state.components.stack.SpellOnStackComponent
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.TypeLine
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.ChoiceSlot
import com.wingedsheep.sdk.scripting.effects.DrawCardsEffect
import com.wingedsheep.sdk.scripting.effects.StormCopyEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetPlayer
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * [StormCopyEffect.copyController] — the extension `StormCopyEffect` needed to model Demonstrate
 * (CR 702.144a: "That player copies the spell"), where the *second* copy is controlled by, and
 * its new targets chosen by, a player other than the ability's own controller. Executor-level
 * (as [StormCopyInheritsAllDecisionsTest]) — the full cast/trigger flow is exercised separately
 * in [DemonstrateScenarioTest].
 */
class StormCopyControllerOverrideTest : FunSpec({

    fun buildState(p1: EntityId, p2: EntityId, spellEntity: EntityId, targeted: Boolean): GameState {
        val cardComponent = CardComponent(
            cardDefinitionId = "X Bolt",
            name = "X Bolt",
            manaCost = ManaCost.parse("{R}"),
            typeLine = TypeLine.instant(),
            oracleText = "",
            ownerId = p1,
            spellEffect = null
        )
        var state = GameState(
            activePlayerId = p1,
            priorityPlayerId = p1,
            turnOrder = listOf(p1, p2)
        )
            .withEntity(p1, ComponentContainer.of(com.wingedsheep.engine.state.components.identity.PlayerComponent("P1")))
            .withEntity(p2, ComponentContainer.of(com.wingedsheep.engine.state.components.identity.PlayerComponent("P2")))
            .withEntity(
                spellEntity,
                ComponentContainer.of(
                    cardComponent,
                    OwnerComponent(p1),
                    ControllerComponent(p1),
                    SpellOnStackComponent(casterId = p1),
                    // The opponent chosen for this spell's demonstrate trigger, as
                    // ChooseOpponentForSourceEffect would have written it (CR 702.144a).
                    CastChoicesComponent(chosen = mapOf(ChoiceSlot.OPPONENT to ChoiceValue.EntityChoice(p2)))
                )
            )
            .copy(stack = listOf(spellEntity))
        return state
    }

    fun runStorm(state: GameState, spellEntity: EntityId, p1: EntityId, targeted: Boolean) =
        StormCopyEffectExecutor(
            cardRegistry = CardRegistry(),
            targetFinder = TargetFinder()
        ).execute(
            state,
            StormCopyEffect(
                copyCount = 1,
                spellEffect = DrawCardsEffect(DynamicAmount.Fixed(1), EffectTarget.Controller),
                spellTargetRequirements = if (targeted) listOf(TargetPlayer()) else emptyList(),
                spellName = "X Bolt",
                copyController = EffectTarget.PlayerRef(Player.ChosenOpponent)
            ),
            EffectContext(sourceId = spellEntity, controllerId = p1)
        )

    fun copyId(state: GameState) = state.stack.single { id ->
        val c = state.getEntity(id)
        c?.get<SpellOnStackComponent>() != null && c.has<CopyOfComponent>()
    }

    test("untargeted copy is controlled and owned by the chosen opponent, not the ability's controller") {
        val p1 = EntityId.generate()
        val p2 = EntityId.generate()
        val spellEntity = EntityId.generate()

        val result = runStorm(buildState(p1, p2, spellEntity, targeted = false), spellEntity, p1, targeted = false)
        result.isSuccess shouldBe true

        val copy = result.state.getEntity(copyId(result.state))!!
        copy.get<CardComponent>()!!.ownerId shouldBe p2
        copy.get<SpellOnStackComponent>()!!.casterId shouldBe p2
    }

    test("a targeted copy's retargeting decision is addressed to the chosen opponent, not the caster") {
        val p1 = EntityId.generate()
        val p2 = EntityId.generate()
        val spellEntity = EntityId.generate()

        val result = runStorm(buildState(p1, p2, spellEntity, targeted = true), spellEntity, p1, targeted = true)
        result.isPaused shouldBe true
        val decision = result.pendingDecision
        decision.shouldBeInstanceOf<ChooseTargetsDecision>()
        decision.playerId shouldBe p2
    }
})
