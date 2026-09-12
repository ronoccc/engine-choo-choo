package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EventPattern
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.PermanentsEnterTapped

/**
 * Kinjalli's Sunwing
 * {2}{W}
 * Creature — Dinosaur
 * Flying
 * Creatures your opponents control enter tapped.
 *
 * Same [PermanentsEnterTapped] runtime replacement as Thalia, Heretic Cathar's "creatures your
 * opponents control enter tapped" clause, minus her nonbasic-land half.
 */
val KinjallisSunwing = card("Kinjalli's Sunwing") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dinosaur"
    power = 2
    toughness = 3
    oracleText = "Flying\n" +
        "Creatures your opponents control enter tapped."

    keywords(Keyword.FLYING)

    replacementEffect(
        PermanentsEnterTapped(
            appliesTo = EventPattern.ZoneChangeEvent(
                filter = GameObjectFilter.Creature.opponentControls(),
                to = Zone.BATTLEFIELD,
            )
        )
    )

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "19"
        artist = "Simon Dominic"
        imageUri = "https://cards.scryfall.io/normal/front/2/b/2b9e0b0f-651a-44e6-8fb0-e46bfda0ada9.jpg?1783935798"
    }
}
