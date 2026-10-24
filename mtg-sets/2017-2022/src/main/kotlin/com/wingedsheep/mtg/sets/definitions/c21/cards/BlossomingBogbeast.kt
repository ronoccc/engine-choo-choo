package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val BlossomingBogbeast = card("Blossoming Bogbeast") {
    manaCost = "{4}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Beast"
    power = 3
    toughness = 3
    oracleText = "Whenever this creature attacks, you gain 2 life. Then creatures you control " +
        "gain trample and get +X/+X until end of turn, where X is the amount of life you gained " +
        "this turn."

    triggeredAbility {
        trigger = Triggers.self.attacks()
        effect = Effects.GainLife(2) then Effects.ForEachInGroup(
            GroupFilter(GameObjectFilter.Creature.youControl()),
            Effects.GrantKeyword(Keyword.TRAMPLE, EffectTarget.Self) then
                Effects.ModifyStats(
                    DynamicAmounts.lifeGainedThisTurn(),
                    DynamicAmounts.lifeGainedThisTurn(),
                    EffectTarget.Self,
                ),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "59"
        artist = "Aaron Miller"
        flavorText = "As subtle as a bogbeast\n—Witherbloom expression meaning \"crude and clumsy\""
        imageUri = "https://cards.scryfall.io/normal/front/3/3/332153ab-1b8e-40a8-b0b4-01f94866d368.jpg?1783927591"
    }
}
