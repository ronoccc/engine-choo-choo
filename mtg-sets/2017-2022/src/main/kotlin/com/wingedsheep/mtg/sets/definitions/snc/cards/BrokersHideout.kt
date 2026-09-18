package com.wingedsheep.mtg.sets.definitions.snc.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.IfYouDoEffect
import com.wingedsheep.sdk.scripting.effects.SearchDestination
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Brokers Hideout
 * Land
 * When this land enters, sacrifice it. When you do, search your library for a basic Forest,
 * Plains, or Island card, put it onto the battlefield tapped, then shuffle and you gain 1 life.
 */
val BrokersHideout = card("Brokers Hideout") {
    manaCost = ""
    colorIdentity = ""
    typeLine = "Land"
    oracleText = "When this land enters, sacrifice it. When you do, search your library for a " +
        "basic Forest, Plains, or Island card, put it onto the battlefield tapped, then shuffle " +
        "and you gain 1 life."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        effect = IfYouDoEffect(
            action = Effects.SacrificeTarget(EffectTarget.Self),
            ifYouDo = Patterns.Library.searchLibrary(
                filter = GameObjectFilter.BasicLand.withSubtype("Forest") or
                    GameObjectFilter.BasicLand.withSubtype("Plains") or
                    GameObjectFilter.BasicLand.withSubtype("Island"),
                destination = SearchDestination.BATTLEFIELD,
                entersTapped = true,
                shuffleAfter = true,
            ) then Effects.GainLife(1)
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "248"
        artist = "James Paick"
        flavorText = "Once the witness was inside, the safe house vanished first from sight, " +
            "then from memory."
        imageUri = "https://cards.scryfall.io/normal/front/9/8/989b299b-daa9-4bda-94e2-9a2f0e8f2bce.jpg?1783923058"
    }
}
