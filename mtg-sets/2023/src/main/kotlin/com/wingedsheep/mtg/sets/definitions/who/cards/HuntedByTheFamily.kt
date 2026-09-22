package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.LoseAllAbilities
import com.wingedsheep.sdk.scripting.SetBasePowerToughnessStatic
import com.wingedsheep.sdk.scripting.TransformPermanent
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.ForEachTargetEffect
import com.wingedsheep.sdk.scripting.effects.GrantStaticAbilityEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.PlayerChoiceEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * Hunted by The Family
 * {5}{U}{U}
 * Sorcery
 *
 * Choose up to four target creatures you don't control. For each of them, that creature's
 * controller faces a villainous choice — That creature becomes a 1/1 white Human creature and
 * loses all abilities, or you create a token that's a copy of it.
 *
 * `ForEachTargetEffect` re-binds `ContextTarget(0)` to each chosen creature in turn (fresh
 * pipeline per iteration), so the nested `PlayerChoiceEffect` with `Chooser.ControllerOfTarget`
 * correctly asks *that* creature's own controller each time, independently — in a multiplayer
 * game the four targets can belong to (and be judged by) up to four different opponents.
 *
 * "Becomes a 1/1 white Human creature and loses all abilities" is the Witness Protection shape
 * (`TransformPermanent` for the Layer 4 subtype replacement / Layer 5 color overwrite,
 * `SetBasePowerToughnessStatic` for Layer 7b, `LoseAllAbilities` for Layer 6) granted directly
 * onto the target permanently via `GrantStaticAbilityEffect(..., duration = Duration.Permanent)`,
 * rather than riding an Aura — this is a one-shot sorcery, not an enchant effect.
 */
val HuntedByTheFamily = card("Hunted by The Family") {
    manaCost = "{5}{U}{U}"
    colorIdentity = "U"
    typeLine = "Sorcery"
    oracleText = "Choose up to four target creatures you don't control. For each of them, that " +
        "creature's controller faces a villainous choice — That creature becomes a 1/1 white " +
        "Human creature and loses all abilities, or you create a token that's a copy of it."

    spell {
        target(
            "up to four target creatures you don't control",
            TargetCreature(filter = TargetFilter.CreatureOpponentControls, count = 4, optional = true)
        )

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

        effect = ForEachTargetEffect(
            listOf(
                PlayerChoiceEffect(
                    chooser = Chooser.ControllerOfTarget,
                    options = listOf(
                        Mode.noTarget(becomesVanillaHuman, "That creature becomes a 1/1 white Human creature and loses all abilities"),
                        Mode.noTarget(
                            Effects.CreateTokenCopyOfTarget(EffectTarget.ContextTarget(0)),
                            "You create a token that's a copy of it"
                        )
                    )
                )
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
