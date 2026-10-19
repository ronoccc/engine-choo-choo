package com.wingedsheep.mtg.sets.definitions.m14.cards

import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Archangel of Thune
 * {3}{W}{W}
 * Creature — Angel
 * 3/4
 * Flying
 * Lifelink
 * Whenever you gain life, put a +1/+1 counter on each creature you control.
 */
val ArchangelOfThune = card("Archangel of Thune") {
    manaCost = "{3}{W}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Angel"
    power = 3
    toughness = 4
    oracleText = "Flying\nLifelink (Damage dealt by this creature also causes you to gain that " +
        "much life.)\nWhenever you gain life, put a +1/+1 counter on each creature you control."

    keywords(Keyword.FLYING, Keyword.LIFELINK)

    triggeredAbility {
        trigger = Triggers.YouGainLife
        effect = Effects.ForEachInGroup(
            GroupFilter(GameObjectFilter.Creature.youControl()),
            Effects.AddCounters(Counters.PLUS_ONE_PLUS_ONE, 1, EffectTarget.Self)
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "5"
        artist = "James Ryman"
        flavorText = "Even the wicked have nightmares."
        imageUri = "https://cards.scryfall.io/normal/front/5/3/531cba81-afd7-4be4-adec-87edb77ba2a9.jpg?1783939946"
    }
}
