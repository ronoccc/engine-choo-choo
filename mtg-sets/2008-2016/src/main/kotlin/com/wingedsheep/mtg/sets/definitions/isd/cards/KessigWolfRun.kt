package com.wingedsheep.mtg.sets.definitions.isd.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Kessig Wolf Run
 * Land
 * {T}: Add {C}.
 * {X}{R}{G}, {T}: Target creature gets +X/+0 and gains trample until end of turn.
 */
val KessigWolfRun = card("Kessig Wolf Run") {
    colorIdentity = "RG"
    typeLine = "Land"
    oracleText = "{T}: Add {C}.\n" +
        "{X}{R}{G}, {T}: Target creature gets +X/+0 and gains trample until end of turn."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
        timing = TimingRule.ManaAbility
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{X}{R}{G}"), Costs.Tap)
        val creature = target(TargetFilter.Creature)
        effect = Effects.ModifyStats(DynamicAmounts.xValue(), DynamicAmount.Fixed(0), creature) then
            Effects.GrantKeyword(Keyword.TRAMPLE, creature)
        description = "Target creature gets +X/+0 and gains trample until end of turn."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "244"
        artist = "Eytan Zana"
    }
}
