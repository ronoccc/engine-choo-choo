package com.wingedsheep.sdk.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Zone(val displayName: String) {
    @SerialName("Library") LIBRARY("a library"),
    @SerialName("Hand") HAND("a hand"),
    @SerialName("Battlefield") BATTLEFIELD("the battlefield"),
    @SerialName("Graveyard") GRAVEYARD("a graveyard"),
    @SerialName("Stack") STACK("the stack"),
    @SerialName("Exile") EXILE("exile"),
    @SerialName("Command") COMMAND("the command zone"),

    /**
     * A player's sideboard — the cards they own *outside the game* (CR 100.4, 400.11a). Strictly,
     * "outside the game is not a zone" (CR 400.11), but the engine models the sideboard as a
     * private per-player pseudo-zone so the "wish" mechanic (Burning Wish, Living Wish, …) can
     * reach it with the ordinary Gather → Select → Move pipeline instead of bespoke machinery.
     *
     * It is private to its owner (masked from opponents, like [LIBRARY]/[HAND]) and, consistent
     * with CR 400.11c ("cards outside the game can't be affected by spells or abilities"), nothing
     * but a wish effect should ever gather from it. In constructed it is an explicit list (≤15,
     * CR 100.4a); in Limited it is every card in the player's pool not in their deck (CR 100.4b).
     */
    @SerialName("Sideboard") SIDEBOARD("a sideboard"),

    /**
     * A player's revealed companion, sitting outside the game (CR 702.139a, 103.2b) after being
     * chosen pregame. Unlike [SIDEBOARD] this is public — a companion is *revealed*, so every
     * player knows its identity and whose it is — but like the sideboard it holds at most one
     * card per player and isn't part of that player's library/hand/battlefield. Holds zero cards
     * (no companion revealed, or none chosen) or exactly one; once its card is paid for and moved
     * to [HAND] (the CR 116.2g special action), it empties and stays empty for the rest of the
     * game — the natural way "once during the game" falls out of zone occupancy instead of a
     * separate used-once flag.
     */
    @SerialName("Companion") COMPANION("outside the game as a companion");

    val isPublic: Boolean
        get() = this in listOf(BATTLEFIELD, GRAVEYARD, STACK, EXILE, COMMAND, COMPANION)

    val isHidden: Boolean
        get() = this in listOf(LIBRARY, HAND, SIDEBOARD)

    val isShared: Boolean
        get() = this in listOf(BATTLEFIELD, STACK, EXILE)
}
