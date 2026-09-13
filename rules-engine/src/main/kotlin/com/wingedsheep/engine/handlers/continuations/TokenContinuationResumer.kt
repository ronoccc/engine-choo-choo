package com.wingedsheep.engine.handlers.continuations

import com.wingedsheep.engine.core.DecisionResponse
import com.wingedsheep.engine.core.EngineServices
import com.wingedsheep.engine.core.ExecutionResult
import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.core.TokenCreationChoiceContinuation
import com.wingedsheep.engine.core.TokenCreationReplacementContinuation
import com.wingedsheep.engine.core.YesNoResponse
import com.wingedsheep.engine.handlers.effects.token.TokenCreationReplacementHelper
import com.wingedsheep.engine.mechanics.layers.StaticAbilityHandler
import com.wingedsheep.engine.state.GameState

/**
 * Handles token-related continuation resumptions:
 * - TokenCreationReplacementContinuation (Mirrormind Crown yes/no)
 * - TokenCreationChoiceContinuation (Jinnie Fay, Jetmir's Second: choose a template or decline)
 */
class TokenContinuationResumer(
    private val services: EngineServices
) : ContinuationResumerModule {

    override fun resumers(): List<ContinuationResumer<*>> = listOf(
        resumer(TokenCreationReplacementContinuation::class, ::resumeTokenCreationReplacement),
        resumer(TokenCreationChoiceContinuation::class, ::resumeTokenCreationChoice)
    )

    private fun resumeTokenCreationReplacement(
        state: GameState,
        continuation: TokenCreationReplacementContinuation,
        response: DecisionResponse,
        checkForMore: CheckForMore
    ): ExecutionResult {
        if (response !is YesNoResponse) {
            return ExecutionResult.error(state, "Expected yes/no response for token creation replacement")
        }

        val context = continuation.effectContext

        if (response.choice) {
            // Player chose to replace: create copies of the attached permanent.
            // Pass cardRegistry so the token applies the attached permanent's printed
            // "enters with N counters" replacement effects (per Mirrormind Crown rulings).
            val result = TokenCreationReplacementHelper.createAttachedPermanentCopies(
                state,
                continuation.attachedPermanentId,
                context.controllerId,
                continuation.tokenCount,
                cardRegistry = services.cardRegistry,
                staticAbilityHandler = StaticAbilityHandler(services.cardRegistry)
            )
            if (result.isPaused) return result.toExecutionResult()
            return checkForMore(result.state, result.events)
        } else {
            // Player declined: execute original token creation effect
            // The source is already marked as "offered this turn" so the replacement
            // won't fire again when we re-execute the original effect.
            val effectResult = services.effectExecutorRegistry.execute(
                state,
                continuation.originalEffect,
                context
            )
            if (effectResult.isPaused) return effectResult.toExecutionResult()
            return checkForMore(effectResult.state, effectResult.events)
        }
    }

    /**
     * Resume after the player answers a [com.wingedsheep.sdk.scripting.ReplaceTokenCreationWithChoiceOfTokens]
     * prompt (Jinnie Fay, Jetmir's Second). Index 0 (only offered when [TokenCreationChoiceContinuation.optional])
     * means "create the original tokens unchanged"; any other index selects the corresponding
     * [com.wingedsheep.sdk.scripting.AlternateTokenTemplate].
     *
     * If the source has since left the battlefield (defensive — see the "source leaving before
     * the choice resolves" note on the SDK type), a chosen template can't be honored since there
     * is no longer a permanent whose ability made the offer; fall back to the original effect
     * rather than fail the resolution.
     */
    private fun resumeTokenCreationChoice(
        state: GameState,
        continuation: TokenCreationChoiceContinuation,
        response: DecisionResponse,
        checkForMore: CheckForMore
    ): ExecutionResult {
        if (response !is OptionChosenResponse) {
            return ExecutionResult.error(state, "Expected option choice response for token creation choice")
        }

        val chosenTemplate = try {
            TokenCreationReplacementHelper.resolveChoiceContinuationOption(
                continuation.templates, continuation.optional, response.optionIndex
            )
        } catch (e: IllegalArgumentException) {
            return ExecutionResult.error(state, e.message ?: "Invalid token creation choice")
        }

        val sourceStillPresent = state.getEntity(continuation.sourceId) != null

        if (chosenTemplate == null || !sourceStillPresent) {
            // Declined, or the source vanished before the choice could be honored: run the
            // original token-creating effect unmodified. Exclude this source from the
            // re-triggered checkReplacement scan (EffectContext.declinedTokenReplacementSourceIds)
            // so the very same prompt isn't raised again for the tokens this re-execution creates.
            val context = continuation.effectContext.copy(
                declinedTokenReplacementSourceIds =
                    continuation.effectContext.declinedTokenReplacementSourceIds + continuation.sourceId
            )
            val effectResult = services.effectExecutorRegistry.execute(
                state, continuation.originalEffect, context
            )
            if (effectResult.isPaused) return effectResult.toExecutionResult()
            return checkForMore(effectResult.state, effectResult.events)
        }

        val context = continuation.effectContext

        val result = TokenCreationReplacementHelper.createChosenTemplateTokens(
            state = state,
            template = chosenTemplate,
            originalEffect = continuation.originalEffect,
            context = context,
            count = continuation.tokenCount,
            controllerId = continuation.tokenControllerId,
            staticAbilityHandler = StaticAbilityHandler(services.cardRegistry),
            cardRegistry = services.cardRegistry
        )
        if (result.isPaused) return result.toExecutionResult()
        return checkForMore(result.state, result.events)
    }
}
