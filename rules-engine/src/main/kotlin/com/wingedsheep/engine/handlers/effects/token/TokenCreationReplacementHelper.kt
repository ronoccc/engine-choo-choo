package com.wingedsheep.engine.handlers.effects.token

import com.wingedsheep.engine.core.suspendForDecision
import com.wingedsheep.engine.core.ChooseOptionDecision
import com.wingedsheep.engine.core.DecisionContext
import com.wingedsheep.engine.core.DecisionPhase
import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.core.TokenCreationChoiceContinuation
import com.wingedsheep.engine.core.TokenCreationReplacementContinuation
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.core.ZoneChangeEvent
import com.wingedsheep.engine.event.DelayedTriggeredAbility
import com.wingedsheep.engine.handlers.ConditionEvaluator
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.EnterTappedReplacements
import com.wingedsheep.engine.handlers.effects.EntersWithReplacements
import com.wingedsheep.engine.handlers.effects.TargetResolutionUtils
import com.wingedsheep.engine.mechanics.layers.StaticAbilityHandler
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.replacement.ActiveReplacements
import com.wingedsheep.engine.state.Component
import com.wingedsheep.engine.state.ComponentContainer
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.battlefield.EnteredThisTurnComponent
import com.wingedsheep.engine.state.components.battlefield.ReplacementEffectSourceComponent
import com.wingedsheep.engine.state.components.battlefield.SummoningSicknessComponent
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.battlefield.TokenReplacementOfferedThisTurnComponent
import com.wingedsheep.engine.state.components.combat.AttackingComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.engine.handlers.PredicateContext
import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.TypeLine
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.CreatureStats
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.AlternateTokenTemplate
import com.wingedsheep.sdk.scripting.CreateAdditionalToken
import com.wingedsheep.sdk.scripting.MultiplyTokenCreation
import com.wingedsheep.sdk.scripting.EventPattern as SdkGameEvent
import com.wingedsheep.sdk.scripting.ModifyTokenCount
import com.wingedsheep.sdk.scripting.ReplaceTokenCreationWithAttachedCopy
import com.wingedsheep.sdk.scripting.ReplaceTokenCreationWithChoiceOfTokens
import com.wingedsheep.sdk.scripting.effects.CreatePredefinedTokenEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.effects.Effect
import com.wingedsheep.sdk.scripting.effects.MoveToZoneEffect
import com.wingedsheep.sdk.scripting.effects.SacrificeTargetEffect
import com.wingedsheep.sdk.scripting.events.ControllerFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Checks for token creation replacement effects (e.g., Mirrormind Crown)
 * before tokens are created. If a replacement applies, returns a paused
 * EffectResult with a yes/no decision; otherwise returns null.
 */
object TokenCreationReplacementHelper {

    /**
     * CR 614-style "whose replacement is this" test: `You` matches when the replacement's own
     * controller is the player the tokens are being created under, `Opponent` when it isn't,
     * `Any` always. Shared by the count and additional-token read paths so a printed ability and
     * a durational grant are dispatched identically.
     */
    private fun controllerMatches(
        filter: ControllerFilter,
        sourceControllerId: EntityId,
        tokenControllerId: EntityId
    ): Boolean = when (filter) {
        is ControllerFilter.You -> sourceControllerId == tokenControllerId
        is ControllerFilter.Opponent -> sourceControllerId != tokenControllerId
        is ControllerFilter.Any -> true
    }

