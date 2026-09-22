package com.wingedsheep.mtg.sets.definitions.kld.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.CostModification
import com.wingedsheep.sdk.scripting.CostReductionSource
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifySpellCost
import com.wingedsheep.sdk.scripting.SpellCostTarget
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty

/**
 * Metalwork Colossus
 * {11}
 * Artifact Creature — Construct
 * 10/10
 *
 * This spell costs {X} less to cast, where X is the total mana value of noncreature artifacts
 * you control.
 * Sacrifice two artifacts: Return this card from your graveyard to your hand.
 *
 * Cost reduction is the Ghalta/Lord-of-the-Eagles rail: `SelfCast` + `ReduceGenericBy` sourced
 * from `TotalPropertyAmongPermanentsYouControl(ManaValue, Artifact.notCreature())` — summed mana
 * value, not count, per the printed wording. The graveyard ability mirrors Sanitarium Skeleton's
 * "return this card from your graveyard to your hand" shape, gated on `Costs.SacrificeMultiple`.
 */
val MetalworkColossus = card("Metalwork Colossus") {
    manaCost = "{11}"
    colorIdentity = ""
    typeLine = "Artifact Creature — Construct"
    power = 10
    toughness = 10
    oracleText = "This spell costs {X} less to cast, where X is the total mana value of noncreature " +
        "artifacts you control.\n" +
        "Sacrifice two artifacts: Return this card from your graveyard to your hand."

    staticAbility {
        ability = ModifySpellCost(
            target = SpellCostTarget.SelfCast,
            modification = CostModification.ReduceGenericBy(
                CostReductionSource.TotalPropertyAmongPermanentsYouControl(
                    property = EntityNumericProperty.ManaValue,
                    filter = GameObjectFilter.Artifact.notCreature()
                )
            ),
        )
    }

    activatedAbility {
        cost = Costs.SacrificeMultiple(2, GameObjectFilter.Artifact)
        effect = Effects.ReturnToHandFromGraveyard(EffectTarget.Self)
        activateFromZone = Zone.GRAVEYARD
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "222"
        artist = "Jaime Jones"
        imageUri = "https://cards.scryfall.io/normal/front/4/7/474480b5-c60b-4c7f-9d3e-751bca43d074.jpg?1783937152"
    }
}
