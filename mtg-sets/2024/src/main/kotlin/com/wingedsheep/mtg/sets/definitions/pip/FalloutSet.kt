package com.wingedsheep.mtg.sets.definitions.pip

import com.wingedsheep.mtg.sets.discovery.CardDiscovery
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.MtgSet
import com.wingedsheep.sdk.model.Printing

/**
 * Fallout (2024)
 *
 * Four Commander preconstructed decks in the Fallout crossover, drafted into the Universes
 * Beyond card pool.
 *
 * Set Code: PIP
 * Release Date: March 8, 2024
 */
object FalloutSet : MtgSet {

    override val code = "PIP"
    override val displayName = "Fallout"
    override val releaseDate = "2024-03-08"
    override val sealedSupported = false
    override val incomplete = true

    override val cards: List<CardDefinition> by lazy {
        CardDiscovery.findIn(CARDS_PACKAGE)
    }

    override val basicLands: List<CardDefinition> by lazy {
        CardDiscovery.findBasicLandsIn(CARDS_PACKAGE, code)
    }

    override val printings: List<Printing> by lazy {
        CardDiscovery.findPrintingsIn(CARDS_PACKAGE)
    }

    private const val CARDS_PACKAGE = "com.wingedsheep.mtg.sets.definitions.pip.cards"
}
