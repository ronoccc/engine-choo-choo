package com.wingedsheep.mtg.sets.definitions.conflux.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.MayEffect
import com.wingedsheep.sdk.scripting.effects.ShuffleLibraryEffect
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Path to Exile
 * {W}
 * Instant
 * Exile target creature. Its controller may search their library for a basic land card, put
 * that card onto the battlefield tapped, then shuffle.
 *
 * Modeled on Magmatic Hellkite's "destroy target X. Its controller searches..." shape:
 * [Player.ControllerOf] scopes the library gather/battlefield destination to the exiled
 * creature's controller, [Chooser.ControllerOfTarget] lets that player (not the caster) make
 * the pick, and [EffectTarget.TargetController] both answers the may-prompt and shuffles —
 * no string key needed since there's only one declared target. Unlike Magmatic Hellkite, the
 * printed text says "may", so the search is wrapped in [MayEffect] (Settle the Wreckage's
 * pattern) rather than relying on ChooseUpTo(1) alone to represent the option to decline.
 */
val PathToExile = card("Path to Exile") {
    manaCost = "{W}"
    colorIdentity = "W"
    typeLine = "Instant"
    oracleText = "Exile target creature. Its controller may search their library for a basic " +
        "land card, put that card onto the battlefield tapped, then shuffle."

    spell {
        val creature = target(
            "target creature",
            TargetObject(filter = TargetFilter.Creature)
        )
        val controller = Player.ControllerOf("target creature")

        val mayRamp = MayEffect(
            Effects.Pipeline {
                val library = gather(
                    CardSource.FromZone(Zone.LIBRARY, controller, GameObjectFilter.BasicLand),
                    name = "searchable"
                )
                val found = chooseUpTo(
                    1,
                    from = library,
                    chooser = Chooser.ControllerOfTarget,
                    prompt = "Search your library for a basic land card",
                    name = "found"
                )
                move(
                    found,
                    CardDestination.ToZone(Zone.BATTLEFIELD, controller, ZonePlacement.Tapped)
                )
                run(ShuffleLibraryEffect(EffectTarget.TargetController))
            },
            decisionMaker = EffectTarget.TargetController,
            descriptionOverride = "Its controller may search their library for a basic land " +
                "card, put that card onto the battlefield tapped, then shuffle."
        )

        effect = Effects.Exile(creature).then(mayRamp)
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "15"
        artist = "Todd Lockwood"
        imageUri = "https://cards.scryfall.io/normal/front/2/9/29b7a8b1-b98e-483a-87a4-73bd831c03d4.jpg?1783942491"
    }
}
