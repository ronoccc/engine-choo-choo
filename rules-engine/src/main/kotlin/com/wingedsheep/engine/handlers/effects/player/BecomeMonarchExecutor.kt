package com.wingedsheep.engine.handlers.effects.player

import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.EffectExecutor
import com.wingedsheep.engine.mechanics.monarch.MonarchService
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.scripting.effects.BecomeMonarchEffect
import kotlin.reflect.KClass

/**
 * Resolves [BecomeMonarchEffect] — "[target] becomes the monarch" (CR 716.1), including the CR
 * 716.6 combat-damage transfer synthesized in
 * [com.wingedsheep.engine.event.MonarchAbilities.combatDamageTransfer].
 *
 * Writes through [MonarchService.become], which is idempotent (CR 716.2).
 */
class BecomeMonarchExecutor : EffectExecutor<BecomeMonarchEffect> {

    override val effectType: KClass<BecomeMonarchEffect> = BecomeMonarchEffect::class

    override fun execute(
        state: GameState,
        effect: BecomeMonarchEffect,
        context: EffectContext
    ): EffectResult {
        val targetId = context.resolveTarget(effect.target)
            ?: return EffectResult.error(state, "No valid target to become the monarch")

        if (!state.turnOrder.contains(targetId)) {
            return EffectResult.error(state, "Monarch target must be a player")
        }

        if (state.getEntity(targetId) == null) {
            return EffectResult.error(state, "Target player no longer exists")
        }

        val sourceName = context.sourceId?.let {
            state.getEntity(it)?.get<CardComponent>()?.name
        } ?: "Unknown"

        val (newState, events) = MonarchService.become(state, targetId, sourceName)
        return EffectResult.success(newState, events)
    }
}
