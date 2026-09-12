package com.wingedsheep.mtg.sets.definitions.xln.cards

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
 * Verdant Sun's Avatar
 * {5}{G}{G}
 * Creature — Dinosaur Avatar
 * Whenever this creature or another creature you control enters, you gain life equal to that
 * creature's toughness.
 *
 * ANY-bound "creature you control enters" trigger (the landfall idiom from
 * [Triggers.LandYouControlEnters]'s own doc comment, applied to creatures instead of lands so it
 * also fires off Verdant Sun's Avatar's own entry) reading the entering creature's toughness via
 * [EntityReference.Triggering].
 */
val VerdantSunsAvatar = card("Verdant Sun's Avatar") {
    manaCost = "{5}{G}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Dinosaur Avatar"
    power = 5
    toughness = 5
    oracleText = "Whenever this creature or another creature you control enters, you gain life " +
        "equal to that creature's toughness."

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
        collectorNumber = "213"
        artist = "Izzy"
        imageUri = "https://cards.scryfall.io/normal/front/9/d/9dbb5b6a-dc74-4e3e-9de1-5b379abdf2b4.jpg?1783935717"
    }
}
