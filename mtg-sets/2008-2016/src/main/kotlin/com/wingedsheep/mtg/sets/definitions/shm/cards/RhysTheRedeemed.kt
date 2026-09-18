package com.wingedsheep.mtg.sets.definitions.shm.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Rhys the Redeemed
 * {G/W}
 * Legendary Creature — Elf Warrior
 * 1/1
 * {2}{G/W}, {T}: Create a 1/1 green and white Elf Warrior creature token.
 * {4}{G/W}{G/W}, {T}: For each creature token you control, create a token that's a copy of that
 * creature.
 *
 * The doubling ability is the same primitive Second Harvest already established:
 * `ForEachInGroup` over token creatures you control, copying each ([EffectTarget.Self] = the
 * iterated token).
 */
val RhysTheRedeemed = card("Rhys the Redeemed") {
    manaCost = "{G/W}"
    colorIdentity = "GW"
    typeLine = "Legendary Creature — Elf Warrior"
    power = 1
    toughness = 1
    oracleText = "{2}{G/W}, {T}: Create a 1/1 green and white Elf Warrior creature token.\n" +
        "{4}{G/W}{G/W}, {T}: For each creature token you control, create a token that's a copy " +
        "of that creature."

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{2}{G/W}"), Costs.Tap)
        effect = Effects.CreateToken(
            power = 1,
            toughness = 1,
            colors = setOf(Color.GREEN, Color.WHITE),
            creatureTypes = setOf("Elf", "Warrior"),
        )
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{4}{G/W}{G/W}"), Costs.Tap)
        effect = Effects.ForEachInGroup(
            filter = GroupFilter(GameObjectFilter.Creature.token().youControl()),
            effect = CreateTokenCopyOfTargetEffect(target = EffectTarget.Self)
        )
        description = "For each creature token you control, create a token that's a copy of " +
            "that creature."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "237"
        artist = "Steve Prescott"
        flavorText = "Whole again in honor and horn."
        imageUri = "https://cards.scryfall.io/normal/front/5/9/59327a58-cd41-479c-81c7-31c9d3b29508.jpg?1783942715"
    }
}
