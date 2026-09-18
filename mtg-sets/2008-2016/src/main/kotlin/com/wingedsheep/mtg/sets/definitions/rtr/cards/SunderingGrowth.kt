package com.wingedsheep.mtg.sets.definitions.rtr.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfChosenPermanentEffect
import com.wingedsheep.sdk.scripting.targets.TargetPermanent
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Sundering Growth
 * {G/W}{G/W}
 * Instant
 * Destroy target artifact or enchantment, then populate. (Create a token that's a copy of a
 * creature token you control.)
 */
val SunderingGrowth = card("Sundering Growth") {
    manaCost = "{G/W}{G/W}"
    colorIdentity = "GW"
    typeLine = "Instant"
    oracleText = "Destroy target artifact or enchantment, then populate. (Create a token that's a " +
        "copy of a creature token you control.)"

    spell {
        val t = target(
            "target artifact or enchantment",
            TargetPermanent(filter = TargetFilter.ArtifactOrEnchantment)
        )
        effect = Effects.Composite(
            Effects.Destroy(t),
            CreateTokenCopyOfChosenPermanentEffect(filter = GameObjectFilter.Creature.token())
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "223"
        artist = "David Palumbo"
        flavorText = "\"One day every pillar will be a tree and every hall a glade.\"\n—Trostani"
        imageUri = "https://cards.scryfall.io/normal/front/1/4/14d5048e-cb76-48c4-8a95-70dcc14775f6.jpg?1783940326"
    }
}
