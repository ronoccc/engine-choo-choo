package com.wingedsheep.mtg.sets.definitions.c14.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.TimingRule

/**
 * Commander's Sphere — Commander 2014 #54
 * {3} · Artifact
 *
 * {T}: Add one mana of any color in your commander's color identity.
 * Sacrifice this artifact: Draw a card.
 *
 * The mana ability mirrors [com.wingedsheep.mtg.sets.definitions.cmd.cards.CommandTower]'s
 * [Effects.AddManaOfColorInCommanderColorIdentity]. The sacrifice ability is the standard
 * "Sacrifice this artifact: Draw a card" shape (no tap symbol in the cost, so it can be sacrificed
 * even while tapped).
 */
val CommandersSphere = card("Commander's Sphere") {
    manaCost = "{3}"
    colorIdentity = ""
    typeLine = "Artifact"
    oracleText = "{T}: Add one mana of any color in your commander's color identity.\n" +
        "Sacrifice this artifact: Draw a card."

    activatedAbility {
        cost = AbilityCost.Tap
        effect = Effects.AddManaOfColorInCommanderColorIdentity()
        manaAbility = true
        timing = TimingRule.ManaAbility
    }

    activatedAbility {
        cost = Costs.SacrificeSelf
        effect = Effects.DrawCards(1)
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "54"
        artist = "Ryan Alexander Lee"
        flavorText = "It harmonizes with the essence of its master."
        imageUri = "https://cards.scryfall.io/normal/front/6/1/61e0a922-1689-484f-abb3-902ce1c964ca.jpg?1783938863"
    }
}