    /**
     * Apply token-count replacement effects whose [SdkGameEvent.TokenCreationEvent] filter
     * matches the player receiving the tokens:
     * - [MultiplyTokenCreation] (Anointed Procession / Exalted Sunborn — factor 2; Ojer Taq —
     *   factor 3) multiplies the count by its factor per source; stacks multiplicatively when
     *   several apply.
     * - [ModifyTokenCount] shifts the count by a fixed amount per source (clamped at zero).
     *
     * Both printed abilities on battlefield permanents *and* durational grants in
     * [com.wingedsheep.engine.state.GameState.grantedReplacementEffects] count — the two are
     * enumerated together by [ActiveReplacements], so Kaya, Geist Hunter's until-end-of-turn
     * doubler stacks with a Doubling Season exactly as a second printed doubler would, and
     * keeps working after Kaya herself has left the battlefield.
     *
     * Dispatch reads `appliesTo.controller`, mirroring [ReplacementEffectUtils.applyCounterPlacementModifiers]:
     * `You` matches when the replacement's controller is the player receiving the tokens,
     * `Opponent` matches when it isn't, `Any` always matches.
     *
     * CR 616.1 hands the order to the affected player when both kinds apply. We default to
     * modifier-then-doublers, which maximizes the count for positive modifiers (e.g. base 3
     * with +1 mod and ×2 doubler: 8 vs 7) and matches what a player optimizing for more
     * tokens would pick. A proper "choose the order" prompt can land when a card actually
     * combines the two kinds.
     */
    fun applyCountReplacements(
        state: GameState,
        tokenControllerId: EntityId,
        baseCount: Int
    ): Int {
        if (baseCount <= 0) return baseCount

        val factors = mutableListOf<Int>()
        var modifier = 0
        for (active in ActiveReplacements.all(state)) {
            val effect = active.effect
            val event = when (effect) {
                is MultiplyTokenCreation -> effect.appliesTo
                is ModifyTokenCount -> effect.appliesTo
                else -> continue
            }
            if (event !is SdkGameEvent.TokenCreationEvent) continue
            // tokenFilter would need a synthetic token-template snapshot to match
            // against; no card uses it yet, so we conservatively skip filtered events
            // rather than treating them as match-all.
            if (event.tokenFilter != null) continue
            if (!controllerMatches(event.controller, active.controllerId, tokenControllerId)) continue
            when (effect) {
                is MultiplyTokenCreation -> factors += effect.factor
                is ModifyTokenCount -> modifier += effect.modifier
            }
        }

        // Saturating arithmetic: a stack of doublers (Doubling Season + Anointed Procession + …)
        // multiplies geometrically, which would overflow `Int` to a negative count. Clamp instead
        // (GameLimits); CreateTokenExecutor applies the structural MAX_TOKENS_PER_EFFECT cap before
        // actually allocating entities.
        var count = com.wingedsheep.engine.core.GameLimits.addClamped(baseCount, modifier)
        if (count <= 0) return 0
        for (factor in factors) count = com.wingedsheep.engine.core.GameLimits.mulClamped(count, factor)
        return count
    }

