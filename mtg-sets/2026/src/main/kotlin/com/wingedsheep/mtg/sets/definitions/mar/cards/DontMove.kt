package com.wingedsheep.mtg.sets.definitions.mar.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.effects.CreateDelayedTriggerEffect
import com.wingedsheep.sdk.scripting.effects.DelayedTriggerExpiry
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Don't Move
 * {3}{W}{W}
 * Sorcery
 * Destroy all tapped creatures. Until your next turn, whenever a creature becomes tapped,
 * destroy it.
 *
 * [Effects.DestroyAll] for the sweep, then a global [CreateDelayedTriggerEffect] (the Tamiyo,
 * Field Researcher shape, minus a `watchedTarget` since this watches every creature, not one
 * bound entity) keyed on [Triggers.becomesTapped] with ANY binding and
 * [DelayedTriggerExpiry.UntilControllersNextTurn].
 */
val DontMove = card("Don't Move") {
    manaCost = "{3}{W}{W}"
    colorIdentity = "W"
    typeLine = "Sorcery"
    oracleText = "Destroy all tapped creatures. Until your next turn, whenever a creature " +
        "becomes tapped, destroy it."

    spell {
        effect = Effects.DestroyAll(GameObjectFilter.Creature.tapped())
            .then(
                CreateDelayedTriggerEffect(
                    effect = Effects.Destroy(EffectTarget.TriggeringEntity),
                    trigger = Triggers.becomesTapped(
                        binding = TriggerBinding.ANY,
                        filter = GameObjectFilter.Creature
                    ),
                    expiry = DelayedTriggerExpiry.UntilControllersNextTurn
                )
            )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "43"
        artist = "John Byrne"
        imageUri = "https://cards.scryfall.io/normal/front/c/5/c55e5f0d-8a4b-4b6f-ab43-1268b6fac74e.jpg?1783903322"
    }
}
