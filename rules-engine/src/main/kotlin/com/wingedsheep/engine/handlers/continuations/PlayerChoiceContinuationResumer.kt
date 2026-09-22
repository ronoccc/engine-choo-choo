package com.wingedsheep.engine.handlers.continuations

import com.wingedsheep.engine.core.EngineServices
import com.wingedsheep.engine.core.ExecutionResult
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.core.PlayerChoiceContinuation
import com.wingedsheep.engine.core.DecisionResponse
import com.wingedsheep.engine.state.GameState

/**
 * Resumes [PlayerChoiceContinuation]: the resolved chooser of a
 * [com.wingedsheep.sdk.scripting.effects.PlayerChoiceEffect] picked one of its options. Runs that
 * option's effect in the captured (unmodified) effect context — see
 * [com.wingedsheep.engine.handlers.effects.composite.PlayerChoiceEffectExecutor] for why the
 * context is never rebound to the chooser.
 */
class PlayerChoiceContinuationResumer(
    services: EngineServices
) : ContinuationResumerModule {

    private val effectRunner: EffectContinuationRunner by lazy {
        EffectContinuationRunner(services.effectExecutorRegistry)
    }

    override fun resumers(): List<ContinuationResumer<*>> = listOf(
        resumer(PlayerChoiceContinuation::class, ::resume)
    )

    private fun resume(
        state: GameState,
        continuation: PlayerChoiceContinuation,
        response: DecisionResponse,
        checkForMore: CheckForMore
    ): ExecutionResult {
        if (response !is OptionChosenResponse) {
            return ExecutionResult.error(state, "Expected option choice for villainous choice")
        }
        val chosen = continuation.options.getOrNull(response.optionIndex)
            ?: return ExecutionResult.error(state, "Invalid villainous choice option index: ${response.optionIndex}")

        val result = effectRunner.executeRemainingEffects(
            state,
            listOf(chosen.effect),
            continuation.effectContext
        )

        if (result.isPaused) {
            return ExecutionResult.propagatePause(result.state, result.events)
        }
        return checkForMore(result.state, result.events.toList())
    }
}