    /**
     * Apply [CreateAdditionalToken] replacement effects after a batch of tokens has been
     * created. Models Worldwalker Helm: "If you would create one or more artifact tokens,
     * instead create those tokens plus an additional Map token."
     *
     * Called by the token-creation executors *after* they place their batch, passing the
     * IDs of the tokens just created and the player who created them. For each active
     * [CreateAdditionalToken] (printed on a battlefield permanent or granted — see
     * [ActiveReplacements]) whose `appliesTo` controller filter matches and whose `tokenFilter`
     * (if any) matches at least one of [createdTokenIds], the additional predefined tokens are
     * created once.
     *
     * The additional tokens are placed directly (no further replacement check) so the added
     * artifact Map token cannot recursively re-trigger the same effect — only the original
     * [createdTokenIds] are considered for the filter match.
     *
     * @return the updated state plus the created additional-token events (empty if none applied).
     */
    fun applyAdditionalTokenReplacements(
        state: GameState,
        tokenControllerId: EntityId,
        createdTokenIds: List<EntityId>,
        originalTapped: Boolean,
        cardRegistry: CardRegistry?,
        staticAbilityHandler: StaticAbilityHandler?,
        predicateEvaluator: PredicateEvaluator = PredicateEvaluator(),
        conditionEvaluator: ConditionEvaluator = ConditionEvaluator()
    ): Pair<GameState, List<com.wingedsheep.engine.core.GameEvent>> {
        if (createdTokenIds.isEmpty() || cardRegistry == null) return state to emptyList()

        var newState = state
        val events = mutableListOf<com.wingedsheep.engine.core.GameEvent>()

        for (active in ActiveReplacements.all(state)) {
            val entityId = active.sourceId
            val effect = active.effect
            if (effect !is CreateAdditionalToken) continue
            val event = effect.appliesTo
            if (event !is SdkGameEvent.TokenCreationEvent) continue

            if (!controllerMatches(event.controller, active.controllerId, tokenControllerId)) continue

            // The replacement applies only if at least one of the just-created tokens
            // matches the event's token filter (e.g. "artifact tokens"). A null filter
            // means "any token". Match against base+projected state of the created tokens.
            val filter = event.tokenFilter
            val anyMatch = if (filter == null) {
                true
            } else {
                createdTokenIds.any { tokenId ->
                    predicateEvaluator.matches(
                        state, state.projectedState, tokenId, filter,
                        PredicateContext(controllerId = tokenControllerId)
                    )
                }
            }
            if (!anyMatch) continue

            // Extra gates on the rider (CR 614). Evaluated with the *creating* player as the
            // controller — the player the event happens to, matching
            // `ReplacementEffect.restrictions` — and with the rider's own permanent as the
            // source, so a source-relative gate resolves. That is what puts Case of the
            // Pilfered Proof's Clue rider behind its solved designation (CR 702.169b).
            if (effect.restrictions.isNotEmpty()) {
                val restrictionContext = EffectContext(
                    sourceId = entityId,
                    controllerId = tokenControllerId
                )
                val allHold = effect.restrictions.all { restriction ->
                    conditionEvaluator.evaluate(state, restriction, restrictionContext)
                }
                if (!allHold) continue
            }

            val cardDef = cardRegistry.getCard(effect.additionalTokenType) ?: continue
            val tapped = effect.inheritTapped && originalTapped

            repeat(
                com.wingedsheep.engine.core.GameLimits.cappedTokenCount(
                    effect.additionalTokenCount, "additional tokens"
                )
            ) {
                val (tokenId, stateWithId) = newState.newEntity()
                newState = stateWithId

                val tokenComponent = CardComponent(
                    cardDefinitionId = effect.additionalTokenType,
                    name = effect.additionalTokenType,
                    manaCost = ManaCost.ZERO,
                    typeLine = cardDef.typeLine,
                    baseStats = cardDef.creatureStats,
                    baseKeywords = cardDef.keywords,
                    // Tokens have no mana cost, so a colored token's printed color lives in
                    // its color indicator (CR 204), stored as colorIdentityOverride. Fall
                    // back to mana-cost-derived colors for tokens without an override.
                    colors = cardDef.colorIdentityOverride ?: cardDef.colors,
                    ownerId = tokenControllerId,
                    imageUri = cardDef.metadata.imageUri
                )

                var tokenContainer = ComponentContainer.of(
                    tokenComponent,
                    TokenComponent,
                    ControllerComponent(tokenControllerId),
                    SummoningSicknessComponent,
                    EnteredThisTurnComponent
                )
                if (tapped) tokenContainer = tokenContainer.with(TappedComponent)
                if (staticAbilityHandler != null) {
                    tokenContainer = staticAbilityHandler.addContinuousEffectComponent(tokenContainer, cardDef)
                    tokenContainer = staticAbilityHandler.addReplacementEffectComponent(tokenContainer, cardDef)
                }

                newState = newState.withEntity(tokenId, tokenContainer)
                newState = com.wingedsheep.engine.handlers.effects.BattlefieldEntry
                    .place(newState, tokenControllerId, tokenId)
                // Honor global "[filter] enter tapped" replacements on the added token too.
                newState = com.wingedsheep.engine.handlers.effects.EnterTappedReplacements
                    .applyCreatedTokenEntryTap(
                        newState, tokenId, tokenControllerId, definedTapped = tapped,
                    )

                events.add(
                    ZoneChangeEvent(
                        entityId = tokenId,
                        entityName = effect.additionalTokenType,
                        fromZone = null,
                        toZone = Zone.BATTLEFIELD,
                        ownerId = tokenControllerId,
                        oldObject = null,
                        newObject = newState.objectRef(tokenId)
                    )
                )
            }
        }

        return newState to events
    }

