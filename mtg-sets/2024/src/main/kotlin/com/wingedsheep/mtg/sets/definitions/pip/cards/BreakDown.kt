package com.wingedsheep.mtg.sets.definitions.pip.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.ActivatedAbility
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.effects.MayPlayExpiry
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetPermanent
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Break Down
 * {2}{G}
 * Instant
 * Destroy target artifact or enchantment. Create a Junk token. (It's an artifact with "{T},
 * Sacrifice this token: Exile the top card of your library. You may play that card this turn.
 * Activate only as a sorcery.")
 */
val BreakDown = card("Break Down") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Instant"
    oracleText = "Destroy target artifact or enchantment. Create a Junk token. (It's an artifact " +
        "with \"{T}, Sacrifice this token: Exile the top card of your library. You may play that " +
        "card this turn. Activate only as a sorcery.\")"

    spell {
        val t = target(
            "target artifact or enchantment",
            TargetPermanent(filter = TargetFilter.ArtifactOrEnchantment)
        )
        effect = Effects.Destroy(t) then CreateTokenEffect(
            count = DynamicAmount.Fixed(1),
            power = 0,
            toughness = 0,
            colors = emptySet(),
            creatureTypes = emptySet(),
            artifactToken = true,
            name = "Junk",
            activatedAbilities = listOf(
                ActivatedAbility(
                    cost = AbilityCost.Composite(listOf(AbilityCost.Tap, AbilityCost.SacrificeSelf)),
                    effect = Patterns.Exile.impulse(count = 1, expiry = MayPlayExpiry.EndOfTurn),
                    timing = TimingRule.SorcerySpeed,
                )
            ),
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "74"
        artist = "Fesbra"
        flavorText = "\"Old human junk. Worst kind of human junk.\""
        imageUri = "https://cards.scryfall.io/normal/front/3/3/3389f9d7-b8ae-4c51-90f9-b6385517914a.jpg?1783912399"
    }
}
