package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.LoseAllAbilities
import com.wingedsheep.sdk.scripting.SetBasePowerToughnessStatic
import com.wingedsheep.sdk.scripting.TransformPermanent
import com.wingedsheep.sdk.scripting.effects.EffectChoice
import com.wingedsheep.sdk.scripting.effects.GrantStaticAbilityEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Hunted by The Family
 * {5}{U}{U}
 * Sorcery
 *
 * Choose up to four target creatures you don't control. For each of them, that creature's
 * controller faces a villainous choice — That creature becomes a 1/1 white Human creature and
 * loses all abilities, or you create a token that's a copy of it.
 */
val HuntedByTheFamily = card("Hunted by The Family") {
    manaCost = "{5}{U}{U}"
    colorIdentity = "U"
    typeLine = "Sorcery"
    oracleText = "Choose up to four target creatures you don't control. For each of them, that " +
        "creature's controller faces a villainous choice — That creature becomes a 1/1 white " +
        "Human creature and loses all abilities, or you create a token that's a copy of it."

    spell {
        val becomesVanillaHuman = Effects.Composite(
            listOf(
                GrantStaticAbilityEffect(
                    ability = TransformPermanent(
                        setCardTypes = setOf("CREATURE"),
                        setSubtypes = setOf("Human"),
                        setColors = setOf(Color.WHITE)
                    ),
                    target = EffectTarget.ContextTarget(0),
                    duration = Duration.Permanent
                ),
                GrantStaticAbilityEffect(
                    ability = SetBasePowerToughnessStatic(1, 1),
                    target = EffectTarget.ContextTarget(0),
                    duration = Duration.Permanent
                ),
                GrantStaticAbilityEffect(
                    ability = LoseAllAbilities(),
                    target = EffectTarget.ContextTarget(0),
                    duration = Duration.Permanent
                )
            )
        )

        targets(TargetFilter.CreatureOpponentControls, count = 4, optional = true)
        effect = Effects.ForEachTarget(
            Effects.ChooseAction(
                choices = listOf(
                    EffectChoice(
                        label = "That creature becomes a 1/1 white Human creature and loses all abilities",
                        effect = becomesVanillaHuman
                    ),
                    EffectChoice(
                        label = "You create a token that's a copy of it",
                        effect = Effects.CreateTokenCopyOfTarget(EffectTarget.ContextTarget(0))
                    )
                ),
                player = EffectTarget.TargetController
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "46"
        artist = "Darren Tan"
        imageUri = "https://cards.scryfall.io/normal/front/0/4/04b0a2a9-ba95-4330-a8ab-9cb530eb4257.jpg?1783914670"
    }
}
