package com.wingedsheep.mtg.sets.definitions.mh3.cards

import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.ConditionalEffect
import com.wingedsheep.sdk.scripting.effects.CreateDelayedTriggerEffect
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetPermanent

/**
 * Phelia, Exuberant Shepherd — Modern Horizons 3 #40.
 *
 * The attack trigger exiles up to one *other* target nonland permanent (any player's —
 * [TargetFilter.OtherNonlandPermanent], `optional = true` for "up to one") and schedules a
 * [CreateDelayedTriggerEffect] for the next end step, the same blink shape
 * [com.wingedsheep.sdk.dsl.Patterns.Exile.exileUntilEndStep] uses elsewhere. It's written out here
 * rather than reused because the return needs a second, conditional effect chained after it:
 * the returned card enters under its owner's control by default (no `controllerOverride`), and
 * once it's back, [Conditions.TargetMatchesFilter] re-checks that same target reference against
 * `GameObjectFilter.Any.ownedByYou()` — since ownership never changes across the exile/return, this
 * reads identically to the printed "if it entered under your control" — gating the +1/+1 counter on
 * Phelia.
 */
val PheliaExuberantShepherd = card("Phelia, Exuberant Shepherd") {
    manaCost = "{1}{W}"
    colorIdentity = "W"
    typeLine = "Legendary Creature — Dog"
    oracleText = "Flash\n" +
        "Whenever Phelia attacks, exile up to one other target nonland permanent. At the beginning " +
        "of the next end step, return that card to the battlefield under its owner's control. If it " +
        "entered under your control, put a +1/+1 counter on Phelia."
    power = 2
    toughness = 2

    keywords(Keyword.FLASH)

    triggeredAbility {
        trigger = Triggers.Attacks
        val t = target(
            "up to one other target nonland permanent",
            TargetPermanent(optional = true, filter = TargetFilter.OtherNonlandPermanent)
        )
        effect = Effects.Composite(
            Effects.Move(t, Zone.EXILE),
            CreateDelayedTriggerEffect(
                step = Step.END,
                effect = Effects.Composite(
                    Effects.Move(t, Zone.BATTLEFIELD),
                    ConditionalEffect(
                        condition = Conditions.TargetMatchesFilter(GameObjectFilter.Any.ownedByYou(), targetIndex = 0),
                        effect = Effects.AddCounters(Counters.PLUS_ONE_PLUS_ONE, 1, EffectTarget.Self)
                    )
                )
            )
        )
        description = "Whenever Phelia attacks, exile up to one other target nonland permanent. At " +
            "the beginning of the next end step, return that card to the battlefield under its " +
            "owner's control. If it entered under your control, put a +1/+1 counter on Phelia."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "40"
        artist = "Rudy Siswanto"
        imageUri = "https://cards.scryfall.io/normal/front/5/5/55707746-da6e-46e5-a5ca-7ac843fdc38e.jpg?1783911298"
    }
}
