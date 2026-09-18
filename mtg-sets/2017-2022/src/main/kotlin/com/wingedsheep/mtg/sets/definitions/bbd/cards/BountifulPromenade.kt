package com.wingedsheep.mtg.sets.definitions.bbd.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EntersTapped
import com.wingedsheep.sdk.scripting.conditions.Compare
import com.wingedsheep.sdk.scripting.conditions.ComparisonOperator
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.ManaColorSet

/**
 * Bountiful Promenade
 * Land
 * This land enters tapped unless you have two or more opponents.
 * {T}: Add {G} or {W}.
 */
val BountifulPromenade = card("Bountiful Promenade") {
    manaCost = ""
    colorIdentity = "GW"
    typeLine = "Land"
    oracleText = "This land enters tapped unless you have two or more opponents.\n{T}: Add {G} or {W}."

    replacementEffect(
        EntersTapped(
            unlessCondition = Compare(
                DynamicAmount.PlayerCount(Player.EachOpponent),
                ComparisonOperator.GTE,
                DynamicAmount.Fixed(2)
            )
        )
    )

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddManaOfChoice(ManaColorSet.Specific(setOf(Color.GREEN, Color.WHITE)))
        manaAbility = true
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "81"
        artist = "Jung Park"
        flavorText = "No pilgrimage to Valor's Reach is complete without a stroll through its " +
            "celebrated shopping district."
        imageUri = "https://cards.scryfall.io/normal/front/2/1/21865ed6-5edd-41f4-9ae0-f501872d91dc.jpg?1783934849"
    }
}
