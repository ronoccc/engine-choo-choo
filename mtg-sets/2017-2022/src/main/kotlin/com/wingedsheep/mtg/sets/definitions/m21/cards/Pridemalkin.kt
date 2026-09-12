package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Pridemalkin — Core Set 2021 #196
 * {2}{G} · Creature — Cat · 2/1 · Common
 *
 * When this creature enters, put a +1/+1 counter on target creature you control.
 * Each creature you control with a +1/+1 counter on it has trample.
 *
 * The ETB is a plain [Effects.AddCounters] onto a targeted creature you control — the ruling that
 * Pridemalkin can target itself falls out for free since it's on the battlefield by the time the
 * trigger resolves. The trample grant is a continuous static ability over
 * [GameObjectFilter.withCounter] rather than a one-shot effect, so it starts, stops, and moves with
 * any +1/+1 counter on any creature you control (including counters this card never granted), and
 * applies to Pridemalkin itself once it has one (second ruling).
 */
val Pridemalkin = card("Pridemalkin") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Cat"
    power = 2
    toughness = 1
    oracleText = "When this creature enters, put a +1/+1 counter on target creature you control.\n" +
        "Each creature you control with a +1/+1 counter on it has trample."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        target = Targets.CreatureYouControl
        effect = Effects.AddCounters(Counters.PLUS_ONE_PLUS_ONE, 1, EffectTarget.ContextTarget(0))
        description = "When this creature enters, put a +1/+1 counter on target creature you control."
    }

    staticAbility {
        ability = GrantKeyword(
            Keyword.TRAMPLE,
            GroupFilter(GameObjectFilter.Creature.youControl().withCounter(Counters.PLUS_ONE_PLUS_ONE))
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "196"
        artist = "Karl Kopinski"
        imageUri = "https://cards.scryfall.io/normal/front/d/f/df520254-0c72-496b-9222-263ca9d3c5d5.jpg?1783930671"

        ruling("2020-06-23", "Pridemalkin can be the target of its own first ability.")
        ruling(
            "2020-06-23",
            "Pridemalkin's second ability applies to Pridemalkin as long as it has a +1/+1 counter " +
                "on it."
        )
    }
}
