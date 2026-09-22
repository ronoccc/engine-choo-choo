package com.wingedsheep.mtg.sets.definitions.acr

import com.wingedsheep.mtg.sets.discovery.CardDiscovery
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.MtgSet
import com.wingedsheep.sdk.model.Printing

/**
 * Assassin's Creed
 *
 * A Universes Beyond draft-innovation set released alongside Assassin's Creed-themed Commander
 * decks.
 *
 * Set Code: ACR
 * Release Date: July 5, 2024
 */
object AssassinsCreedSet : MtgSet {

    override val code = "ACR"
    override val displayName = "Assassin's Creed"
    override val releaseDate = "2024-07-05"
    override val sealedSupported = false
    override val incomplete = true

    override val cards: List<CardDefinition> by lazy {
        CardDiscovery.findIn(CARDS_PACKAGE)
    }

    override val printings: List<Printing> by lazy {
        CardDiscovery.findPrintingsIn(CARDS_PACKAGE)
    }

    private const val CARDS_PACKAGE = "com.wingedsheep.mtg.sets.definitions.acr.cards"
}
