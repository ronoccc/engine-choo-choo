package com.wingedsheep.engine.handlers.effects.token

import com.wingedsheep.engine.core.suspendForDecision
import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.DecisionContext
import com.wingedsheep.engine.core.DecisionPhase
import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.core.MyriadAttackTargetContinuation
import com.wingedsheep.engine.core.MyriadOpponentContinuation
import com.wingedsheep.engine.core.TargetRequirementInfo
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.TargetResolutionUtils
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.PlayerComponent
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect

/**
 * Drives Myriad's own per-opponent loop (CR 702.116a): "for each opponent other than defending
 * player, you may create a token that's a copy of this creature that's tapped and attacking that
 * player or a planeswalker they control."
 *
 * Two independent decisions per eligible opponent, asked one opponent at a time (their own ruling
 * confirms this — "you may choose separately for each token"):
 *  1. [MyriadOpponentContinuation] — "create a token attacking them?" (a plain yes/no). Declining
 *     moves on to the next opponent with no token.
 *  2. If accepted and that opponent controls one or more planeswalkers,
 *     [MyriadAttackTargetContinuation] — "attack them, or one of their planeswalkers?" (a 2+-way
 *     pick). If they control none, there is only one legal choice and no second decision is
 *     needed — the token just attacks them directly.
 *
 * Mirrors [AuraTokenHostChooser]'s pause/resume-with-a-smaller-remainder shape, but over a list of
 * *opponents* rather than a token *count* — Myriad's count is never known up front, since it's
 * exactly the number of opponents who end up with an accepted "may".
 */
internal object MyriadTokenChooser {

    /**
     * Entry point from [CreateTokenCopyOfTargetExecutor.execute]. Resolves the creature's current
     * defending player (CR 802.2a — always a player, even when attacking a planeswalker or battle)
     * and starts the loop over every *other* opponent. A 1-opponent game (the defending player is
     * the controller's only opponent) immediately finishes with zero tokens, per the card's own
     * ruling.
     */
    fun start(
        state: GameState,
        effect: CreateTokenCopyOfTargetEffect,
        context: EffectContext,
        controllerId: EntityId,
        executor: CreateTokenCopyOfTargetExecutor,
    ): EffectResult {
        val defendingPlayer = TargetResolutionUtils.resolveDefendingPlayer(context, state)
        val otherOpponents = state.getOpponents(controllerId).filter { it != defendingPlayer }
        return pauseForNextOpponent(state, effect, context, controllerId, otherOpponents, emptyList(), executor)
    }

    /**
     * Ask about the next opponent in [remainingOpponents] (head of the list), or — once none are
     * left — finish the effect.
     */
    fun pauseForNextOpponent(
        state: GameState,
        effect: CreateTokenCopyOfTargetEffect,
        context: EffectContext,
        controllerId: EntityId,
        remainingOpponents: List<EntityId>,
        createdTokens: List<EntityId>,
        executor: CreateTokenCopyOfTargetExecutor,
    ): EffectResult {
        val opponent = remainingOpponents.firstOrNull()
            ?: return finish(state, effect, context, controllerId, createdTokens, executor)

        val sourceName = context.sourceId?.let { state.getEntity(it)?.get<CardComponent>()?.name }
        val opponentName = state.getEntity(opponent)?.get<PlayerComponent>()?.name ?: "that opponent"

        val decision = { decisionId: String -> YesNoDecision(
            id = decisionId,
            playerId = controllerId,
            prompt = "Create a token copy of ${sourceName ?: "this creature"} attacking $opponentName?",
            context = DecisionContext(
                sourceId = context.sourceId,
                sourceName = sourceName,
                phase = DecisionPhase.RESOLUTION,
            ),
            yesText = "Create token",
            noText = "Don't",
        ) }

        val continuation = MyriadOpponentContinuation(
            effect = effect,
            context = context,
            controllerId = controllerId,
            opponentId = opponent,
            remainingOpponents = remainingOpponents,
            createdTokens = createdTokens,
        )

        return EffectResult.from(state.suspendForDecision(decision, continuation, emptyList()))
    }

