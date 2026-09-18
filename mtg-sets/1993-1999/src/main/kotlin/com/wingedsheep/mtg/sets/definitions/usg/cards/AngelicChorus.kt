package com.wingedsheep.mtg.sets.definitions.usg.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty
import com.wingedsheep.sdk.scripting.values.EntityReference

/**
 * Angelic Chorus
 * {3}{W}{W}
 * Enchantment
 * Whenever a creature you control enters, you gain life equal to its toughness.
 */
val AngelicChorus = card("Angelic Chorus") {
    manaCost = "{3}{W}{W}"
    colorIdentity = "W"
    typeLine = "Enchantment"
    oracleText = "Whenever a creature you control enters, you gain life equal to its toughness."

    triggeredAbility {
        trigger = Triggers.entersBattlefield(
            filter = GameObjectFilter.Creature.youControl(),
            binding = TriggerBinding.ANY
        )
        effect = Effects.GainLife(
            DynamicAmount.EntityProperty(EntityReference.Triggering, EntityNumericProperty.Toughness)
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "3"
        artist = "Ron Spencer"
        flavorText = "The very young and the very old know best the song the angels sing."
        imageUri = "https://cards.scryfall.io/normal/front/9/0/907bf221-a1bf-41ab-9b7e-e5a64c385642.jpg?1783946378"
    }
}
