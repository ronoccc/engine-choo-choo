package com.wingedsheep.mtg.sets.definitions.aer.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty

val ScrapTrawler = card("Scrap Trawler") {
    manaCost = "{3}"
    colorIdentity = ""
    typeLine = "Artifact Creature — Construct"
    power = 3
    toughness = 2
    oracleText = "Whenever this creature dies or another artifact you control is put into a " +
        "graveyard from the battlefield, return to your hand target artifact card in your " +
        "graveyard with lesser mana value."

    triggeredAbility {
        val lesserManaValue = DynamicAmount.Subtract(
            DynamicAmounts.propertyOf(EffectTarget.TriggeringEntity, EntityNumericProperty.ManaValue),
            DynamicAmount.Fixed(1),
        )
        trigger = Triggers.a(GameObjectFilter.Artifact.youControl()).dies()
        val t = target(
            TargetFilter(
                GameObjectFilter.Artifact.ownedByYou().manaValueAtMostDynamic(lesserManaValue),
                zone = Zone.GRAVEYARD,
            ),
        )
        effect = Effects.ReturnToHand(t)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "175"
        artist = "Daarken"
        imageUri = "https://cards.scryfall.io/normal/front/6/8/68abc75f-596f-4169-96fc-ada941ef47ed.jpg?1783936719"
    }
}
