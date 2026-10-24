package com.wingedsheep.mtg.sets.definitions.lci.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty

/**
 * Pantlaza, Sun-Favored
 * {2}{R}{G}{W}
 * Legendary Creature — Dinosaur
 * 4/4
 * Whenever Pantlaza, Sun-Favored or another Dinosaur you control enters, you may discover X,
 * where X is that creature's toughness. Do this only once each turn.
 *
 * "Do this only once each turn" is [effectOncePerTurn]: the ability can trigger again, but
 * discovering spends the action for the turn, and declining the "you may" does not.
 */
val PantlazaSunFavored = card("Pantlaza, Sun-Favored") {
    manaCost = "{2}{R}{G}{W}"
    colorIdentity = "RGW"
    typeLine = "Legendary Creature — Dinosaur"
    power = 4
    toughness = 4
    oracleText = "Whenever Pantlaza or another Dinosaur you control enters, you may discover X, " +
        "where X is that creature's toughness. Do this only once each turn."

    triggeredAbility {
        trigger = Triggers.a(GameObjectFilter.Creature.youControl().withSubtype("Dinosaur")).enters()
        optional = true
        effectOncePerTurn = true
        effect = Effects.Discover(
            DynamicAmount.EntityProperty(EffectTarget.TriggeringEntity, EntityNumericProperty.Toughness)
        )
        description = "You may discover X, where X is that creature's toughness. " +
            "Do this only once each turn."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "232"
        artist = "Victor Adame Minguez"
    }
}
