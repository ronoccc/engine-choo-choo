package com.wingedsheep.mtg.sets.definitions.mh2.cards

import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.MayEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.ContextPropertyKey
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Nykthos Paragon
 * {4}{W}{W}
 * Enchantment Creature — Human Soldier
 * 4/6
 * Whenever you gain life, you may put that many +1/+1 counters on each creature you control. Do
 * this only once each turn.
 */
val NykthosParagon = card("Nykthos Paragon") {
    manaCost = "{4}{W}{W}"
    colorIdentity = "W"
    typeLine = "Enchantment Creature — Human Soldier"
    power = 4
    toughness = 6
    oracleText = "Whenever you gain life, you may put that many +1/+1 counters on each creature " +
        "you control. Do this only once each turn."

    triggeredAbility {
        trigger = Triggers.YouGainLife
        effectOncePerTurn = true
        effect = MayEffect(
            Effects.ForEachInGroup(
                GroupFilter(GameObjectFilter.Creature.youControl()),
                Effects.AddDynamicCounters(
                    Counters.PLUS_ONE_PLUS_ONE,
                    DynamicAmount.ContextProperty(ContextPropertyKey.TRIGGER_LIFE_GAINED),
                    EffectTarget.Self,
                )
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "22"
        artist = "Martina Pilcerova"
        flavorText = "Though Heliod turned against humanity in his hunger for power, the ideals " +
            "he once championed still stand strong."
        imageUri = "https://cards.scryfall.io/normal/front/2/9/2963205e-b181-44d1-809f-6577e29fa812.jpg?1783926889"
    }
}
