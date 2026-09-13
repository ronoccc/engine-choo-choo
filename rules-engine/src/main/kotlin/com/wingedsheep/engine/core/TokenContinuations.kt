package com.wingedsheep.engine.core

import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.AlternateTokenTemplate
import com.wingedsheep.sdk.scripting.effects.Effect
import kotlinx.serialization.Serializable

/**
 * Resume token creation after the player answers a "may" question for
 * ReplaceTokenCreationWithAttachedCopy (Mirrormind Crown, Moonlit Meditation).
 *
 * If yes: create [tokenCount] token copies of the attached permanent.
 * If no: execute the original [originalEffect] normally — this can be any
 * token-creating effect (e.g. CreateTokenEffect, CreateTokenCopyOfTargetEffect).
 *
 * @property sourceId The Equipment / Aura / other permanent with the replacement effect
 * @property attachedPermanentId The permanent attached at the time the decision was posed
 * @property originalEffect The original token creation effect (used if player declines)
 * @property tokenCount The evaluated number of tokens to create
 * @property effectContext The execution context from the original effect
 */
@Serializable
data class TokenCreationReplacementContinuation(
    val sourceId: EntityId,
    val attachedPermanentId: EntityId,
    val originalEffect: Effect,
    val tokenCount: Int,
    val effectContext: EffectContext
) : AnswerContinuation

/**
 * Resume token creation after the player answers a [ReplaceTokenCreationWithChoiceOfTokens]
 * prompt (Jinnie Fay, Jetmir's Second) — a single [ChooseOptionDecision] offering "don't
 * replace" (when [optional]) followed by one option per entry in [templates].
 *
 * Option index 0 means "create the original tokens unchanged" when [optional] is true (and is
 * not offered when it's false); every other index `i` selects `templates[i - (if optional) 1
 * else 0]`.
 *
 * @property sourceId The permanent carrying the replacement effect.
 * @property originalEffect The original token-creating effect — replayed unmodified on decline,
 *           and the source of the "still applies" riders (tapped / attacking / exile-at-step /
 *           sacrifice-at-step) when a template is chosen instead.
 * @property tokenCount The already-replacement-adjusted token count (Doubling Season etc. have
 *           already been applied upstream).
 * @property effectContext The execution context from the original effect.
 * @property templates The alternate templates offered, in prompt order.
 * @property optional Whether "don't replace" was offered as option 0.
 * @property tokenControllerId The player who would receive the tokens (and who answers this
 *           decision) — carried explicitly rather than assumed to equal
 *           `effectContext.controllerId`, since a token-creating effect can target a different
 *           recipient (e.g. `CreateTokenEffect.controller`).
 */
@Serializable
data class TokenCreationChoiceContinuation(
    val sourceId: EntityId,
    val originalEffect: Effect,
    val tokenCount: Int,
    val effectContext: EffectContext,
    val templates: List<AlternateTokenTemplate>,
    val optional: Boolean,
    val tokenControllerId: EntityId
) : AnswerContinuation
