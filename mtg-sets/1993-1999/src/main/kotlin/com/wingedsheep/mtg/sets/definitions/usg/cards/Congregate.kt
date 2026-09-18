package com.wingedsheep.mtg.sets.definitions.usg.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.TargetPlayer
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Congregate
 * {3}{W}
 * Instant
 * Target player gains 2 life for each creature on the battlefield.
 */
val Congregate = card("Congregate") {
    manaCost = "{3}{W}"
    colorIdentity = "W"
    typeLine = "Instant"
    oracleText = "Target player gains 2 life for each creature on the battlefield."

    spell {
        val t = target("target", TargetPlayer())
        effect = Effects.GainLife(
            DynamicAmount.Multiply(
                DynamicAmount.AggregateBattlefield(Player.Each, GameObjectFilter.Creature),
                2
            ),
            t
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "8"
        artist = "Mark Zug"
        flavorText = "\"In the gathering there is strength for all who founder, renewal for all who " +
            "languish, love for all who sing.\"\n—Song of All, canto 642"
        imageUri = "https://cards.scryfall.io/normal/front/8/0/80b7923b-eb1c-49ce-8250-a1ea6efbb56e.jpg?1783946377"
    }
}
