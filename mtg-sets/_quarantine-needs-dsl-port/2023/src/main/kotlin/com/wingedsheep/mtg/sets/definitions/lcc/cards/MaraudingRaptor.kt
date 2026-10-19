package com.wingedsheep.mtg.sets.definitions.lcc.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.CostModification
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifySpellCost
import com.wingedsheep.sdk.scripting.SpellCostTarget
import com.wingedsheep.sdk.scripting.events.RecipientFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Marauding Raptor
 * {1}{R}
 * Creature — Dinosaur
 * Creature spells you cast cost {1} less to cast.
 * Whenever another creature you control enters, this creature deals 2 damage to it. If a
 * Dinosaur is dealt damage this way, this creature gets +2/+0 until end of turn.
 *
 * The cost reduction is Goblin Warchief's [ModifySpellCost] shape, widened from a subtype filter
 * to [GameObjectFilter.Creature]. The pump is a second, separate triggered ability keyed off this
 * creature dealing damage to a Dinosaur ([Triggers.dealsDamage] with [RecipientFilter.Matching]) —
 * Marauding Raptor has no other way to deal damage, so this reproduces "dealt damage this way"
 * without a bespoke inline conditional.
 */
val MaraudingRaptor = card("Marauding Raptor") {
    manaCost = "{1}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Dinosaur"
    power = 2
    toughness = 3
    oracleText = "Creature spells you cast cost {1} less to cast.\n" +
        "Whenever another creature you control enters, this creature deals 2 damage to it. If a " +
        "Dinosaur is dealt damage this way, this creature gets +2/+0 until end of turn."

    staticAbility {
        ability = ModifySpellCost(
            target = SpellCostTarget.YouCast(GameObjectFilter.Creature),
            modification = CostModification.ReduceGeneric(1),
        )
    }

    triggeredAbility {
        trigger = Triggers.OtherCreatureEnters
        effect = Effects.DealDamage(2, EffectTarget.TriggeringEntity, damageSource = EffectTarget.Self)
    }

    triggeredAbility {
        trigger = Triggers.dealsDamage(
            recipient = RecipientFilter.Matching(GameObjectFilter.Creature.withSubtype("Dinosaur"))
        )
        effect = Effects.ModifyStats(power = 2, toughness = 0, target = EffectTarget.Self)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "228"
        artist = "Bayard Wu"
        imageUri = "https://cards.scryfall.io/normal/front/0/b/0b5cf2aa-f6dd-47d2-a57f-0ae2308c0f9a.jpg?1783913864"
    }
}
