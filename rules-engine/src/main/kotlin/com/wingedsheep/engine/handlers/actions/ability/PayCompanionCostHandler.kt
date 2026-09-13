package com.wingedsheep.engine.handlers.actions.ability

import com.wingedsheep.engine.core.EngineServices
import com.wingedsheep.engine.core.ExecutionResult
import com.wingedsheep.engine.core.GameEvent
import com.wingedsheep.engine.core.ManaSpentEvent
import com.wingedsheep.engine.core.PayCompanionCost
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.core.ZoneChangeEvent
import com.wingedsheep.engine.core.tap
import com.wingedsheep.engine.handlers.actions.ActionHandler
import com.wingedsheep.engine.mechanics.mana.ManaPool
import com.wingedsheep.engine.mechanics.mana.ManaSolver
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Zone
import kotlin.reflect.KClass

/**
 * Handler for the [PayCompanionCost] special action (CR 702.139a, 116.2g).
 *
 * A companion revealed pregame sits in [Zone.COMPANION] (see [com.wingedsheep.engine.core.GameInitializer])
 * until its controller pays a fixed {3} to put it into their hand. Per CR 116.2g / 702.139a this
 * is a special action: it doesn't use the stack, can't be responded to, and is only available any
 * time the player has priority, the stack is empty, and it's a main phase of their turn. There is
 * no card to name in the action itself — a player has at most one card in their companion zone, so
 * this handler resolves it from there. Once moved to hand the zone is empty, which is what makes
 * the action "once during the game" (CR 702.139a) without needing a separate used-once flag: an
 * empty companion zone offers nothing to [com.wingedsheep.engine.legalactions.enumerators.PayCompanionCostEnumerator].
 *
 * Mirrors [ForetellCardHandler]'s mana-payment shape (drain the pool first, then solve/tap the
 * remainder), adapted to a fixed {3} generic cost and a companion-zone-to-hand move instead of a
 * hand-to-exile one.
 */
