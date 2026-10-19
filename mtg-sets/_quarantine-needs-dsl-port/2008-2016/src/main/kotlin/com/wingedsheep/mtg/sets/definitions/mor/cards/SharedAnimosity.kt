package com.wingedsheep.mtg.sets.definitions.mor.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityReference

/**
 * Shared Animosity
 * {2}{R}
 * Enchantment
 * Whenever a creature you control attacks, it gets +1/+0 until end of turn for each other
 * attacking creature that shares a creature type with it.
 *
 * A per-attacker triggered pump, not a continuous anthem: the oracle ruling confirms this counts
 * *creatures*, not creature types — a creature with five creature types among five attackers still
 * contributes at most one to each other attacker's count, and a creature sharing several types with
 * several different attackers still counts each qualifying attacker exactly once. `AggregateBattlefield`
 * already does that (COUNT over a filter, no double-counting for multi-type matches).
 *
 * The exclusion is the interesting part. "Each OTHER attacking creature that shares a type with it"
 * needs to drop the *triggering* creature itself from its own count — not this enchantment (the
 * ability's source), which [DynamicAmount.AggregateBattlefield.excludeSelf] would resolve to instead
 * and which isn't a creature, so excludeSelf would silently exclude nothing. That gap is what
 * [DynamicAmount.AggregateBattlefield.excludeEntity] is for: naming the entity to drop by reference
 * rather than assuming it's the source, resolved here as `EntityReference.Triggering` — the same
 * attacker `sharingCreatureTypeWith` is being asked about.
 *
 * `Player.Each` (not `Player.You`) because the 2008-04-01 ruling is explicit that in a multiplayer
 * team game, teammates' (and by extension any other) attacking creatures are included in the count
 * even though only *your* creatures trigger the ability and get the bonus.
 *
 * Each attacking creature you control triggers this separately ([TriggerBinding.ANY] over
 * `Triggers.attacks`), and the amount is evaluated fresh at each trigger's own resolution — the
 * standard "amount computed at resolution, not when put on the stack" rule for pump effects, so a
 * creature that leaves combat after some of these triggers have already resolved (but before others
 * do) correctly shrinks the count for the ones still on the stack.
 */
val SharedAnimosity = card("Shared Animosity") {
    manaCost = "{2}{R}"
    colorIdentity = "R"
    typeLine = "Enchantment"
    oracleText = "Whenever a creature you control attacks, it gets +1/+0 until end of turn for each " +
        "other attacking creature that shares a creature type with it."

    triggeredAbility {
        trigger = Triggers.attacks(filter = GameObjectFilter.Creature.youControl(), binding = TriggerBinding.ANY)
        effect = Effects.ModifyStats(
            power = DynamicAmount.AggregateBattlefield(
                player = Player.Each,
                filter = GameObjectFilter.Creature.attacking().sharingCreatureTypeWith(EntityReference.Triggering),
                excludeEntity = EntityReference.Triggering,
            ),
            toughness = DynamicAmount.Fixed(0),
            target = EffectTarget.TriggeringEntity,
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "104"
        artist = "Chuck Lukacs"
        flavorText = "\"It is the nature of souls that they burn more brightly together than apart.\"\n" +
            "—Vessifrus, flamekin demagogue"
        imageUri = "https://cards.scryfall.io/normal/front/f/e/fe332c46-90f0-4cc0-8bf1-35a3934ff8a0.jpg?1783942783"
        ruling(
            "2008-04-01",
            "This ability counts creatures, not creature types. For example, if you attack with five " +
                "creatures — an Elf Shaman, an Elf Warrior, a Goblin Shaman, an Elemental, and a creature " +
                "with all creature types — the ability will trigger five times. Those creatures will get " +
                "+3/+0, +2/+0, +2/+0, +1/+0, and +4/+0, respectively.",
        )
        ruling(
            "2008-04-01",
            "In a Two-Headed Giant game, only creatures you control trigger the ability and get the " +
                "bonus, but your teammate's attacking creatures are included in the calculation of those " +
                "bonuses.",
        )
    }
}