    /**
     * After the controller accepts an opponent's token, choose whether it attacks that opponent
     * directly or a planeswalker they control. Skips the prompt entirely (and just attacks the
     * opponent) when they control zero planeswalkers — there is nothing to choose between.
     */
    fun pauseForAttackTargetOrCreate(
        state: GameState,
        effect: CreateTokenCopyOfTargetEffect,
        context: EffectContext,
        controllerId: EntityId,
        opponentId: EntityId,
        remainingOpponents: List<EntityId>,
        createdTokens: List<EntityId>,
        executor: CreateTokenCopyOfTargetExecutor,
    ): EffectResult {
        val theirPlaneswalkers = state.projectedState.getBattlefieldControlledBy(opponentId)
            .filter { state.projectedState.isPlaneswalker(it) }

        if (theirPlaneswalkers.isEmpty()) {
            return createOneAndContinue(
                state, effect, context, controllerId, opponentId, remainingOpponents, createdTokens, executor
            )
        }

        val candidates = listOf(opponentId) + theirPlaneswalkers
        val sourceName = context.sourceId?.let { state.getEntity(it)?.get<CardComponent>()?.name }
        val opponentName = state.getEntity(opponentId)?.get<PlayerComponent>()?.name ?: "that opponent"

        val decision = { decisionId: String -> ChooseTargetsDecision(
            id = decisionId,
            playerId = controllerId,
            prompt = "Attack $opponentName or one of their planeswalkers?",
            context = DecisionContext(
                sourceId = context.sourceId,
                sourceName = sourceName,
                phase = DecisionPhase.RESOLUTION,
            ),
            targetRequirements = listOf(
                TargetRequirementInfo(
                    index = 0,
                    description = "$opponentName or a planeswalker they control",
                    minTargets = 1,
                    maxTargets = 1,
                )
            ),
            legalTargets = mapOf(0 to candidates),
        ) }

        val continuation = MyriadAttackTargetContinuation(
            effect = effect,
            context = context,
            controllerId = controllerId,
            opponentId = opponentId,
            remainingOpponents = remainingOpponents,
            createdTokens = createdTokens,
        )

        return EffectResult.from(state.suspendForDecision(decision, continuation, emptyList()))
    }

    /** Create the one token this opponent's "yes" earned, then move on to the next opponent. */
    fun createOneAndContinue(
        state: GameState,
        effect: CreateTokenCopyOfTargetEffect,
        context: EffectContext,
        controllerId: EntityId,
        defenderId: EntityId,
        remainingOpponents: List<EntityId>,
        createdTokens: List<EntityId>,
        executor: CreateTokenCopyOfTargetExecutor,
    ): EffectResult {
        val result = executor.createTokens(
            state = state,
            effect = effect.copy(attacking = true, tapped = true),
            context = context,
            controllerId = controllerId,
            count = 1,
            auraHostId = null,
            forcedDefenderId = defenderId,
        )
        if (result.isPaused) {
            // A granted riot / as-enters choice on the token itself outranks the next opponent
            // prompt — only one suspension may be installed at a time (see the Aura chooser's
            // identical guard). No printed Myriad creature has such a choice today.
            return result
        }
        val newlyCreated = result.updatedCollections[com.wingedsheep.sdk.scripting.effects.CREATED_TOKENS]
            .orEmpty()
        val next = pauseForNextOpponent(
            result.state, effect, context, controllerId,
            remainingOpponents, createdTokens + newlyCreated, executor
        )
        val events = result.events + next.events
        return if (next.pendingDecision == null) EffectResult(next.state, events) else next.copy(events = events)
    }

    /**
     * No opponents left to ask. Applies the shared [CreateTokenCopyOfTargetExecutor.applyExileAtStep]
     * cleanup (Conclave Evangelist: `exileAtStep = Step.END_COMBAT`) — a no-op when [createdTokens]
     * is empty, which is exactly CR 702.116a's "if one or more tokens are created this way".
     */
    fun finish(
        state: GameState,
        effect: CreateTokenCopyOfTargetEffect,
        context: EffectContext,
        controllerId: EntityId,
        createdTokens: List<EntityId>,
        executor: CreateTokenCopyOfTargetExecutor,
    ): EffectResult {
        val (finalState, exileEvents) = executor.applyExileAtStep(state, effect, context, controllerId, createdTokens)
        return EffectResult(
            state = finalState,
            events = exileEvents,
            updatedCollections = mapOf(com.wingedsheep.sdk.scripting.effects.CREATED_TOKENS to createdTokens)
        )
    }
}
