package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ActivationRestriction
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.conditions.Compare
import com.wingedsheep.sdk.scripting.conditions.ComparisonOperator
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Speaker of the Heavens
 * {W}
 * Creature — Human Cleric
 * 1/1
 * Vigilance, lifelink
 * {T}: Create a 4/4 white Angel creature token with flying. Activate only if you have at least 7
 * life more than your starting life total and only as a sorcery.
 */
val SpeakerOfTheHeavens = card("Speaker of the Heavens") {
    manaCost = "{W}"
    colorIdentity = "W"
    typeLine = "Creature — Human Cleric"
    power = 1
    toughness = 1
    oracleText = "Vigilance, lifelink\n" +
        "{T}: Create a 4/4 white Angel creature token with flying. Activate only if you have at " +
        "least 7 life more than your starting life total and only as a sorcery."

    keywords(Keyword.VIGILANCE, Keyword.LIFELINK)

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.CreateToken(
            power = 4,
            toughness = 4,
            colors = setOf(Color.WHITE),
            creatureTypes = setOf("Angel"),
            keywords = setOf(Keyword.FLYING)
        )
        timing = TimingRule.SorcerySpeed
        restrictions = listOf(
            ActivationRestriction.OnlyIfCondition(
                Compare(
                    left = DynamicAmount.LifeTotal(Player.You),
                    operator = ComparisonOperator.GTE,
                    right = DynamicAmount.Add(DynamicAmount.StartingLifeTotal(Player.You), DynamicAmount.Fixed(7))
                )
            )
        )
        description = "Create a 4/4 white Angel creature token with flying. Activate only if you " +
            "have at least 7 life more than your starting life total and only as a sorcery."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "38"
        artist = "Randy Vargas"
        flavorText = "\"All are welcome in the angels' light.\""
        imageUri = "https://cards.scryfall.io/normal/front/1/f/1f44b96a-8498-414a-a4ac-54c80dfa9f23.jpg?1783930732"
    }
}
