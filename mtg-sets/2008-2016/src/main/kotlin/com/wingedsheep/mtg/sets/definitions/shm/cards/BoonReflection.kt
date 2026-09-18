package com.wingedsheep.mtg.sets.definitions.shm.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EventPattern
import com.wingedsheep.sdk.scripting.ModifyLifeGain
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Boon Reflection
 * {4}{W}
 * Enchantment
 * If you would gain life, you gain twice that much life instead.
 */
val BoonReflection = card("Boon Reflection") {
    manaCost = "{4}{W}"
    colorIdentity = "W"
    typeLine = "Enchantment"
    oracleText = "If you would gain life, you gain twice that much life instead."

    replacementEffect(
        ModifyLifeGain(
            multiplier = 2,
            appliesTo = EventPattern.LifeGainEvent(player = Player.You)
        )
    )

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "5"
        artist = "Terese Nielsen & Ron Spencer"
        flavorText = "Kithkin healers chant the clan songs of both their parents over the broth to " +
            "double its curative effect."
        imageUri = "https://cards.scryfall.io/normal/front/1/a/1a27ba4d-53af-427d-9e51-be04db077fac.jpg?1783942769"
    }
}
