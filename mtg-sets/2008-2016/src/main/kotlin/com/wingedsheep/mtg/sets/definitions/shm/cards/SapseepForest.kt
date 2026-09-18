package com.wingedsheep.mtg.sets.definitions.shm.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ActivationRestriction
import com.wingedsheep.sdk.scripting.EntersTapped
import com.wingedsheep.sdk.scripting.GameObjectFilter

/**
 * Sapseep Forest
 * Land — Forest
 * ({T}: Add {G}.)
 * This land enters tapped.
 * {G}, {T}: You gain 1 life. Activate only if you control two or more green permanents.
 */
val SapseepForest = card("Sapseep Forest") {
    colorIdentity = "G"
    typeLine = "Land — Forest"
    oracleText = "({T}: Add {G}.)\nThis land enters tapped.\n{G}, {T}: You gain 1 life. Activate " +
        "only if you control two or more green permanents."

    // Mana ability is intrinsic from the basic land type in the type line.

    replacementEffect(EntersTapped())

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{G}"), Costs.Tap)
        effect = Effects.GainLife(1)
        restrictions = listOf(
            ActivationRestriction.OnlyIfCondition(
                Conditions.YouControlAtLeast(2, GameObjectFilter.Permanent.withColor(com.wingedsheep.sdk.core.Color.GREEN))
            )
        )
        description = "You gain 1 life. Activate only if you control two or more green permanents."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "279"
        artist = "Aleksi Briclot"
        imageUri = "https://cards.scryfall.io/normal/front/d/1/d1e29d9f-6aa9-49ef-8484-54af216aa509.jpg?1783942705"
    }
}
