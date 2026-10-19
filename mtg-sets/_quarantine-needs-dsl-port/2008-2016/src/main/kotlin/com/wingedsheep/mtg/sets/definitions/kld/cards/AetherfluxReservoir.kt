package com.wingedsheep.mtg.sets.definitions.kld.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Aetherflux Reservoir
 * {4}
 * Artifact
 * Whenever you cast a spell, you gain 1 life for each spell you've cast this turn.
 * Pay 50 life: This artifact deals 50 damage to any target.
 */
val AetherfluxReservoir = card("Aetherflux Reservoir") {
    manaCost = "{4}"
    colorIdentity = ""
    typeLine = "Artifact"
    oracleText = "Whenever you cast a spell, you gain 1 life for each spell you've cast this turn.\n" +
        "Pay 50 life: This artifact deals 50 damage to any target."

    triggeredAbility {
        trigger = Triggers.YouCastSpell
        effect = Effects.GainLife(DynamicAmounts.spellsCastThisTurn())
    }

    activatedAbility {
        cost = Costs.PayLife(50)
        val anyTarget = target("any target", Targets.Any)
        effect = Effects.DealDamage(50, anyTarget)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "192"
        artist = "Cliff Childs"
        flavorText = "While most power is used for ordinary, everyday tasks, one shouldn't " +
            "underestimate its potential for the extraordinary."
        imageUri = "https://cards.scryfall.io/normal/front/9/6/96b6b2e1-c3e6-464c-8a13-b15deb34e862.jpg?1783937164"
    }
}
