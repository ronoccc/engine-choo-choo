package com.wingedsheep.mtg.sets.definitions.pip.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Steel Overseer reprint in PIP (collector #1015, extended-art surge foil). Canonical
 * [com.wingedsheep.sdk.model.CardDefinition] lives in M11's `cards/` package; this file
 * contributes only presentation data.
 */
val SteelOverseerReprint1015 = Printing(
    oracleId = "986ae327-f433-4c58-93dc-afc544b9bfcb",
    name = "Steel Overseer",
    setCode = "PIP",
    collectorNumber = "1015",
    scryfallId = "1752cc62-abf9-406b-a5ac-f4c8b12a92ef",
    artist = "Daarken",
    imageUri = "https://cards.scryfall.io/normal/front/1/7/1752cc62-abf9-406b-a5ac-f4c8b12a92ef.jpg?1783912066",
    releaseDate = "2024-03-08",
    rarity = Rarity.RARE,
    frameEffects = listOf("extendedart"),
)
