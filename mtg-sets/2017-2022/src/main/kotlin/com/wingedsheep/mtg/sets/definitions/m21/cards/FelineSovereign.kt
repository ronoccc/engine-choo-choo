package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.events.Recipient
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

private const val PROTECTION_FROM_DOGS = "PROTECTION_FROM_SUBTYPE_DOG"

val FelineSovereign = card("Feline Sovereign") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Cat"
    power = 2
    toughness = 3
    oracleText = "Other Cats you control get +1/+1 and have protection from Dogs.\n" +
        "Whenever one or more Cats you control deal combat damage to a player, destroy up to one " +
        "target artifact or enchantment that player controls."

    staticAbility {
        ability = ModifyStats(
            powerBonus = 1,
            toughnessBonus = 1,
            filter = GroupFilter(GameObjectFilter.Creature.withSubtype(Subtype.CAT).youControl(), excludeSelf = true),
        )
    }
    staticAbility {
        ability = GrantKeyword(
            keyword = PROTECTION_FROM_DOGS,
            filter = GroupFilter(GameObjectFilter.Creature.withSubtype(Subtype.CAT).youControl(), excludeSelf = true),
        )
    }

    triggeredAbility {
        trigger = Triggers.a(GameObjectFilter.Creature.youControl().withSubtype(Subtype.CAT))
            .dealsCombatDamage(Recipient.AnyPlayer, batch = true)
        val t = target(
            TargetFilter(GameObjectFilter.ArtifactOrEnchantment.controlledByTriggeringPlayer()),
            optional = true,
        )
        effect = Effects.Destroy(t)
        description = "Whenever one or more Cats you control deal combat damage to a player, destroy " +
            "up to one target artifact or enchantment that player controls."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "180"
        artist = "Dan Murayama Scott"
        flavorText = "Dogs beg. Cats lay claim."
        imageUri = "https://cards.scryfall.io/normal/front/8/4/84a9485a-d356-4cbe-b257-b62008a21328.jpg?1783930677"
    }
}
