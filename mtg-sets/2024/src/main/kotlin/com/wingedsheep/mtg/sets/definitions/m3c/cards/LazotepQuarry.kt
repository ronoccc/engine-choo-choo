package com.wingedsheep.mtg.sets.definitions.m3c.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Lazotep Quarry
 * Land — Desert
 * {T}: Add {C}.
 * {T}, Sacrifice a creature: Add one mana of any color.
 * {X}{2}, {T}, Sacrifice a Desert: Exile target creature card with mana value X from your
 * graveyard. Create a token that's a copy of it, except it's a 4/4 black Zombie. Activate only
 * as a sorcery.
 */
val LazotepQuarry = card("Lazotep Quarry") {
    manaCost = ""
    colorIdentity = ""
    typeLine = "Land — Desert"
    oracleText = "{T}: Add {C}.\n{T}, Sacrifice a creature: Add one mana of any color.\n{X}{2}, " +
        "{T}, Sacrifice a Desert: Exile target creature card with mana value X from your " +
        "graveyard. Create a token that's a copy of it, except it's a 4/4 black Zombie. Activate " +
        "only as a sorcery."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Tap, Costs.Sacrifice(GameObjectFilter.Creature))
        effect = Effects.AddAnyColorMana(1)
        manaAbility = true
    }

    activatedAbility {
        cost = Costs.Composite(
            Costs.Mana("{X}{2}"),
            Costs.Tap,
            Costs.Sacrifice(GameObjectFilter.Permanent.withSubtype(Subtype.DESERT))
        )
        val creature = target(
            "target creature card with mana value X",
            TargetObject(filter = TargetFilter(GameObjectFilter.Creature.manaValueEqualsX(), zone = Zone.GRAVEYARD))
        )
        effect = Effects.Exile(creature) then CreateTokenCopyOfTargetEffect(
            target = creature,
            overridePower = 4,
            overrideToughness = 4,
            overrideColors = setOf(Color.BLACK),
            addedSubtypes = setOf(Subtype.ZOMBIE),
        )
        timing = TimingRule.SorcerySpeed
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "79"
        artist = "Sam Burley"
        imageUri = "https://cards.scryfall.io/normal/front/f/f/ff73b7f3-62f3-4a05-b439-bae2d0f63d2f.jpg?1783911415"
    }
}
