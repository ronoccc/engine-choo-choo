package com.wingedsheep.mtg.sets.definitions.c19.cards

import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfChosenPermanentEffect

/**
 * Song of the Worldsoul
 * {4}{W}{W}
 * Enchantment
 * Whenever you cast a spell, populate. (Create a token that's a copy of a creature token you
 * control.)
 */
val SongOfTheWorldsoul = card("Song of the Worldsoul") {
    manaCost = "{4}{W}{W}"
    colorIdentity = "W"
    typeLine = "Enchantment"
    oracleText = "Whenever you cast a spell, populate. (Create a token that's a copy of a " +
        "creature token you control.)"

    triggeredAbility {
        trigger = Triggers.YouCastSpell
        effect = CreateTokenCopyOfChosenPermanentEffect(filter = GameObjectFilter.Creature.token())
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "6"
        artist = "Torstein Nordstrand"
        flavorText = "\"Mat'Selesnya sings out, and life flourishes in answer.\""
        imageUri = "https://cards.scryfall.io/normal/front/b/b/bb73ec0d-f582-4f74-9b5c-180fe3aedcf6.jpg?1783932817"
    }
}
