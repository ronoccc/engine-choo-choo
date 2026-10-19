package com.wingedsheep.mtg.sets.definitions.grn.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Camaraderie
 * {4}{G}{W}
 * Sorcery
 * You gain X life and draw X cards, where X is the number of creatures you control. Creatures you
 * control get +1/+1 until end of turn.
 */
val Camaraderie = card("Camaraderie") {
    manaCost = "{4}{G}{W}"
    colorIdentity = "GW"
    typeLine = "Sorcery"
    oracleText = "You gain X life and draw X cards, where X is the number of creatures you control. " +
        "Creatures you control get +1/+1 until end of turn."

    spell {
        val x = DynamicAmounts.creaturesYouControl()
        effect = Effects.Composite(
            Effects.GainLife(x),
            Effects.DrawCards(x),
            Effects.ForEachInGroup(
                GroupFilter(GameObjectFilter.Creature.youControl()),
                Effects.ModifyStats(1, 1, EffectTarget.Self)
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "157"
        artist = "Sidharth Chaturvedi"
        flavorText = "\"Within the song of Mat'Selesnya, one becomes all.\"\n—Heruj, Selesnya hierophant"
        imageUri = "https://cards.scryfall.io/normal/front/8/9/890a2fa9-1141-4a4c-85af-05723aba5e39.jpg?1783934140"
    }
}
