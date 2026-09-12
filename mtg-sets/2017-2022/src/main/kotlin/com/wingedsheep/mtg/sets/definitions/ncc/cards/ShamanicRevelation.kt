package com.wingedsheep.mtg.sets.definitions.ncc.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Shamanic Revelation
 * {3}{G}{G}
 * Sorcery
 * Draw a card for each creature you control.
 * Ferocious — You gain 4 life for each creature you control with power 4 or greater.
 *
 * Both halves are plain [DynamicAmounts.battlefield] counts — the second scoped to a
 * power-at-least-4 filter (the Ferocious threshold) and multiplied by 4 for the life total.
 */
val ShamanicRevelation = card("Shamanic Revelation") {
    manaCost = "{3}{G}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    oracleText = "Draw a card for each creature you control.\n" +
        "Ferocious — You gain 4 life for each creature you control with power 4 or greater."

    spell {
        effect = Effects.DrawCards(DynamicAmounts.battlefield(Player.You, GameObjectFilter.Creature).count())
            .then(
                Effects.GainLife(
                    DynamicAmount.Multiply(
                        DynamicAmounts.battlefield(Player.You, GameObjectFilter.Creature.powerAtLeast(4)).count(),
                        4
                    )
                )
            )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "311"
        artist = "Cynthia Sheppard"
        imageUri = "https://cards.scryfall.io/normal/front/b/3/b3784eff-ab7b-4fd4-9a07-fbc852d116bf.jpg?1783923241"
    }
}