    /**
     * Check if any permanent controlled by the token creator has a token-creation
     * replacement effect that applies — [ReplaceTokenCreationWithAttachedCopy] (e.g.,
     * Mirrormind Crown, Moonlit Meditation) or [ReplaceTokenCreationWithChoiceOfTokens]
     * (e.g., Jinnie Fay, Jetmir's Second).
     *
     * @return A paused EffectResult if a replacement decision is needed, or null
     */
    fun checkReplacement(
        state: GameState,
        effect: Effect,
        context: EffectContext,
        tokenCount: Int,
        tokenControllerId: EntityId,
        cardRegistry: CardRegistry? = null,
        staticAbilityHandler: StaticAbilityHandler? = null
    ): EffectResult? {
        if (tokenCount <= 0) return null

        val controllerId = tokenControllerId

        for (entityId in state.getBattlefield()) {
            // Already asked (and answered) about this exact source for this exact re-invocation —
            // see EffectContext.declinedTokenReplacementSourceIds. Without this, re-executing the
            // original effect after a decline would find the same source and offer it again.
            if (entityId in context.declinedTokenReplacementSourceIds) continue

            val container = state.getEntity(entityId) ?: continue
            val entityController = container.get<ControllerComponent>()?.playerId ?: continue
            if (entityController != controllerId) continue

            val replacementComponent = container.get<ReplacementEffectSourceComponent>() ?: continue

            for (re in replacementComponent.replacementEffects) {
                when (re) {
                    is ReplaceTokenCreationWithAttachedCopy -> {
                        // Check once-per-turn
                        if (re.oncePerTurn && container.has<TokenReplacementOfferedThisTurnComponent>()) continue

                        // Check the source is attached to something (Aura/Equipment that fell off
                        // or never attached can't fire). Attachment-type validation is enforced at
                        // cast/attach time by auraTarget / equipmentTarget — no re-check here.
                        val attachedTo = container.get<AttachedToComponent>() ?: continue
                        val attachedContainer = state.getEntity(attachedTo.targetId) ?: continue
                        val attachedCard = attachedContainer.get<CardComponent>() ?: continue

                        val cardName = container.get<CardComponent>()?.name ?: "Source"

                        // Mark as offered this turn (prevents re-offering on decline)
                        var newState = state.withEntity(entityId, container.with(TokenReplacementOfferedThisTurnComponent))

                        if (re.optional) {
                            val prompt = "Use $cardName? Create ${if (tokenCount == 1) "a token that's a copy" else "$tokenCount tokens that are copies"} of ${attachedCard.name} instead?"

                            val decision = { decisionId: String -> YesNoDecision(
                                id = decisionId,
                                playerId = controllerId,
                                prompt = prompt,
                                context = DecisionContext(
                                    sourceId = entityId,
                                    sourceName = cardName,
                                    phase = DecisionPhase.RESOLUTION
                                )
                            ) }

                            val continuation = TokenCreationReplacementContinuation(
                                sourceId = entityId,
                                attachedPermanentId = attachedTo.targetId,
                                originalEffect = effect,
                                tokenCount = tokenCount,
                                effectContext = context
                            )

                            return EffectResult.from(newState.suspendForDecision(decision, continuation, emptyList()))
                        } else {
                            // Mandatory replacement — create copies directly
                            return createAttachedPermanentCopies(
                                newState, attachedTo.targetId, controllerId, tokenCount,
                                cardRegistry, staticAbilityHandler
                            )
                        }
                    }

                    is ReplaceTokenCreationWithChoiceOfTokens -> {
                        val cardName = container.get<CardComponent>()?.name ?: "Source"
                        val plural = tokenCount != 1

                        // Option 0 is "don't replace" only when optional; every other option
                        // maps positionally onto re.templates (see TokenCreationChoiceContinuation).
                        val templateOptions = re.templates.map { template ->
                            "Create ${if (plural) "$tokenCount" else "a"} ${template.description}${if (plural) "s" else ""} instead"
                        }
                        val options = if (re.optional) {
                            listOf("Create the original ${if (plural) "tokens" else "token"}") + templateOptions
                        } else {
                            templateOptions
                        }

                        val decision = { decisionId: String -> ChooseOptionDecision(
                            id = decisionId,
                            playerId = controllerId,
                            prompt = "$cardName: choose what ${if (plural) "tokens" else "token"} to create",
                            context = DecisionContext(
                                sourceId = entityId,
                                sourceName = cardName,
                                phase = DecisionPhase.RESOLUTION
                            ),
                            options = options
                        ) }

                        val continuation = TokenCreationChoiceContinuation(
                            sourceId = entityId,
                            originalEffect = effect,
                            tokenCount = tokenCount,
                            effectContext = context,
                            templates = re.templates,
                            optional = re.optional,
                            tokenControllerId = controllerId
                        )

                        return EffectResult.from(state.suspendForDecision(decision, continuation, emptyList()))
                    }

                    else -> continue
                }
            }
        }
        return null
    }

