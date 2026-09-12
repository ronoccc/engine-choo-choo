package com.wingedsheep.mtg.sets.definitions.aer.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Heroic Intervention
 * {1}{G}
 * Instant
 * Permanents you control gain hexproof and indestructible until end of turn.
 *
 * Same "for each in group, grant a keyword" shape as Selfless Spirit's indestructible ability,
 * generalized from creatures to all permanents and chained across both keywords. Both
 * [Effects.GrantKeyword] calls default to `Duration.EndOfTurn`, matching the printed duration,
 * so neither needs to name it explicitly. The affected set is fixed when the spell resolves
 * (mirrors Selfless Spirit's own ruling) — a permanent you gain control of later this turn
 * doesn't retroactively gain either keyword.
 */
val HeroicIntervention = card("Heroic Intervention") {
    manaCost = "{1}{G}"
    colorIdentity = "G"
    typeLine = "Instant"
    oracleText = "Permanents you control gain hexproof and indestructible until end of turn."

    spell {
        effect = Effects.ForEachInGroup(
            GroupFilter(GameObjectFilter.Permanent.youControl()),
            Effects.GrantKeyword(Keyword.HEXPROOF, EffectTarget.Self)
                .then(Effects.GrantKeyword(Keyword.INDESTRUCTIBLE, EffectTarget.Self))
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "109"
        artist = "James Ryman"
        flavorText = "\"Wherever the strong would harm the weak, I will be there.\"\n—Ajani Goldmane"
        imageUri = "https://cards.scryfall.io/normal/front/8/f/8f5a620c-fde7-4b72-bf8a-efc4f14560c5.jpg?1783936743"
    }
}
