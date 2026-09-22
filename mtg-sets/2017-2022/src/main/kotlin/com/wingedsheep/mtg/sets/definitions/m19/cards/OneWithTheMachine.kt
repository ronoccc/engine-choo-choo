package com.wingedsheep.mtg.sets.definitions.m19.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player

/**
 * One with the Machine
 * {3}{U}
 * Sorcery
 * Draw cards equal to the greatest mana value among artifacts you control.
 *
 * `DynamicAmounts.battlefield(Player.You, GameObjectFilter.Artifact).maxManaValue()` is the
 * generic max-aggregate over your battlefield (`AggregateBattlefield` with `Aggregation.MAX` and
 * `CardNumericProperty.MANA_VALUE`) filtered down to artifacts you control. Zero artifacts means
 * zero cards drawn rather than an error, matching the rules for "greatest X among Y" with an
 * empty Y.
 */
val OneWithTheMachine = card("One with the Machine") {
    manaCost = "{3}{U}"
    colorIdentity = "U"
    typeLine = "Sorcery"
    oracleText = "Draw cards equal to the greatest mana value among artifacts you control."

    spell {
        effect = Effects.DrawCards(
            DynamicAmounts.battlefield(Player.You, GameObjectFilter.Artifact).maxManaValue()
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "66"
        artist = "Chase Stone"
        flavorText = "\"When I grafted the Planar Bridge into myself I felt my Planeswalker spark " +
            "flare beyond my body. The Multiverse was my plaything. It felt . . . incredible.\" " +
            "—Tezzeret"
        imageUri = "https://cards.scryfall.io/normal/front/1/7/17f2aaf5-6c1f-4663-865f-6cdd5640485a.jpg?1783934583"
    }
}