    /**
     * Resolve a [TokenCreationChoiceContinuation] after the player answers its
     * [ChooseOptionDecision]. `chosenIndex == 0` (only offered when [TokenCreationChoiceContinuation.optional])
     * means "create the original tokens unchanged" — the caller re-executes
     * [TokenCreationChoiceContinuation.originalEffect] in that case; any other index selects
     * `templates[chosenIndex - (if optional 1 else 0)]`.
     *
     * @return the chosen [AlternateTokenTemplate], or `null` if the player declined (only
     *         possible when `optional`).
     */
    fun resolveChoiceContinuationOption(
        templates: List<AlternateTokenTemplate>,
        optional: Boolean,
        chosenIndex: Int
    ): AlternateTokenTemplate? {
        if (optional && chosenIndex == 0) return null
        val templateIndex = if (optional) chosenIndex - 1 else chosenIndex
        require(templateIndex in templates.indices) {
            "Invalid token-creation choice index: $chosenIndex (optional=$optional, ${templates.size} templates)"
        }
        return templates[templateIndex]
    }

    /**
     * Riders read off the *original* token-creating effect that still apply to the substitute
     * tokens created by [ReplaceTokenCreationWithChoiceOfTokens], per the printed ruling:
     * "Anything else specified in the effect creating the tokens (such as tapped, attacking,
     * 'That token gains haste,' or 'Exile that token at end of combat') still applies." Everything
     * else about the original effect (its printed P/T, colors, creature types, keywords, granted
     * abilities, initial counters) is intentionally dropped — the chosen [AlternateTokenTemplate]
     * is the *entire* printable shape of the substitute tokens.
     */
    private data class TokenCreationRiders(
        val tapped: Boolean = false,
        val attacking: Boolean = false,
        val exileAtStep: Step? = null,
        val sacrificeAtStep: Step? = null,
    )

    private fun ridersFrom(effect: Effect): TokenCreationRiders = when (effect) {
        is CreateTokenEffect -> TokenCreationRiders(
            tapped = effect.tapped,
            attacking = effect.attacking,
            exileAtStep = effect.exileAtStep,
            sacrificeAtStep = effect.sacrificeAtStep,
        )
        is CreateTokenCopyOfTargetEffect -> TokenCreationRiders(
            tapped = effect.tapped,
            attacking = effect.attacking,
            exileAtStep = effect.exileAtStep,
        )
        is CreatePredefinedTokenEffect -> TokenCreationRiders(tapped = effect.tapped)
        else -> TokenCreationRiders()
    }

