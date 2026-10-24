package com.wingedsheep.mtg.sets.definitions.znr.cards

import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val TerritorialScythecat = card("Territorial Scythecat") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Cat"
    power = 2
    toughness = 1
    oracleText = "Trample\n" +
        "Landfall — Whenever a land you control enters, put a +1/+1 counter on this creature."

    keywords(Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.a(GameObjectFilter.Land.youControl()).enters()
        effect = Effects.AddCounters(CounterType.PLUS_ONE_PLUS_ONE, 1, EffectTarget.Self)
        description = "Landfall — Whenever a land you control enters, put a +1/+1 counter on this creature."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "213"
        artist = "Wisnu Tan"
        flavorText = "Standing between a scythecat and its prey makes you the appetizer."
        imageUri = "https://cards.scryfall.io/normal/front/5/e/5e0f725c-8d1f-47ff-ad81-a5007199a5e2.jpg?1783929328"
    }
}
