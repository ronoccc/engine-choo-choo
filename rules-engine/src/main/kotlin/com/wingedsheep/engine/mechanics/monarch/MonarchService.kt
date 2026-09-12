package com.wingedsheep.engine.mechanics.monarch

import com.wingedsheep.engine.core.GameEvent
import com.wingedsheep.engine.core.MonarchChangedEvent
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.PlayerComponent
import com.wingedsheep.sdk.model.EntityId

/**
 * The one place [GameState.monarchId] changes (CR 716, "the monarch").
 *
 * Mirrors [com.wingedsheep.engine.mechanics.daynight.DayNightService]: every writer routes through
 * [become] so the designation can never drift or double-fire its event. Two writers exist —
 * [com.wingedsheep.engine.handlers.effects.player.BecomeMonarchExecutor] behind a card's own "you
 * become the monarch" effect, and the CR 716.6 combat-damage transfer synthesized in
 * [com.wingedsheep.engine.event.MonarchAbilities] / detected in
 * `TriggerDetector.detectMonarchCombatDamageTrigger` — both resolve to the same
 * [com.wingedsheep.sdk.scripting.effects.BecomeMonarchEffect], so there is really only one call site.
 *
 * Unlike [com.wingedsheep.engine.mechanics.citysblessing.CitysBlessingService.has] there is no "live,
 * before-the-marker-is-written" read to worry about: nothing continuously qualifies a player for the
 * monarchy the way ascend continuously qualifies a permanent for the city's blessing. [has] is a
 * plain field read.
 */
object MonarchService {

    /** Is [playerId] the monarch right now? A plain read of [GameState.monarchId]. */
    fun has(state: GameState, playerId: EntityId): Boolean = state.monarchId == playerId

    /**
     * [playerId] becomes the monarch (CR 716.1), attributing the change to [sourceName].
     *
     * A no-op — no state change, no event — when [playerId] already holds it (CR 716.2: "if a player
     * would become the monarch while already the monarch, nothing happens"). Otherwise removes the
     * designation from whoever held it (if anyone) and emits one [MonarchChangedEvent] carrying both
     * the old and new holder.
     */
    fun become(
        state: GameState,
        playerId: EntityId,
        sourceName: String
    ): Pair<GameState, List<GameEvent>> {
        val current = state.monarchId
        if (current == playerId) return state to emptyList()

        val newName = state.getEntity(playerId)?.get<PlayerComponent>()?.name ?: "Player"
        val oldName = current?.let { state.getEntity(it)?.get<PlayerComponent>()?.name }

        val newState = state.copy(monarchId = playerId)
        val event = MonarchChangedEvent(
            newMonarchId = playerId,
            newMonarchName = newName,
            oldMonarchId = current,
            oldMonarchName = oldName,
            sourceName = sourceName
        )
        return newState to listOf(event)
    }
}
