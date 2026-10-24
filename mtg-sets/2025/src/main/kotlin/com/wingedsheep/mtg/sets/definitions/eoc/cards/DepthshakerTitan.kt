package com.wingedsheep.mtg.sets.definitions.eoc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.DelayedTriggerTiming
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Depthshaker Titan
 * {5}{R}{R}
 * Artifact Creature — Robot
 * 5/5
 *
 * When this creature enters, any number of target noncreature artifacts you control become 3/3
 * artifact creatures. Sacrifice them at the beginning of the next end step.
 * Each artifact creature you control has melee, trample, and haste. (Whenever a creature with
 * melee attacks, it gets +1/+1 until end of turn for each opponent you attacked this combat.)
 *
 * The ETB is an unbounded target set (`unlimited = true`, not a huge `count` — the DSL allocates
 * one handle per count while the card loads). Each chosen artifact is animated, then gathered so
 * the next end step's delayed trigger can sacrifice that same set (`carryCollections`).
 *
 * Fidelity gap: `melee` has no numeric behavior in the engine yet (see `Keyword.MELEE`'s doc
 * comment) — there is no `DynamicAmount` for "opponents you attacked this combat" to hang the
 * +1/+1-per-opponent trigger on. The keyword is granted for display/rules-text fidelity; trample
 * and haste are fully functional.
 */
val DepthshakerTitan = card("Depthshaker Titan") {
    manaCost = "{5}{R}{R}"
    colorIdentity = "R"
    typeLine = "Artifact Creature — Robot"
    power = 5
    toughness = 5
    oracleText = "When this creature enters, any number of target noncreature artifacts you " +
        "control become 3/3 artifact creatures. Sacrifice them at the beginning of the next end " +
        "step.\n" +
        "Each artifact creature you control has melee, trample, and haste. (Whenever a creature " +
        "with melee attacks, it gets +1/+1 until end of turn for each opponent you attacked this " +
        "combat.)"

    triggeredAbility {
        trigger = Triggers.self.enters()
        targets(
            TargetFilter(GameObjectFilter.Artifact.notCreature().youControl()),
            unlimited = true,
        )
        effect = Effects.Pipeline {
            run(Effects.ForEachTarget(listOf(
                Effects.BecomeCreature(
                    target = EffectTarget.ContextTarget(0),
                    power = 3,
                    toughness = 3
                )
            )))
            val animated = gather(CardSource.ChosenTargets)
            run(Effects.CreateDelayedTrigger(
                step = Step.END,
                timing = DelayedTriggerTiming.NEXT_END_STEP,
                effect = Effects.Pipeline { sacrifice(animated) },
                carryCollections = listOf(animated.key)
            ))
        }
        description = "When this creature enters, any number of target noncreature artifacts you " +
            "control become 3/3 artifact creatures. Sacrifice them at the beginning of the next " +
            "end step."
    }

    // TODO: melee — Keyword.MELEE is not in the current mtg-sdk; trample and haste are granted below.
    staticAbility {
        ability = GrantKeyword(
            Keyword.TRAMPLE,
            GroupFilter(GameObjectFilter.Artifact.youControl())
        )
    }
    staticAbility {
        ability = GrantKeyword(
            Keyword.HASTE,
            GroupFilter(GameObjectFilter.Artifact.youControl())
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "9"
        artist = "PINDURSKI"
        imageUri = "https://cards.scryfall.io/normal/front/9/7/972f3582-ad7c-4fe9-af29-90fa695a623f.jpg?1783906065"
    }
}
