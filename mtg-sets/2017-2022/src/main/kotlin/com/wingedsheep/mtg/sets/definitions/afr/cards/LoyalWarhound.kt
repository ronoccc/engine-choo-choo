package com.wingedsheep.mtg.sets.definitions.afr.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.SearchDestination

/**
 * Loyal Warhound
 * {1}{W}
 * Creature — Dog
 * 3/1
 * Vigilance
 * When this creature enters, if an opponent controls more lands than you, search your library
 * for a basic Plains card, put it onto the battlefield tapped, then shuffle.
 *
 * The enters ability carries an intervening-if clause (CR 603.4) modelled as
 * [Conditions.OpponentControlsMoreLands] — checked both as the trigger goes on the stack and
 * again as it resolves. Unlike Claim Jumper's later "you may" version, this search is mandatory
 * and single-shot: [Patterns.Library.searchLibrary] for a basic Plains straight onto the
 * battlefield tapped, shuffling afterward regardless of whether a Plains was found.
 */
val LoyalWarhound = card("Loyal Warhound") {
    manaCost = "{1}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dog"
    oracleText = "Vigilance\n" +
        "When this creature enters, if an opponent controls more lands than you, search your " +
        "library for a basic Plains card, put it onto the battlefield tapped, then shuffle."
    power = 3
    toughness = 1

    keywords(Keyword.VIGILANCE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        interveningIf = Conditions.OpponentControlsMoreLands
        effect = Patterns.Library.searchLibrary(
            filter = GameObjectFilter.BasicLand.withSubtype(Subtype.PLAINS),
            count = 1,
            destination = SearchDestination.BATTLEFIELD,
            entersTapped = true,
            shuffleAfter = true
        )
        description = "When this creature enters, if an opponent controls more lands than you, " +
            "search your library for a basic Plains card, put it onto the battlefield tapped, " +
            "then shuffle."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "23"
        artist = "Dmitry Burmak"
        flavorText = "A paladin's steed is a celestial spirit in animal form."
        imageUri = "https://cards.scryfall.io/normal/front/f/4/f408c5e4-e18d-4e4d-959a-f41df4c3019c.jpg?1783926530"
    }
}
