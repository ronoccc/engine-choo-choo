package com.wingedsheep.mtg.sets.definitions.clu

import com.wingedsheep.mtg.sets.discovery.CardDiscovery
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.MtgSet
import com.wingedsheep.sdk.model.Printing

/**
 * Ravnica: Clue Edition (2024)
 *
 * Set Code: CLU
 * Release Date: February 23, 2024
 *
 * Scaffolded to hold the canonical [CardDefinition] for cards whose earliest real printing is CLU
 * (e.g. Conclave Evangelist).
 */
object RavnicaClueEditionSet : MtgSet {

    override val code = "CLU"
    override val displayName = "Ravnica: Clue Edition"
    override val releaseDate = "2024-02-23"
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

    private const val CARDS_PACKAGE = "com.wingedsheep.mtg.sets.definitions.clu.cards"
}
