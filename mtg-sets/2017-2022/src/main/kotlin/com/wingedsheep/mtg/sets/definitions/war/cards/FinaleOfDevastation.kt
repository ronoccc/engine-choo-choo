package com.wingedsheep.mtg.sets.definitions.war.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.conditions.ComparisonOperator
import com.wingedsheep.sdk.scripting.effects.SearchDestination
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val FinaleOfDevastation = card("Finale of Devastation") {
    manaCost = "{X}{G}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    oracleText = "Search your library and/or graveyard for a creature card with mana value X or " +
        "less and put it onto the battlefield. If you search your library this way, shuffle. If " +
        "X is 10 or more, creatures you control get +X/+X and gain haste until end of turn."

    spell {
        effect = Patterns.Library.searchMultipleZones(
            zones = listOf(Zone.GRAVEYARD, Zone.LIBRARY),
            filter = GameObjectFilter.Creature.manaValueAtMostX(),
            count = 1,
            destination = SearchDestination.BATTLEFIELD,
        ) then Effects.ShuffleLibrary() then Effects.If(
            condition = Conditions.CompareAmounts(
                DynamicAmounts.xValue(),
                ComparisonOperator.GTE,
                10,
            ),
            then = Effects.ForEachInGroup(
                GroupFilter(GameObjectFilter.Creature.youControl()),
                Effects.ModifyStats(DynamicAmounts.xValue(), DynamicAmounts.xValue(), EffectTarget.Self) then
                    Effects.GrantKeyword(Keyword.HASTE, EffectTarget.Self),
            ),
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "160"
        artist = "Bayard Wu"
        flavorText = "Some spells sing a quiet tune. Others roar."
        imageUri = "https://cards.scryfall.io/normal/front/9/8/985453e7-997e-4d77-a338-cc0290791ebe.jpg?1783933415"
    }
}
