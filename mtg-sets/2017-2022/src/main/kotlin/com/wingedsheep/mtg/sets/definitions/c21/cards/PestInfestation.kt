package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.TriggeredAbility
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject
import com.wingedsheep.sdk.scripting.values.DynamicAmount

val PestInfestation = card("Pest Infestation") {
    manaCost = "{X}{X}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    oracleText = "Destroy up to X target artifacts and/or enchantments. Create twice X 1/1 black " +
        "and green Pest creature tokens with \"When this token dies, you gain 1 life.\""

    spell {
        target = TargetObject(
            optional = true,
            unlimited = true,
            dynamicMaxCount = DynamicAmounts.xValue(),
            filter = TargetFilter.ArtifactOrEnchantment,
        )
        effect = Effects.ForEachTarget(Effects.Destroy(EffectTarget.ContextTarget(0))) then
            Effects.CreateToken(
                power = 1,
                toughness = 1,
                colors = setOf(Color.BLACK, Color.GREEN),
                creatureTypes = setOf("Pest"),
                count = DynamicAmount.Multiply(DynamicAmounts.xValue(), 2),
                triggeredAbilities = listOf(
                    TriggeredAbility.create(
                        trigger = Triggers.self.dies(),
                        effect = Effects.GainLife(1),
                    ),
                ),
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
