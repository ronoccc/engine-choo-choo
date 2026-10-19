package com.wingedsheep.mtg.sets.definitions.rtr.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfChosenPermanentEffect
import com.wingedsheep.sdk.scripting.effects.GrantKeywordEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Rootborn Defenses
 * {2}{W}
 * Instant
 * Populate. Creatures you control gain indestructible until end of turn. (To populate, create a
 * token that's a copy of a creature token you control.)
 */
val RootbornDefenses = card("Rootborn Defenses") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Instant"
    oracleText = "Populate. Creatures you control gain indestructible until end of turn. (To " +
        "populate, create a token that's a copy of a creature token you control.)"

    spell {
        effect = Effects.Composite(
            CreateTokenCopyOfChosenPermanentEffect(filter = GameObjectFilter.Creature.token()),
            Effects.ForEachInGroup(
                GroupFilter(GameObjectFilter.Creature.youControl()),
                GrantKeywordEffect(Keyword.INDESTRUCTIBLE, EffectTarget.Self)
            )
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "19"
        artist = "Mark Zug"
        imageUri = "https://cards.scryfall.io/normal/front/d/e/deccfa48-b8df-4dcc-ba1b-920f8352def7.jpg?1783940374"
    }
}
