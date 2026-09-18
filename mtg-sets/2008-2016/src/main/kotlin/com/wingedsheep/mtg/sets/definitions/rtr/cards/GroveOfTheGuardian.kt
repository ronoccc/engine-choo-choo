package com.wingedsheep.mtg.sets.definitions.rtr.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.costs.CostAtom

/**
 * Grove of the Guardian
 * Land
 * {T}: Add {C}.
 * {3}{G}{W}, {T}, Tap two untapped creatures you control, Sacrifice this land: Create an 8/8
 * green and white Elemental creature token with vigilance.
 */
val GroveOfTheGuardian = card("Grove of the Guardian") {
    manaCost = ""
    colorIdentity = "GW"
    typeLine = "Land"
    oracleText = "{T}: Add {C}.\n{3}{G}{W}, {T}, Tap two untapped creatures you control, Sacrifice " +
        "this land: Create an 8/8 green and white Elemental creature token with vigilance."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
    }

    activatedAbility {
        cost = AbilityCost.Composite(
            listOf(
                AbilityCost.Atom(CostAtom.Mana(com.wingedsheep.sdk.core.ManaCost.parse("{3}{G}{W}"))),
                AbilityCost.Tap,
                AbilityCost.Atom(CostAtom.TapPermanents(count = 2, filter = GameObjectFilter.Creature.youControl())),
                AbilityCost.SacrificeSelf,
            )
        )
        effect = Effects.CreateToken(
            power = 8,
            toughness = 8,
            colors = setOf(Color.GREEN, Color.WHITE),
            creatureTypes = setOf("Elemental"),
            keywords = setOf(Keyword.VIGILANCE),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "240"
        artist = "Christine Choi"
        imageUri = "https://cards.scryfall.io/normal/front/3/c/3cf60ca0-e01f-499c-8d04-d59050f38c33.jpg?1783940321"
    }
}
