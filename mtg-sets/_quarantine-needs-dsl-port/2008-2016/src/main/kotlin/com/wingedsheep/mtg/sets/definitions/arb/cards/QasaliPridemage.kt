package com.wingedsheep.mtg.sets.definitions.arb.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.events.AttackPredicate
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Qasali Pridemage — Alara Reborn #75.
 *
 * Exalted (CR 702.90) has no dedicated keyword in this SDK — like Rogue Kavu and other
 * "attacks alone" cards, it composes directly: [Triggers.attacks] with
 * [AttackPredicate.Alone] and `ANY` binding fires whenever *a* creature you control attacks
 * alone (not just this one), and [Effects.ModifyStats] targets the attacker via
 * [EffectTarget.TriggeringEntity] with its default until-end-of-turn duration. Multiple exalted
 * sources each register their own trigger and stack naturally, matching the 2021-03-19 ruling
 * that every exalted ability a player controls triggers off the same single-attacker declaration.
 *
 * The sacrifice ability is the plain Caustic Caterpillar shape: mana + sacrifice-self cost,
 * destroy target artifact or enchantment.
 */
val QasaliPridemage = card("Qasali Pridemage") {
    manaCost = "{G}{W}"
    colorIdentity = "GW"
    typeLine = "Creature — Cat Wizard"
    power = 2
    toughness = 2
    oracleText = "Exalted (Whenever a creature you control attacks alone, that creature gets +1/+1 " +
        "until end of turn.)\n" +
        "{1}, Sacrifice this creature: Destroy target artifact or enchantment."

    triggeredAbility {
        trigger = Triggers.attacks(
            filter = GameObjectFilter.Creature.youControl(),
            requires = setOf(AttackPredicate.Alone),
            binding = TriggerBinding.ANY
        )
        effect = Effects.ModifyStats(1, 1, EffectTarget.TriggeringEntity)
        description = "Exalted (Whenever a creature you control attacks alone, that creature gets " +
            "+1/+1 until end of turn.)"
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{1}"), Costs.SacrificeSelf)
        val t = target("target artifact or enchantment", Targets.ArtifactOrEnchantment)
        effect = Effects.Destroy(t)
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "75"
        artist = "Chris Rahn"
        flavorText = "An elder in one pride, of the Sigiled caste in another."
        imageUri = "https://cards.scryfall.io/normal/front/7/5/75256550-bbe0-499d-909e-af10691749c2.jpg?1783942425"
        ruling(
            "2021-03-19",
            "You must attack with exactly one creature for exalted abilities to trigger. They won't " +
                "trigger if you attack a player with one creature and a planeswalker with another, " +
                "for example, or if you attack with two creatures but one is removed from combat."
        )
    }
}
