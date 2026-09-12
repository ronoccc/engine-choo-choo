package com.wingedsheep.mtg.sets.definitions.mar.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EntersWithDevour
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * Ravenous Tyrannosaurus
 * {4}{R}{G}
 * Creature — Dinosaur
 * Devour 3 (As this creature enters, you may sacrifice any number of creatures. It enters with
 * three times that many +1/+1 counters on it.)
 * Whenever this creature attacks, it deals damage equal to its power to up to one other target
 * creature. Excess damage is dealt to that creature's controller instead.
 *
 * Devour is the [KeywordAbility.devour] + [EntersWithDevour] pair from Predator Dragon. The attack
 * trigger is [Effects.DealDamageExcessToController] with [DynamicAmounts.sourcePower] as the
 * amount, targeting an optional other creature.
 */
val RavenousTyrannosaurus = card("Ravenous Tyrannosaurus") {
    manaCost = "{4}{R}{G}"
    colorIdentity = "GR"
    typeLine = "Creature — Dinosaur"
    power = 6
    toughness = 6
    oracleText = "Devour 3 (As this creature enters, you may sacrifice any number of creatures. " +
        "It enters with three times that many +1/+1 counters on it.)\n" +
        "Whenever this creature attacks, it deals damage equal to its power to up to one other " +
        "target creature. Excess damage is dealt to that creature's controller instead."

    keywords(Keyword.DEVOUR)
    keywordAbility(KeywordAbility.devour(3))
    replacementEffect(EntersWithDevour(multiplier = 3))

    triggeredAbility {
        trigger = Triggers.Attacks
        val creature = target(
            "other target creature",
            TargetCreature(optional = true, filter = TargetFilter.OtherCreature)
        )
        effect = Effects.DealDamageExcessToController(DynamicAmounts.sourcePower(), creature)
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "93"
        artist = "Jack Kirby"
        imageUri = "https://cards.scryfall.io/normal/front/d/2/d20ce47d-f08f-475d-a8c3-8a4ebeb0b4f2.jpg?1783903305"
    }
}
