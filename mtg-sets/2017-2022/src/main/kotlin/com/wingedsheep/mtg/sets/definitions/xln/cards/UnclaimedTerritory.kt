package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.ChoiceType
import com.wingedsheep.sdk.scripting.EntersWithChoice
import com.wingedsheep.sdk.scripting.TimingRule

/**
 * Unclaimed Territory
 * Land
 * As this land enters, choose a creature type.
 * {T}: Add {C}.
 * {T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen
 * type.
 *
 * Same shape as Cavern of Souls, minus its uncounterable rider.
 */
val UnclaimedTerritory = card("Unclaimed Territory") {
    typeLine = "Land"
    colorIdentity = ""
    oracleText =
        "As this land enters, choose a creature type.\n" +
        "{T}: Add {C}.\n" +
        "{T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type."

    replacementEffect(EntersWithChoice(ChoiceType.CREATURE_TYPE))

    activatedAbility {
        cost = AbilityCost.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
        timing = TimingRule.ManaAbility
    }

    activatedAbility {
        cost = AbilityCost.Tap
        effect = Effects.AddAnyColorManaSpendOnChosenType(creatureOnly = true)
        manaAbility = true
        timing = TimingRule.ManaAbility
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "258"
        artist = "Dimitar Marinski"
        imageUri = "https://cards.scryfall.io/normal/front/f/f/ff765732-6fe3-4594-bff5-6ce47a79f45a.jpg?1783935697"
    }
}
