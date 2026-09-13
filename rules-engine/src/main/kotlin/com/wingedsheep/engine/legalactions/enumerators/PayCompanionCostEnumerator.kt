package com.wingedsheep.engine.legalactions.enumerators

import com.wingedsheep.engine.core.PayCompanionCost
import com.wingedsheep.engine.legalactions.ActionEnumerator
import com.wingedsheep.engine.legalactions.EnumerationContext
import com.wingedsheep.engine.legalactions.LegalAction
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Zone

/**
 * Enumerates the "pay {3}, put your companion into your hand" special action (CR 702.139a,
 * 116.2g).
 *
 * Offered only at sorcery speed ([EnumerationContext.canPlaySorcerySpeed] already encodes "you
 * have priority, the stack is empty, during a main phase of your turn" — exactly CR 116.2g's
 * gate) and only while the player's [Zone.COMPANION] holds a card. Once that card moves to hand
 * the zone empties and this enumerator stops offering the action — the mechanism behind
 * "once during the game" (CR 702.139a).
 */
class PayCompanionCostEnumerator : ActionEnumerator {

    /** The fixed cost to put a companion into hand, per CR 702.139a. */
    private val cost: ManaCost = ManaCost.parse("{3}")

    override fun enumerate(context: EnumerationContext): List<LegalAction> {
        if (!context.canPlaySorcerySpeed) return emptyList()

        val state = context.state
        val playerId = context.playerId
        val cardId = state.getZone(ZoneKey(playerId, Zone.COMPANION)).firstOrNull() ?: return emptyList()
        val cardComponent = state.getEntity(cardId)?.get<CardComponent>() ?: return emptyList()

        val canAfford = context.manaSolver.canPay(
            state, playerId, cost, precomputedSources = context.availableManaSources
        )
        val autoTapPreview = if (context.skipAutoTapPreview) null else {
            context.manaSolver.solve(
                state, playerId, cost, precomputedSources = context.availableManaSources
            )?.sources?.map { it.entityId }
        }
        return listOf(
            LegalAction(
                actionType = "PayCompanionCost",
                description = "Put ${cardComponent.name} into your hand from outside the game",
                action = PayCompanionCost(playerId),
                affordable = canAfford,
                manaCostString = cost.toString(),
                autoTapPreview = autoTapPreview
            )
        )
    }
}