class PayCompanionCostHandler(
    private val cardRegistry: CardRegistry,
    private val manaSolver: ManaSolver,
    private val manaAbilitySideEffectExecutor: com.wingedsheep.engine.mechanics.mana.ManaAbilitySideEffectExecutor,
) : ActionHandler<PayCompanionCost> {
    override val actionType: KClass<PayCompanionCost> = PayCompanionCost::class

    /** The fixed cost to put a companion into hand, per CR 702.139a. */
    private val cost: ManaCost = ManaCost.parse("{3}")

    private fun companionCardId(state: GameState, playerId: com.wingedsheep.sdk.model.EntityId) =
        state.getZone(ZoneKey(playerId, Zone.COMPANION)).firstOrNull()

    override fun validate(state: GameState, action: PayCompanionCost): String? {
        // CR 116.2g / 702.139a: any time you have priority, the stack is empty, during a main
        // phase of your turn.
        if (!state.hasPriority(action.playerId)) {
            return "You don't have priority"
        }
        if (!state.step.isMainPhase) {
            return "You can only put your companion into your hand during a main phase"
        }
        if (state.stack.isNotEmpty()) {
            return "You can only put your companion into your hand when the stack is empty"
        }
        if (!state.isActiveTurnFor(action.playerId)) {
            return "You can only put your companion into your hand on your turn"
        }
        if (companionCardId(state, action.playerId) == null) {
            return "You have no companion waiting outside the game"
        }
        if (action.paymentStrategy is PaymentStrategy.Explicit) {
            for (sourceId in action.paymentStrategy.manaAbilitiesToActivate) {
                val sourceContainer = state.getEntity(sourceId)
                    ?: return "Mana source not found: $sourceId"
                if (sourceContainer.has<TappedComponent>()) {
                    return "Mana source is already tapped: $sourceId"
                }
            }
        } else if (!manaSolver.canPay(state, action.playerId, cost)) {
            return "Not enough mana to put your companion into your hand"
        }
        return null
    }

    override fun execute(state: GameState, action: PayCompanionCost): ExecutionResult {
        val cardId = companionCardId(state, action.playerId)
            ?: return ExecutionResult.error(state, "You have no companion waiting outside the game")
        val cardComponent = state.getEntity(cardId)?.get<CardComponent>()
            ?: return ExecutionResult.error(state, "Not a card")

        var currentState = state
        val events = mutableListOf<GameEvent>()

        // Pay the fixed {3} cost — drain mana pool first, then tap lands for the remainder.
        // Mirrors ForetellCardHandler's payment shape exactly.
        val poolComponent = currentState.getEntity(action.playerId)?.get<ManaPoolComponent>()
            ?: ManaPoolComponent()
        val pool = ManaPool(
            white = poolComponent.white,
            blue = poolComponent.blue,
            black = poolComponent.black,
            red = poolComponent.red,
            green = poolComponent.green,
            colorless = poolComponent.colorless
        )
        val partialResult = pool.payPartial(cost)
        val poolAfterPayment = partialResult.newPool
        val remainingCost = partialResult.remainingCost
        val manaSpentFromPool = partialResult.manaSpent

        var whiteSpent = manaSpentFromPool.white
        var blueSpent = manaSpentFromPool.blue
        var blackSpent = manaSpentFromPool.black
        var redSpent = manaSpentFromPool.red
        var greenSpent = manaSpentFromPool.green
        var colorlessSpent = manaSpentFromPool.colorless

        currentState = currentState.updateEntity(action.playerId) { c ->
            c.with(
                ManaPoolComponent(
                    white = poolAfterPayment.white,
                    blue = poolAfterPayment.blue,
                    black = poolAfterPayment.black,
                    red = poolAfterPayment.red,
                    green = poolAfterPayment.green,
                    colorless = poolAfterPayment.colorless
                )
            )
        }

        if (!remainingCost.isEmpty()) {
            if (action.paymentStrategy is PaymentStrategy.Explicit) {
                for (sourceId in action.paymentStrategy.manaAbilitiesToActivate) {
                    val (tappedState, tapEvent) = tap(currentState, sourceId)
                    currentState = tappedState
                    tapEvent?.let(events::add)
                }
            } else {
                val solution = manaSolver.solve(currentState, action.playerId, remainingCost, 0)
                    ?: return ExecutionResult.error(state, "Not enough mana to put your companion into your hand")
                val (stateAfterTaps, tapEvents) = manaAbilitySideEffectExecutor
                    .tapSourcesWithSideEffects(currentState, solution, action.playerId)
                currentState = stateAfterTaps
                events.addAll(tapEvents)

                for ((_, production) in solution.manaProduced) {
                    when (production.color) {
                        Color.WHITE -> whiteSpent++
                        Color.BLUE -> blueSpent++
                        Color.BLACK -> blackSpent++
                        Color.RED -> redSpent++
                        Color.GREEN -> greenSpent++
                        null -> colorlessSpent += production.colorless
                    }
                }
            }
        }

        events.add(
            ManaSpentEvent(
                playerId = action.playerId,
                reason = "Put ${cardComponent.name} into hand from outside the game",
                white = whiteSpent,
                blue = blueSpent,
                black = blackSpent,
                red = redSpent,
                green = greenSpent,
                colorless = colorlessSpent
            )
        )

        // Move the card from the companion zone to hand (CR 702.139a). Once in hand it's an
        // ordinary card — CR 702.139c: "it remains in the game until the game ends."
        val companionZone = ZoneKey(action.playerId, Zone.COMPANION)
        val handZone = ZoneKey(action.playerId, Zone.HAND)
        currentState = currentState.removeFromZone(companionZone, cardId)
        val oldObjectRef = currentState.objectRef(cardId)
        currentState = currentState.addToZone(handZone, cardId)
        events.add(
            ZoneChangeEvent(
                entityId = cardId,
                entityName = cardComponent.name,
                fromZone = Zone.COMPANION,
                toZone = Zone.HAND,
                ownerId = action.playerId,
                oldObject = oldObjectRef,
                newObject = currentState.objectRef(cardId)
            )
        )

        currentState = currentState.tick()

        // Special action — no stack, no priority change, no triggers to detect on a companion
        // simply arriving in hand.
        return ExecutionResult.success(currentState, events)
    }

    companion object {
        fun create(services: EngineServices): PayCompanionCostHandler {
            return PayCompanionCostHandler(
                services.cardRegistry,
                services.manaSolver,
                services.manaAbilitySideEffectExecutor,
            )
        }
    }
}
