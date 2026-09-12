package com.wingedsheep.mtg.sets.definitions.ala.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.SearchDestination

/**
 * Naya Panorama
 * Land
 * {T}: Add {C}.
 * {1}, {T}, Sacrifice this land: Search your library for a basic Mountain, Forest, or Plains
 * card, put it onto the battlefield tapped, then shuffle.
 *
 * One of the ten Alara "Panorama" cycle lands (the color-pair-crossing five plus this
 * three-color trio). Unlike Evolving Wilds/Terramorphic Expanse, the fetched card is
 * restricted to the three specific basic land types named in the text rather than any basic
 * land — modeled as [GameObjectFilter.BasicLand] intersected with an `or` of the three
 * subtypes. The current Oracle wording (post-2010 templating) carries no life-gain clause.
 */
val NayaPanorama = card("Naya Panorama") {
    manaCost = ""
    colorIdentity = ""
    typeLine = "Land"
    oracleText = "{T}: Add {C}.\n{1}, {T}, Sacrifice this land: Search your library for a basic " +
        "Mountain, Forest, or Plains card, put it onto the battlefield tapped, then shuffle."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{1}"), Costs.Tap, Costs.SacrificeSelf)
        effect = Patterns.Library.searchLibrary(
            filter = GameObjectFilter.BasicLand.withSubtype("Mountain") or
                GameObjectFilter.BasicLand.withSubtype("Forest") or
                GameObjectFilter.BasicLand.withSubtype("Plains"),
            destination = SearchDestination.BATTLEFIELD,
            entersTapped = true,
            shuffleAfter = true
        )
        manaAbility = false
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "227"
        artist = "Hideaki Takamura"
        flavorText = "Between the thunderous footfalls of Naya's behemoths lie moments of perfect quiet."
        imageUri = "https://cards.scryfall.io/normal/front/f/b/fba31e86-0924-424d-b4e3-41a878e3e0e1.jpg?1783942532"
    }
}
