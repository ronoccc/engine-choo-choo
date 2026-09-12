package com.wingedsheep.mtg.sets.definitions.j25

import com.wingedsheep.mtg.sets.discovery.CardDiscovery
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.MtgSet
import com.wingedsheep.sdk.model.Printing

/**
 * Foundations Jumpstart (2024)
 *
 * Set Code: J25
 * Release Date: November 15, 2024
 *
 * A Jumpstart-style supplemental product released alongside Foundations, contributing a handful
 * of new cards (Generous Pup among them) plus reprints for its themed half-decks. Scaffolded here
 * as the canonical home for those new cards, per [MtgSet]'s minimal shape (mirrors JumpstartSet).
 */
object FoundationsJumpstartSet : MtgSet {

    override val code = "J25"
    override val displayName = "Foundations Jumpstart"
    override val releaseDate = "2024-11-15"
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

    private const val CARDS_PACKAGE = "com.wingedsheep.mtg.sets.definitions.j25.cards"
}
