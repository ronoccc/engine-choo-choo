package com.wingedsheep.mtg.sets.definitions.lcc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.CantAttackUnless
import com.wingedsheep.sdk.scripting.CantBlockUnless
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.ShuffleLibraryEffect
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Topiary Stomper
 * {1}{G}{G}
 * Creature — Plant Dinosaur
 * Vigilance
 * When this creature enters, search your library for a basic land card, put it onto the
 * battlefield tapped, then shuffle.
 * This creature can't attack or block unless you control seven or more lands.
 *
 * The ETB search is unconditional (no "may"), unlike Path to Exile's/Ranging Raptors' — straight
 * gather → choose exactly 1 → move → shuffle pipeline.
 */
val TopiaryStomper = card("Topiary Stomper") {
    manaCost = "{1}{G}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Plant Dinosaur"
    power = 4
    toughness = 4
    oracleText = "Vigilance\n" +
        "When this creature enters, search your library for a basic land card, put it onto the " +
        "battlefield tapped, then shuffle.\n" +
        "This creature can't attack or block unless you control seven or more lands."

    keywords(Keyword.VIGILANCE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        effect = Effects.Pipeline {
            val library = gather(
                CardSource.FromZone(Zone.LIBRARY, Player.You, GameObjectFilter.BasicLand),
                name = "searchable"
            )
            val found = chooseExactly(
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
    }

    staticAbility {
        ability = CantAttackUnless(Conditions.ControlLandsAtLeast(7))
    }
    staticAbility {
        ability = CantBlockUnless(Conditions.ControlLandsAtLeast(7))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "261"
        artist = "Robin Olausson"
        imageUri = "https://cards.scryfall.io/normal/front/e/8/e8b92271-4412-4165-a4a6-e8653e755107.jpg?1783913853"
    }
}
