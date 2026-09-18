package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.CreatePredefinedTokenEffect
import com.wingedsheep.sdk.scripting.effects.ForEachTargetEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetPermanent
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Pest Infestation
 * {X}{X}{G}
 * Sorcery
 * Destroy up to X target artifacts and/or enchantments. Create twice X 1/1 black and green Pest
 * creature tokens with "When this token dies, you gain 1 life."
 */
val PestInfestation = card("Pest Infestation") {
    manaCost = "{X}{X}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    oracleText = "Destroy up to X target artifacts and/or enchantments. Create twice X 1/1 black " +
        "and green Pest creature tokens with \"When this token dies, you gain 1 life.\""

    spell {
        target(
            "up to X target artifacts and/or enchantments",
            TargetPermanent(
                optional = true,
                unlimited = true,
                dynamicMaxCount = DynamicAmount.XValue,
                filter = TargetFilter.ArtifactOrEnchantment,
            )
        )
        effect = ForEachTargetEffect(listOf(Effects.Destroy(EffectTarget.ContextTarget(0)))) then
            CreatePredefinedTokenEffect(
                tokenType = "Pest",
                dynamicCount = DynamicAmount.Multiply(DynamicAmount.XValue, 2),
            )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "65"
        artist = "Brian Valeza"
        flavorText = "A thousand years of history undone by one juvenile prank."
        imageUri = "https://cards.scryfall.io/normal/front/4/7/4720b4f2-e6af-4223-9250-a0ed21ed5693.jpg?1783927588"
    }
}
