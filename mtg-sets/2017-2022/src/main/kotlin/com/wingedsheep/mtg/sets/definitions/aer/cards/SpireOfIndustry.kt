package com.wingedsheep.mtg.sets.definitions.aer.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ActivationRestriction
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TimingRule

/**
 * Spire of Industry — Aether Revolt #184
 * Land
 *
 * {T}: Add {C}.
 * {T}, Pay 1 life: Add one mana of any color. Activate only if you control an artifact.
 *
 * The colorless ability is a plain [Costs.Tap] mana ability. The colored ability adds
 * [Costs.PayLife] to the tap cost and gates activation on controlling at least one artifact,
 * mirroring [com.wingedsheep.mtg.sets.definitions.som.cards.MoxOpal]'s
 * [ActivationRestriction.OnlyIfCondition] usage.
 */
val SpireOfIndustry = card("Spire of Industry") {
    typeLine = "Land"
    colorIdentity = ""
    oracleText = "{T}: Add {C}.\n" +
        "{T}, Pay 1 life: Add one mana of any color. Activate only if you control an artifact."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
        timing = TimingRule.ManaAbility
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Tap, Costs.PayLife(1))
        effect = Effects.AddAnyColorMana(1)
        manaAbility = true
        timing = TimingRule.ManaAbility
        restrictions = listOf(
            ActivationRestriction.OnlyIfCondition(
                Conditions.YouControlAtLeast(1, GameObjectFilter.Artifact)
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "184"
        artist = "John Avon"
        flavorText = "A beacon of prosperity to some, a shadow of oppression to others."
        imageUri = "https://cards.scryfall.io/normal/front/8/3/8331724d-6fab-454a-b06c-b06e499fa552.jpg?1783936719"
    }
}