    /**
     * Create [count] tokens matching [template] — the substitute-token half of
     * [ReplaceTokenCreationWithChoiceOfTokens] — preserving the "still applies" riders read off
     * [originalEffect] by [ridersFrom] (tapped / attacking / exile-at-step / sacrifice-at-step).
     *
     * Mirrors the relevant slice of `CreateTokenExecutor.createTokensFor`, but builds the token's
     * printed characteristics entirely from [template] rather than a [com.wingedsheep.sdk.scripting.effects.CreateTokenEffect] —
     * no keywords, granted abilities, or counters carry over from whatever effect was replaced,
     * per the printed ruling.
     */
    fun createChosenTemplateTokens(
        state: GameState,
        template: AlternateTokenTemplate,
        originalEffect: Effect,
        context: EffectContext,
        count: Int,
        controllerId: EntityId,
        staticAbilityHandler: StaticAbilityHandler? = null,
        cardRegistry: CardRegistry? = null,
    ): EffectResult {
        val riders = ridersFrom(originalEffect)
        val cappedCount = com.wingedsheep.engine.core.GameLimits.cappedTokenCount(count, "tokens")

        val defaultName = "${template.creatureTypes.joinToString(" ")} Token"
        val tokenName = template.name ?: defaultName

        var newState = state
        val createdTokens = mutableListOf<EntityId>()
        val events = mutableListOf<com.wingedsheep.engine.core.GameEvent>()

        repeat(cappedCount) {
            val (tokenId, stateWithId) = newState.newEntity()
            newState = stateWithId
            createdTokens.add(tokenId)

            val tokenComponent = CardComponent(
                cardDefinitionId = "token:${template.creatureTypes.joinToString("-")}",
                name = tokenName,
                manaCost = ManaCost.ZERO,
                typeLine = TypeLine.parse("Creature - ${template.creatureTypes.joinToString(" ")}"),
                baseStats = CreatureStats(template.power, template.toughness),
                baseKeywords = template.keywords,
                colors = template.colors,
                ownerId = controllerId,
                imageUri = template.imageUri
            )

            val components = mutableListOf<Component>(
                tokenComponent,
                TokenComponent,
                ControllerComponent(controllerId),
                SummoningSicknessComponent,
                EnteredThisTurnComponent
            )
            if (riders.tapped) components.add(TappedComponent)
            if (riders.attacking) {
                val defenderId = TargetResolutionUtils.resolveDefendingPlayer(context, newState)
                    ?: newState.getOpponents(controllerId).firstOrNull()
                if (defenderId != null) components.add(AttackingComponent(defenderId))
            }

            var container = ComponentContainer.of(*components.toTypedArray())
            if (staticAbilityHandler != null) {
                container = staticAbilityHandler.addContinuousEffectComponent(container)
                container = staticAbilityHandler.addReplacementEffectComponent(container)
            }
            newState = newState.withEntity(tokenId, container)

            newState = com.wingedsheep.engine.handlers.effects.BattlefieldEntry
                .place(newState, controllerId, tokenId)
            newState = EnterTappedReplacements.applyCreatedTokenEntryTap(
                newState, tokenId, controllerId,
                definedTapped = riders.tapped, attacking = riders.attacking,
            )

            events.add(
                ZoneChangeEvent(
                    entityId = tokenId,
                    entityName = tokenName,
                    fromZone = null,
                    toZone = Zone.BATTLEFIELD,
                    ownerId = controllerId,
                    oldObject = null,
                    newObject = newState.objectRef(tokenId)
                )
            )
        }

        // "Enters with counters" replacements from other battlefield permanents (e.g. Gev,
        // Scaled Scorch) apply to the substitute tokens exactly as they would to the originals.
        for (tokenId in createdTokens) {
            val (nextState, counterEvents) = EntersWithReplacements.applyGlobal(
                newState, tokenId, controllerId, cardRegistry
            )
            newState = nextState
            events.addAll(counterEvents)
        }

        val sourceId = context.sourceId ?: context.controllerId
        val sourceName = state.getEntity(sourceId)?.get<CardComponent>()?.name ?: "Unknown"

        riders.exileAtStep?.let { step ->
            for (tokenId in createdTokens) {
                val (delayedTriggerId, stateWithRoutingId) = newState.newRoutingId()
                newState = stateWithRoutingId
                newState = newState.addDelayedTrigger(
                    DelayedTriggeredAbility(
                        id = delayedTriggerId,
                        effect = MoveToZoneEffect(EffectTarget.SpecificEntity(tokenId), Zone.EXILE),
                        fireAtStep = step,
                        sourceId = sourceId,
                        objectReferences = context.objectReferences,
                        sourceName = sourceName,
                        controllerId = controllerId
                    )
                )
            }
        }

        riders.sacrificeAtStep?.let { step ->
            for (tokenId in createdTokens) {
                val (delayedTriggerId, stateWithRoutingId) = newState.newRoutingId()
                newState = stateWithRoutingId
                newState = newState.addDelayedTrigger(
                    DelayedTriggeredAbility(
                        id = delayedTriggerId,
                        effect = SacrificeTargetEffect(EffectTarget.SpecificEntity(tokenId)),
                        fireAtStep = step,
                        sourceId = sourceId,
                        objectReferences = context.objectReferences,
                        sourceName = sourceName,
                        controllerId = controllerId
                    )
                )
            }
        }

        return EffectResult(state = newState, events = events)
    }

