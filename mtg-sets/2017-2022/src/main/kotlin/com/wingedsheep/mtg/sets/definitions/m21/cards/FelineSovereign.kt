package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.events.DamageType
import com.wingedsheep.sdk.scripting.events.RecipientFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetPermanent

/** The projected keyword string for "protection from Dogs" — see [GrantKeyword]'s String overload. */
private const val PROTECTION_FROM_DOGS = "PROTECTION_FROM_SUBTYPE_DOG"

/**
 * Feline Sovereign — Core Set 2021 #180.
 *
 * The lord is two static abilities over "other Cats you control" ([GroupFilter] with
 * `excludeSelf = true`): a [ModifyStats] +1/+1 and a [GrantKeyword] of the synthetic
 * `PROTECTION_FROM_SUBTYPE_DOG` string, the same projected-keyword convention Katilda,
 * Dawnhart Martyr's "protection from Vampires" uses.
 *
 * The trigger is the Keeper of Fables batch shape: [Triggers.dealsDamage] with `ANY` binding and
 * `batch = true` fires once no matter how many Cats connect at once (CR 603.2c), scoped to Cats
 * you control. "That player" is [com.wingedsheep.sdk.scripting.predicates.ControllerPredicate.ControlledByTriggeringPlayer]
 * (`controlledByTriggeringPlayer()`) — the same predicate Dreadmaw's Ire uses for "destroy target
 * artifact that player controls" — and "up to one target" is `TargetPermanent(optional = true)`,
 * the Webstrike Elite idiom.
 */
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
            filter = GroupFilter(GameObjectFilter.Creature.withSubtype(Subtype.CAT).youControl(), excludeSelf = true)
        )
    }
    staticAbility {
        ability = GrantKeyword(
            keyword = PROTECTION_FROM_DOGS,
            filter = GroupFilter(GameObjectFilter.Creature.withSubtype(Subtype.CAT).youControl(), excludeSelf = true)
        )
    }

    triggeredAbility {
        // CR 603.2c batch trigger: one destroy no matter how many Cats connect at once.
        trigger = Triggers.dealsDamage(
            damageType = DamageType.Combat,
            recipient = RecipientFilter.AnyPlayer,
            sourceFilter = GameObjectFilter.Creature.youControl().withSubtype(Subtype.CAT),
            binding = TriggerBinding.ANY,
            batch = true,
        )
        val t = target(
            "up to one target artifact or enchantment that player controls",
            TargetPermanent(
                optional = true,
                filter = TargetFilter(GameObjectFilter.ArtifactOrEnchantment.controlledByTriggeringPlayer())
            )
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
