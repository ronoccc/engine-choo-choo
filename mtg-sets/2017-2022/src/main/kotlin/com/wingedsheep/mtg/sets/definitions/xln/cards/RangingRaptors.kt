package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.MayEffect
import com.wingedsheep.sdk.scripting.effects.ShuffleLibraryEffect
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Ranging Raptors
 * {2}{G}
 * Creature — Dinosaur
 * Enrage — Whenever this creature is dealt damage, you may search your library for a basic land
 * card, put it onto the battlefield tapped, then shuffle.
 *
 * Same [MayEffect] + library-search-and-shuffle [Effects.Pipeline] shape as Path to Exile's ramp
 * clause, just triggered off [Triggers.TakesDamage] (the enrage idiom from Ripjaw Raptor) instead
 * of an exile-and-target effect, and decided by the default controller rather than a target's
 * controller.
 */
val RangingRaptors = card("Ranging Raptors") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Dinosaur"
    power = 2
    toughness = 3
    oracleText = "Enrage — Whenever this creature is dealt damage, you may search your library " +
        "for a basic land card, put it onto the battlefield tapped, then shuffle."

    triggeredAbility {
        trigger = Triggers.TakesDamage
        description = "Enrage — Whenever this creature is dealt damage, you may search your " +
            "library for a basic land card, put it onto the battlefield tapped, then shuffle."
        effect = MayEffect(
            Effects.Pipeline {
                val library = gather(
                    CardSource.FromZone(Zone.LIBRARY, Player.You, GameObjectFilter.BasicLand),
                    name = "searchable"
                )
                val found = chooseUpTo(
                    1,
                    from = library,
                    chooser = Chooser.Controller,
                    prompt = "Search your library for a basic land card",
                    name = "found"
                )
                move(
                    found,
                    CardDestination.ToZone(Zone.BATTLEFIELD, Player.You, ZonePlacement.Tapped)
                )
                run(ShuffleLibraryEffect(EffectTarget.Controller))
            }
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "201"
        artist = "Simon Dominic"
        imageUri = "https://cards.scryfall.io/normal/front/9/e/9e91efc6-0e6a-4a9e-a486-adf53e53d3f1.jpg?1783935720"
    }
}
