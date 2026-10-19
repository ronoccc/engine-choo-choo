package com.wingedsheep.mtg.sets.definitions.rtr.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfChosenPermanentEffect
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty
import com.wingedsheep.sdk.scripting.values.EntityReference

/**
 * Trostani, Selesnya's Voice
 * {G}{G}{W}{W}
 * Legendary Creature — Dryad
 * 2/5
 * Whenever another creature you control enters, you gain life equal to that creature's toughness.
 * {1}{G}{W}, {T}: Populate. (Create a token that's a copy of a creature token you control.)
 */
val TrostaniSelesnyasVoice = card("Trostani, Selesnya's Voice") {
    manaCost = "{G}{G}{W}{W}"
    colorIdentity = "GW"
    typeLine = "Legendary Creature — Dryad"
    power = 2
    toughness = 5
    oracleText = "Whenever another creature you control enters, you gain life equal to that " +
        "creature's toughness.\n" +
        "{1}{G}{W}, {T}: Populate. (Create a token that's a copy of a creature token you control.)"

    triggeredAbility {
        trigger = Triggers.entersBattlefield(
            filter = GameObjectFilter.Creature.youControl(),
            binding = TriggerBinding.OTHER
        )
        effect = Effects.GainLife(
            DynamicAmount.EntityProperty(EntityReference.Triggering, EntityNumericProperty.Toughness)
        )
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{1}{G}{W}"), Costs.Tap)
        effect = CreateTokenCopyOfChosenPermanentEffect(filter = GameObjectFilter.Creature.token())
        description = "Populate."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "206"
        artist = "Chippy"
        imageUri = "https://cards.scryfall.io/normal/front/9/d/9d1d9d86-5666-4e59-9766-137657b4e040.jpg?1783940330"
    }
}
