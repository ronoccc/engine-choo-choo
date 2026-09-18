package com.wingedsheep.mtg.sets.definitions.gpt.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Storm Herd
 * {8}{W}{W}
 * Sorcery
 * Create X 1/1 white Pegasus creature tokens with flying, where X is your life total.
 */
val StormHerd = card("Storm Herd") {
    manaCost = "{8}{W}{W}"
    colorIdentity = "W"
    typeLine = "Sorcery"
    oracleText = "Create X 1/1 white Pegasus creature tokens with flying, where X is your life total."

    spell {
        effect = Effects.CreateToken(
            count = DynamicAmount.LifeTotal(Player.You),
            power = 1,
            toughness = 1,
            colors = setOf(Color.WHITE),
            creatureTypes = setOf("Pegasus"),
            keywords = setOf(Keyword.FLYING),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "19"
        artist = "Jim Nelson"
        flavorText = "\"When you hear thunder on a cloudless day, take cover and brace for the " +
            "coming of the storm herd.\"\n—Skotov, Tin Street basket vendor"
        imageUri = "https://cards.scryfall.io/normal/front/c/8/c8c86398-2cbc-4ad7-a231-6802e3b4d1c0.jpg?1783943524"
    }
}
