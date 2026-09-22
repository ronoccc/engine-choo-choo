package com.wingedsheep.mtg.sets.definitions.aer.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.conditions.ComparisonOperator
import com.wingedsheep.sdk.scripting.effects.ConditionalEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetPermanent
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Mechanized Production — Aether Revolt #38
 * {2}{U}{U} · Enchantment — Aura
 *
 * Enchant artifact you control. At the beginning of your upkeep, create a token that's a copy of
 * enchanted artifact. Then if you control eight or more artifacts with the same name as one
 * another, you win the game.
 *
 * Modeling notes:
 *  - `Effects.CreateTokenCopyOfTarget(EffectTarget.EnchantedPermanent)` produces the yearly token
 *    copy; the win check is a `ConditionalEffect` sequenced *after* the copy, matching the "Then
 *    if" wording (the token just created can push the count over eight the very turn it enters).
 *  - **New SDK primitive**: `Aggregation.MAX_NAME_GROUP_SIZE` / `DynamicAmounts.battlefield(...)
 *    .maxNameGroupSize()` — the size of the *largest* same-named group among matched permanents,
 *    the complement of the existing `DISTINCT_NAMES` aggregation (which counts how many different
 *    names are present, not how big the biggest pile is). No prior card needed "N of the same
 *    name", so this is genuinely new vocabulary; documented in
 *    `docs/card-sdk-language-reference.md` alongside `DISTINCT_NAMES`.
 */
val MechanizedProduction = card("Mechanized Production") {
    manaCost = "{2}{U}{U}"
    colorIdentity = "U"
    typeLine = "Enchantment — Aura"
    oracleText = "Enchant artifact you control\n" +
        "At the beginning of your upkeep, create a token that's a copy of enchanted artifact. " +
        "Then if you control eight or more artifacts with the same name as one another, you win " +
        "the game."

    auraTarget = TargetPermanent(filter = TargetFilter(GameObjectFilter.Artifact.youControl()))

    triggeredAbility {
        trigger = Triggers.YourUpkeep
        effect = Effects.Composite(
            Effects.CreateTokenCopyOfTarget(EffectTarget.EnchantedPermanent),
            ConditionalEffect(
                condition = Conditions.CompareAmounts(
                    DynamicAmounts.battlefield(Player.You, GameObjectFilter.Artifact).maxNameGroupSize(),
                    ComparisonOperator.GTE,
                    DynamicAmount.Fixed(8)
                ),
                effect = Effects.WinGame()
            )
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "38"
        artist = "Adam Paquette"
        flavorText = "\"Give me eight walkers, I'll give you the city.\"\n" +
            "—Dovin Baan"
        imageUri = "https://cards.scryfall.io/normal/front/2/3/235dd8f1-215a-4b0a-9e94-0d0d5a3c730b.jpg?1783936771"
    }
}
