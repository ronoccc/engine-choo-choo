package com.wingedsheep.mtg.sets.definitions.afr.cards

import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EventPattern
import com.wingedsheep.sdk.scripting.ModifyLifeGain
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.references.Player

val ClericClass = card("Cleric Class") {
    manaCost = "{W}"
    colorIdentity = "W"
    typeLine = "Enchantment — Class"
    oracleText = "(Gain the next level as a sorcery to add its ability.)\n" +
        "If you would gain life, you gain that much life plus 1 instead.\n" +
        "{3}{W}: Level 2\n" +
        "Whenever you gain life, put a +1/+1 counter on target creature you control.\n" +
        "{4}{W}: Level 3\n" +
        "When this Class becomes level 3, return target creature card from your graveyard to " +
        "the battlefield. You gain life equal to that creature's toughness."

    replacementEffect(
        ModifyLifeGain(
            multiplier = 1,
            modifier = 1,
            appliesTo = EventPattern.LifeGainEvent(player = Player.You),
        ),
    )

    classLevel(2, "{3}{W}") {
        triggeredAbility {
            trigger = Triggers.you.gainsLife()
            val creature = target(TargetFilter.CreatureYouControl)
            effect = Effects.AddCounters(CounterType.PLUS_ONE_PLUS_ONE, 1, creature)
        }
    }

    classLevel(3, "{4}{W}") {
        triggeredAbility {
            trigger = Triggers.self.enters()
            val creatureCard = target(TargetFilter.CreatureInYourGraveyard)
            effect = Effects.PutOntoBattlefield(creatureCard) then
                Effects.GainLife(DynamicAmounts.targetToughness(0))
        }
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "6"
        artist = "Alayna Danner"
        imageUri = "https://cards.scryfall.io/normal/front/4/7/47ce8b7e-d8e1-489a-a69e-99089eeb8739.jpg?1783926538"
    }
}
