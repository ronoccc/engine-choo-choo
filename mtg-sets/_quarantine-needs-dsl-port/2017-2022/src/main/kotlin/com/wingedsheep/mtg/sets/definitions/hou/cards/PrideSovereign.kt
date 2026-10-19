package com.wingedsheep.mtg.sets.definitions.hou.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GrantDynamicStatsEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Pride Sovereign — Hour of Devastation #126.
 *
 * The static self-buff is the Boneclub Berserker shape: [GrantDynamicStatsEffect] on
 * `GroupFilter.source()` with [DynamicAmounts.otherCreaturesWithSubtypeYouControl] (Cat) for both
 * power and toughness. The activated ability is the plain Basri, Tomorrow's Champion Exert-token
 * idiom, at two tokens instead of one.
 */
val PrideSovereign = card("Pride Sovereign") {
    manaCost = "{2}{G}"
    colorIdentity = "GW"
    typeLine = "Creature — Cat"
    power = 2
    toughness = 2
    oracleText = "This creature gets +1/+1 for each other Cat you control.\n" +
        "{W}, {T}, Exert this creature: Create two 1/1 white Cat creature tokens with lifelink. " +
        "(An exerted creature won't untap during your next untap step.)"

    staticAbility {
        ability = GrantDynamicStatsEffect(
            filter = GroupFilter.source(),
            powerBonus = DynamicAmounts.otherCreaturesWithSubtypeYouControl(Subtype.CAT),
            toughnessBonus = DynamicAmounts.otherCreaturesWithSubtypeYouControl(Subtype.CAT),
        )
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{W}"), Costs.Tap, Costs.Exert)
        effect = Effects.CreateToken(
            power = 1,
            toughness = 1,
            colors = setOf(Color.WHITE),
            creatureTypes = setOf("Cat"),
            keywords = setOf(Keyword.LIFELINK),
            count = 2,
            imageUri = "https://cards.scryfall.io/normal/front/8/6/86701490-17ac-4253-810d-6cfd7a46594c.jpg?1783908586",
        )
        description = "{W}, {T}, Exert this creature: Create two 1/1 white Cat creature tokens with lifelink."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "126"
        artist = "Ryan Yee"
        imageUri = "https://cards.scryfall.io/normal/front/d/d/dd3d32b7-672f-4ceb-a1c9-17daefd2cb0c.jpg?1783936016"
    }
}
