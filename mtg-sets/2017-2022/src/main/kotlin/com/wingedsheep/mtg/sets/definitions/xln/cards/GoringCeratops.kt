package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Goring Ceratops
 * {5}{W}{W}
 * Creature — Dinosaur
 * Double strike
 * Whenever this creature attacks, other creatures you control gain double strike until end of
 * turn.
 *
 * Same [ForEachInGroupEffect] + [Effects.GrantKeyword] shape as Heroic Intervention, scoped to
 * [GroupFilter.OtherCreaturesYouControl] and fired on [Triggers.Attacks] instead of on cast.
 */
val GoringCeratops = card("Goring Ceratops") {
    manaCost = "{5}{W}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dinosaur"
    power = 3
    toughness = 3
    oracleText = "Double strike\n" +
        "Whenever this creature attacks, other creatures you control gain double strike until " +
        "end of turn."

    keywords(Keyword.DOUBLE_STRIKE)

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = Effects.ForEachInGroup(
            GroupFilter.OtherCreaturesYouControl,
            Effects.GrantKeyword(Keyword.DOUBLE_STRIKE, EffectTarget.Self, Duration.EndOfTurn)
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "13"
        artist = "Zezhou Chen"
        imageUri = "https://cards.scryfall.io/normal/front/8/3/8309f684-5912-4191-9f64-d573f1cc84c9.jpg?1783935801"
    }
}
