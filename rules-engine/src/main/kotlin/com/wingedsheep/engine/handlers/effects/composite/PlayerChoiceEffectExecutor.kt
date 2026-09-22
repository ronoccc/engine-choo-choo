package com.wingedsheep.engine.handlers.effects.composite

import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.DecisionContext
import com.wingedsheep.engine.core.DecisionPhase
import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.core.PlayerChoiceContinuation
import com.wingedsheep.engine.core.suspendForDecision
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.ChooserResolution
import com.wingedsheep.engine.handlers.effects.EffectExecutor
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.scripting.VillainousChoiceExtraForOpponents
import com.wingedsheep.sdk.scripting.effects.CompositeEffect
import com.wingedsheep.sdk.scripting.effects.PlayerChoiceEffect
import kotlin.reflect.KClass

/**
 * Executor for [PlayerChoiceEffect] — the "villainous choice" mechanic.
 *
 * Resolves [PlayerChoiceEffect.chooser] to a concrete player (routing through the same
 * [ChooserResolution] every other chooser-carrying effect uses, including its "which opponent"
 * pause with several opponents), presents that player with the option descriptions, and pushes a
 * [PlayerChoiceContinuation] carrying the **unmodified** [EffectContext] — resolution of the
 * chosen option happens in [com.wingedsheep.engine.handlers.continuations.PlayerChoiceContinuationResumer].
 *
 * Every [PlayerChoiceEffect] is a CR-flavored "villainous choice", so — mirroring
 * [ChooseActionEffectExecutor]'s handling of `ChooseActionEffect(isVillainousChoice = true)` —
 * The Valeyard's "if an opponent would face a villainous choice, they face that choice an
 * additional time" doubles it here too: once the chooser is resolved, count
 * [VillainousChoiceExtraForOpponents] sources whose controller has that chooser as an opponent,
 * and resolve that many extra (independent) copies via a [CompositeEffect] before presenting
 * anything. Without this, a card using [com.wingedsheep.sdk.dsl.Effects.VillainousChoice] (This
 * Is How It Ends, Great Intelligence's Plan, …) silently ignored The Valeyard even though a
 * `ChooseActionEffect`-based villainous choice (Davros, Dalek Creator) already honored it.
 */
class PlayerChoiceEffectExecutor(
    private val effectExecutor: ((GameState, com.wingedsheep.sdk.scripting.effects.Effect, EffectContext) -> EffectResult)? = null,
    private val cardRegistry: com.wingedsheep.engine.registry.CardRegistry? = null
) : EffectExecutor<PlayerChoiceEffect> {

    override val effectType: KClass<PlayerChoiceEffect> = PlayerChoiceEffect::class

    override fun execute(
        state: GameState,
        effect: PlayerChoiceEffect,
        context: EffectContext
    ): EffectResult {
        val chooserId = when (val outcome = ChooserResolution.resolve(state, effect.chooser, context)) {
            is ChooserResolution.Outcome.Resolved -> outcome.playerId
            is ChooserResolution.Outcome.NeedsOpponentPick -> return ChooserResolution.pauseForOpponentPick(
                state, outcome.opponents, effect, context,
                prompt = "Choose which opponent faces the villainous choice"
            )
            is ChooserResolution.Outcome.Unresolvable ->
                return EffectResult.error(state, "PlayerChoice chooser: ${outcome.reason}")
        }

        if (!effect.alreadyDoubled && effectExecutor != null && cardRegistry != null) {
            val extraTimes = countVillainousChoiceDoublers(state, cardRegistry, chooserId)
            if (extraTimes > 0) {
                val single = effect.copy(alreadyDoubled = true)
                val repeated = CompositeEffect(List(1 + extraTimes) { single })
                return effectExecutor.invoke(state, repeated, context)
            }
        }

        val sourceName = context.sourceId?.let { state.getEntity(it)?.get<CardComponent>()?.name }

        val decision = { decisionId: String -> ChooseOptionDecision(
            id = decisionId,
            playerId = chooserId,
            prompt = effect.promptOverride ?: "Face a villainous choice",
            context = DecisionContext(
                sourceId = context.sourceId,
                sourceName = sourceName,
                phase = DecisionPhase.RESOLUTION
            ),
            options = effect.options.map { it.description }
        ) }

        val continuation = PlayerChoiceContinuation(
            chooserId = chooserId,
            options = effect.options,
            effectContext = context
        )

        return EffectResult.from(state.suspendForDecision(decision, continuation))
    }

    /**
     * Number of battlefield permanents carrying [VillainousChoiceExtraForOpponents] whose
     * controller has [chooserId] as an opponent (The Valeyard). Mirrors
     * [ChooseActionEffectExecutor.countVillainousChoiceDoublers] exactly — kept as a separate
     * copy rather than a shared helper since the two executors' constructor shapes differ and
     * this one is small.
     */
    private fun countVillainousChoiceDoublers(
        state: GameState,
        cardRegistry: com.wingedsheep.engine.registry.CardRegistry,
        chooserId: com.wingedsheep.sdk.model.EntityId
    ): Int {
        return state.turnOrder.sumOf { playerId ->
            if (!state.isOpponentOf(chooserId, playerId)) return@sumOf 0
            state.projectedState.getBattlefieldControlledBy(playerId).sumOf { permanentId ->
                val card = state.getEntity(permanentId)?.get<CardComponent>() ?: return@sumOf 0
                val cardDef = cardRegistry.getCard(card.cardDefinitionId) ?: return@sumOf 0
                cardDef.script.staticAbilities.count { it is VillainousChoiceExtraForOpponents }
            }
        }
    }
}
