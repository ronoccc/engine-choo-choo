package com.wingedsheep.mtg.sets.definitions.hou.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Crested Sunmare
 * {3}{W}{W}
 * Creature — Horse
 * 5/5
 * Other Horses you control have indestructible.
 * At the beginning of each end step, if you gained life this turn, create a 5/5 white Horse
 * creature token.
 */
val CrestedSunmare = card("Crested Sunmare") {
    manaCost = "{3}{W}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Horse"
    power = 5
    toughness = 5
    oracleText = "Other Horses you control have indestructible.\n" +
        "At the beginning of each end step, if you gained life this turn, create a 5/5 white Horse " +
        "creature token."

    staticAbility {
        ability = GrantKeyword(
            Keyword.INDESTRUCTIBLE,
            GroupFilter(GameObjectFilter.Creature.withSubtype(Subtype.HORSE).youControl()).other()
        )
    }

    triggeredAbility {
        trigger = Triggers.EachEndStep
        interveningIf = Conditions.YouGainedLifeThisTurn
        effect = Effects.CreateToken(
            power = 5,
            toughness = 5,
            colors = setOf(Color.WHITE),
            creatureTypes = setOf("Horse")
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "6"
        artist = "Lucas Graciano"
        flavorText = "\"It is evidence that some pure corner of the world must still exist.\"\n" +
            "—Djeru, former Tah-crop initiate"
        imageUri = "https://cards.scryfall.io/normal/front/7/3/732fa4c9-11da-4bdb-96af-aa37c74be25f.jpg?1783936066"
    }
}