    /**
     * Create N token copies of the attached permanent (equipped creature, enchanted
     * permanent, etc.).
     *
     * Per the printed rulings (Mirrormind Crown, Moonlit Meditation), the tokens copy
     * exactly what was printed on the attached permanent. That includes printed
     * "enters with N counters" replacement effects (e.g., Burdened Stoneback's "this
     * creature enters with two -1/-1 counters"), applied via
     * [EntersWithReplacements.applyOnEntry]. Summoning sickness is added
     * only when the copy is itself a creature (CR 302.6).
     */
    fun createAttachedPermanentCopies(
        state: GameState,
        attachedPermanentId: EntityId,
        controllerId: EntityId,
        count: Int,
        cardRegistry: CardRegistry? = null,
        staticAbilityHandler: StaticAbilityHandler? = null
    ): EffectResult {
        val attachedContainer = state.getEntity(attachedPermanentId)
            ?: return EffectResult.success(state)

        val attachedCard = attachedContainer.get<CardComponent>()
            ?: return EffectResult.success(state)

        var newState = state
        val events = mutableListOf<com.wingedsheep.engine.core.GameEvent>()

        // Same structural cap as CreateTokenExecutor: copies are full entities too.
        val cappedCount = com.wingedsheep.engine.core.GameLimits.cappedTokenCount(count, "token copies")

        repeat(cappedCount) {
            val (tokenId, stateWithId) = newState.newEntity()
            newState = stateWithId
            val tokenCard = attachedCard.copy(ownerId = controllerId)

            val components = mutableListOf<Component>(
                tokenCard,
                TokenComponent,
                ControllerComponent(controllerId),
                EnteredThisTurnComponent
            )
            // Summoning sickness only applies to creatures (CR 302.6). An artifact or
            // enchantment token copy doesn't get it; a creature (or artifact-creature)
            // copy does.
            if (tokenCard.typeLine.isCreature) {
                components.add(SummoningSicknessComponent)
            }

            var container = ComponentContainer.of(*components.toTypedArray())
            if (staticAbilityHandler != null) {
                container = staticAbilityHandler.addContinuousEffectComponent(container)
                container = staticAbilityHandler.addReplacementEffectComponent(container)
            }
            newState = newState.withEntity(tokenId, container)
            newState = com.wingedsheep.engine.handlers.effects.BattlefieldEntry
                .place(newState, controllerId, tokenId)
            // Honor global "[filter] enter tapped" replacements on the copy too.
            newState = com.wingedsheep.engine.handlers.effects.EnterTappedReplacements
                .applyCreatedTokenEntryTap(newState, tokenId, controllerId)

            // Apply the attached permanent's printed enters-with-counters replacement
            // effects (and any global ones from other permanents).
            if (cardRegistry != null) {
                val (afterCounters, counterEvents) = EntersWithReplacements.applyOnEntry(
                    newState, tokenId, controllerId, cardRegistry
                )
                newState = afterCounters
                events.addAll(counterEvents)

                // CR 306.5b: an Aura can enchant a planeswalker, so a copy of the attached
                // permanent may be one — it enters with the copied printed loyalty (a copiable
                // value, CR 707.2) or state-based actions (CR 704.5i) bin it on arrival.
                val (afterLoyalty, loyaltyEvents) = com.wingedsheep.engine.handlers.effects
                    .ZoneMovementUtils.applyIntrinsicEntryCountersIfNeeded(
                        newState, tokenId, controllerId, cardRegistry
                    )
                newState = afterLoyalty
                events.addAll(loyaltyEvents)
            }

            events.add(
                ZoneChangeEvent(
                    entityId = tokenId,
                    entityName = tokenCard.name,
                    fromZone = null,
                    toZone = Zone.BATTLEFIELD,
                    ownerId = controllerId,
                    oldObject = null,
                    newObject = newState.objectRef(tokenId)
                )
            )
        }

        return EffectResult.success(newState, events)
    }
}
