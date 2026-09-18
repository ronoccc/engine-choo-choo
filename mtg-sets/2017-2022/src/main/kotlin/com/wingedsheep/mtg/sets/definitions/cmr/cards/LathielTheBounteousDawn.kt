package com.wingedsheep.mtg.sets.definitions.cmr.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * Lathiel, the Bounteous Dawn
 * {2}{G}{W}
 * Legendary Creature — Unicorn
 * 2/2
 * Lifelink
 * At the beginning of each end step, if you gained life this turn, distribute up to that many
 * +1/+1 counters among any number of other target creatures.
 */
val LathielTheBounteousDawn = card("Lathiel, the Bounteous Dawn") {
    manaCost = "{2}{G}{W}"
    colorIdentity = "GW"
    typeLine = "Legendary Creature — Unicorn"
    power = 2
    toughness = 2
    oracleText = "Lifelink\n" +
        "At the beginning of each end step, if you gained life this turn, distribute up to that " +
        "many +1/+1 counters among any number of other target creatures."

    keywords(Keyword.LIFELINK)

    triggeredAbility {
        trigger = Triggers.EachEndStep
        interveningIf = Conditions.YouGainedLifeThisTurn
        target(
            "other target creatures",
            TargetCreature(
                unlimited = true,
                minCount = 0,
                dynamicMaxCount = DynamicAmounts.lifeGainedThisTurn(),
                filter = TargetFilter.Creature.other(),
            )
        )
        effect = Effects.DistributeCountersAmongTargets(DynamicAmounts.lifeGainedThisTurn())
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "285"
        artist = "Lucas Graciano"
        flavorText = "Weeping, the hunter cast aside her bow, struck by the majesty of the " +
            "creature before her."
        imageUri = "https://cards.scryfall.io/normal/front/2/9/29d3484f-abd1-43fe-99f8-e0fa0a7b6692.jpg?1783928769"
    }
}
