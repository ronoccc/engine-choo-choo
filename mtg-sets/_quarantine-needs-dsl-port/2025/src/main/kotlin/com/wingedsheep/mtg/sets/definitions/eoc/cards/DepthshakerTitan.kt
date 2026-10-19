package com.wingedsheep.mtg.sets.definitions.eoc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.CreateDelayedTriggerEffect
import com.wingedsheep.sdk.scripting.effects.DelayedTriggerTiming
import com.wingedsheep.sdk.scripting.effects.ForEachTargetEffect
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.effects.MoveType
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject
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
 * The ETB is an unbounded target set (`TargetObject(unlimited = true, ...)`) animated per-target
 * via `ForEachTargetEffect`, then captured into a pipeline collection (`GatherCardsEffect(
 * CardSource.ChosenTargets, ...)`) so a `CreateDelayedTriggerEffect` scheduled for the next END
 * step can sacrifice exactly that set.
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
        trigger = Triggers.EntersBattlefield
        target(
            "any number of target noncreature artifacts you control",
            TargetObject(
                unlimited = true,
                minCount = 0,
                filter = TargetFilter(GameObjectFilter.Artifact.notCreature().youControl())
            )
        )
        effect = Effects.Composite(
            ForEachTargetEffect(
                listOf(
                    Effects.BecomeCreature(
                        target = EffectTarget.ContextTarget(0),
                        power = 3,
                        toughness = 3
                    )
                )
            ),
            GatherCardsEffect(source = CardSource.ChosenTargets, storeAs = "animatedArtifacts"),
            CreateDelayedTriggerEffect(
                step = Step.END,
                timing = DelayedTriggerTiming.NEXT_END_STEP,
                effect = MoveCollectionEffect(
                    from = "animatedArtifacts",
                    destination = CardDestination.ToZone(Zone.GRAVEYARD, Player.You),
                    moveType = MoveType.Sacrifice
                )
            )
        )
        description = "When this creature enters, any number of target noncreature artifacts you " +
            "control become 3/3 artifact creatures. Sacrifice them at the beginning of the next " +
            "end step."
    }

    staticAbility {
        ability = GrantKeyword(
            Keyword.MELEE,
            GroupFilter(GameObjectFilter.Artifact.youControl())
        )
    }
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
