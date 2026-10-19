package com.wingedsheep.mtg.sets.definitions.usg.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.OnEnterRunEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.effects.PayAnyAmountOfLifeAsEntersEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty
import com.wingedsheep.sdk.scripting.values.EntityReference

/**
 * Phyrexian Processor
 * {4}
 * Artifact
 * As this artifact enters, pay any amount of life.
 * {4}, {T}: Create an X/X black Phyrexian Minion creature token, where X is the life paid as
 * this artifact entered.
 */
val PhyrexianProcessor = card("Phyrexian Processor") {
    manaCost = "{4}"
    colorIdentity = ""
    typeLine = "Artifact"
    oracleText = "As this artifact enters, pay any amount of life.\n{4}, {T}: Create an X/X black " +
        "Phyrexian Minion creature token, where X is the life paid as this artifact entered."

    replacementEffect(
        OnEnterRunEffect(
            PayAnyAmountOfLifeAsEntersEffect(maxAmount = DynamicAmount.LifeTotal(Player.You))
        )
    )

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{4}"), Costs.Tap)
        effect = CreateTokenEffect(
            power = 0,
            toughness = 0,
            colors = setOf(Color.BLACK),
            creatureTypes = setOf("Phyrexian", "Minion"),
            dynamicPower = DynamicAmount.EntityProperty(EntityReference.Source, EntityNumericProperty.ValueChosenAsEntered),
            dynamicToughness = DynamicAmount.EntityProperty(EntityReference.Source, EntityNumericProperty.ValueChosenAsEntered),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "306"
        artist = "Ron Spencer"
        imageUri = "https://cards.scryfall.io/normal/front/6/8/6875ce99-badd-44da-8e5d-509600efa1d0.jpg?1783946304"
    }
}
