package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.effects.PreventionScope
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Pack Leader
 * {1}{W}
 * Creature — Dog
 * 2/2
 * Other Dogs you control get +1/+1.
 * Whenever this creature attacks, prevent all combat damage that would be dealt this turn to
 * Dogs you control.
 *
 * The anthem excludes Pack Leader itself ([GroupFilter.excludeSelf]); the attack trigger's
 * prevention shield does not — "Dogs you control" includes Pack Leader — so it's modelled with
 * [Effects.PreventAllDamageToGroup] over a Dog-you-control filter with no `excludeSelf`, scoped
 * to combat damage for the rest of the turn.
 */
val PackLeader = card("Pack Leader") {
    manaCost = "{1}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dog"
    oracleText = "Other Dogs you control get +1/+1.\n" +
        "Whenever this creature attacks, prevent all combat damage that would be dealt this " +
        "turn to Dogs you control."
    power = 2
    toughness = 2

    staticAbility {
        ability = ModifyStats(
            powerBonus = 1,
            toughnessBonus = 1,
            filter = GroupFilter(GameObjectFilter.Permanent.withSubtype(Subtype.DOG).youControl(), excludeSelf = true)
        )
    }

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = Effects.PreventAllDamageToGroup(
            group = GroupFilter(GameObjectFilter.Creature.withSubtype(Subtype.DOG).youControl()),
            scope = PreventionScope.CombatOnly
        )
        description = "Whenever this creature attacks, prevent all combat damage that would be " +
            "dealt this turn to Dogs you control."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "29"
        artist = "Rudy Siswanto"
        flavorText = "He will be your loyal champion, and his pack your protectors. All he asks " +
            "for is a full belly, a spot by the fire, and all the love in your heart."
        imageUri = "https://cards.scryfall.io/normal/front/a/8/a8b94bc1-68d1-41dc-914c-a33ecb9aeb49.jpg?1783930738"
    }
}
