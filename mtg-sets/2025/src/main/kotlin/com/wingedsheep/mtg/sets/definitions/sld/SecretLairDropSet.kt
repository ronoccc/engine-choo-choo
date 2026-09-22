package com.wingedsheep.mtg.sets.definitions.sld

import com.wingedsheep.mtg.sets.discovery.CardDiscovery
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.MtgSet
import com.wingedsheep.sdk.model.Printing

/**
 * Secret Lair Drop
 *
 * Set Code: SLD
 * Release Date: ongoing — this scaffold's `releaseDate` tracks the earliest card placed here
 * (Dr. Eggman, Secret Lair x Sonic the Hedgehog, 2025-07-14). Secret Lair drops release
 * continuously under one evergreen set code, so unlike a fixed expansion this date is not "the"
 * set's release date, only the anchor for the first card this corpus implements from it.
 */
object SecretLairDropSet : MtgSet {

    override val code = "SLD"
    override val displayName = "Secret Lair Drop"
    override val releaseDate = "2025-07-14"

    override val cards: List<CardDefinition> by lazy {
        CardDiscovery.findIn(CARDS_PACKAGE)
    }

    override val basicLands: List<CardDefinition> by lazy {
        CardDiscovery.findBasicLandsIn(CARDS_PACKAGE, code)
    }

    override val printings: List<Printing> by lazy {
        CardDiscovery.findPrintingsIn(CARDS_PACKAGE)
    }

    private const val CARDS_PACKAGE = "com.wingedsheep.mtg.sets.definitions.sld.cards"
}
