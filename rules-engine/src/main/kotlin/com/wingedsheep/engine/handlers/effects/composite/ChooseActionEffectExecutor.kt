package com.wingedsheep.engine.handlers.effects.composite

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.PipelineState
import com.wingedsheep.engine.handlers.PredicateContext
import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.engine.handlers.effects.BattlefieldFilterUtils
import com.wingedsheep.engine.handlers.effects.EffectExecutor
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.scripting.VillainousChoiceExtraForOpponents
import com.wingedsheep.sdk.scripting.effects.ChooseActionEffect
import com.wingedsheep.sdk.scripting.effects.CompositeEffect
import com.wingedsheep.sdk.scripting.effects.Effect
import com.wingedsheep.sdk.scripting.effects.EffectChoice
import com.wingedsheep.sdk.scripting.effects.FeasibilityCheck
import kotlin.reflect.KClass

/**
 * Executor for [ChooseActionEffect].
 *
 * Presents a player with labeled options and executes the chosen effect.
 * Infeasible options are filtered out. If only one remains, it auto-executes.
 * If zero remain, nothing happens.
 */
class ChooseActionEffectExecutor(
    private val effectExecutor: (GameState, Effect, EffectContext) -> EffectResult,
    private val cardRegistry: CardRegistry? = null
) : EffectExecutor<ChooseActionEffect> {

    override val effectType: KClass<ChooseActionEffect> = ChooseActionEffect::class

    private val predicateEvaluator = PredicateEvaluator()

    override fun execute(
        state: GameState,
        effect: ChooseActionEffect,
        context: EffectContext
    ): EffectResult {
        // Resolve who makes the choice. State-aware so relational references such as
        // EffectTarget.TargetController (the controller of the chosen permanent) resolve — used by
        // "[do X to a permanent] unless its controller [accepts an avoidance]" choices routed to the
        // targeted permanent's controller rather than the ability's controller.
        val choosingPlayerId = context.resolvePlayerTarget(effect.player, state)
            ?: return EffectResult.error(state, "Could not resolve player for ChooseActionEffect")

        // "If an opponent would face a villainous choice, they face that choice an additional
        // time" (The Valeyard) — count VillainousChoiceExtraForOpponents sources whose controller
        // has choosingPlayerId as an opponent, and if any, resolve the (un-flagged, so it doesn't
        // re-trigger itself) choice that many extra times via CompositeEffect.
        if (effect.isVillainousChoice) {
            val extraTimes = countVillainousChoiceDoublers(state, choosingPlayerId)
            if (extraTimes > 0) {
                val single = effect.copy(isVillainousChoice = false)
                val repeated = CompositeEffect(List(1 + extraTimes) { single })
                return effectExecutor(state, repeated, context)
            }
        }

        // Filter to feasible choices
        val feasibleChoices = effect.choices.filter { choice ->
            isFeasible(state, choosingPlayerId, choice.feasibilityCheck)
        }

        if (feasibleChoices.isEmpty()) {
            return EffectResult.success(state)
        }

        // If only one option, auto-execute it
        if (feasibleChoices.size == 1) {
            return effectExecutor(state, feasibleChoices[0].effect, context)
        }

        // Present options to the choosing player
        val sourceName = context.sourceId?.let { sourceId ->
            state.getEntity(sourceId)?.get<CardComponent>()?.name
        }

        val decision = { decisionId: String -> ChooseOptionDecision(
            id = decisionId,
            playerId = choosingPlayerId,
            prompt = "Choose one for ${sourceName ?: "ability"}",
            context = DecisionContext(
                sourceId = context.sourceId,
                sourceName = sourceName,
                phase = DecisionPhase.RESOLUTION
            ),
            options = feasibleChoices.map { it.label }
        ) }

        val continuation = ChooseActionContinuation(
            choosingPlayerId = choosingPlayerId,
            controllerId = context.controllerId,
            sourceId = context.sourceId,
            objectReferences = context.objectReferences,
            sourceName = sourceName,
            choices = feasibleChoices,
            targets = context.targets,
            namedTargets = context.pipeline.namedTargets,
            triggeringEntityId = context.triggeringEntityId
        )

        return EffectResult.from(state.suspendForDecision(decision, continuation))
    }

    private fun isFeasible(
        state: GameState,
        playerId: com.wingedsheep.sdk.model.EntityId,
        check: FeasibilityCheck?
    ): Boolean = checkFeasibility(state, playerId, check, predicateEvaluator)

    /**
     * Number of battlefield permanents carrying [VillainousChoiceExtraForOpponents] whose
     * controller has [choosingPlayerId] as an opponent (The Valeyard). Mirrors
     * [com.wingedsheep.engine.handlers.effects.CoinFlipModifiers] — walk the controller's
     * battlefield, look up each permanent's [com.wingedsheep.sdk.model.CardDefinition] via the
     * registry, and count matching static abilities. Uses projected controllers so a stolen
     * Valeyard still doubles for its new controller's opponents.
     */
    private fun countVillainousChoiceDoublers(
        state: GameState,
        choosingPlayerId: com.wingedsheep.sdk.model.EntityId
    ): Int {
        val registry = cardRegistry ?: return 0
        return state.turnOrder.sumOf { playerId ->
            if (!state.isOpponentOf(choosingPlayerId, playerId)) return@sumOf 0
            state.projectedState.getBattlefieldControlledBy(playerId).sumOf { permanentId ->
                val card = state.getEntity(permanentId)?.get<CardComponent>() ?: return@sumOf 0
                val cardDef = registry.getCard(card.cardDefinitionId) ?: return@sumOf 0
                cardDef.script.staticAbilities.count { it is VillainousChoiceExtraForOpponents }
            }
        }
    }
}

/**
 * Check whether a [FeasibilityCheck] is satisfied for the given player.
 * Shared by [ChooseActionEffectExecutor], [MayEffectExecutor], and [ReflexiveTriggerEffectExecutor].
 */
internal fun checkFeasibility(
    state: GameState,
    playerId: com.wingedsheep.sdk.model.EntityId,
    check: FeasibilityCheck?,
    predicateEvaluator: PredicateEvaluator = PredicateEvaluator()
): Boolean {
    if (check == null) return true

    return when (check) {
        is FeasibilityCheck.ControlsPermanentMatching -> {
            val matching = BattlefieldFilterUtils.findMatchingOnBattlefield(
                state,
                check.filter.youControl(),
                PredicateContext(controllerId = playerId)
            )
            matching.size >= check.count
        }
        is FeasibilityCheck.HasCardsInZone -> {
            val zoneKey = ZoneKey(playerId, check.zone)
            val cards = state.getZone(zoneKey)
            if (check.filter == com.wingedsheep.sdk.scripting.GameObjectFilter.Any) {
                cards.size >= check.count
            } else {
                val context = PredicateContext(controllerId = playerId)
                cards.count { cardId ->
                    predicateEvaluator.matches(state, state.projectedState, cardId, check.filter, context)
                } >= check.count
            }
        }
    }
}
