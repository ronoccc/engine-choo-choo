package com.wingedsheep.mtg.sets.definitions.pip.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Steel Overseer reprint in PIP (collector #487, extended-art frame). Canonical
 * [com.wingedsheep.sdk.model.CardDefinition] lives in M11's `cards/` package; this file
 * contributes only presentation data.
 */
val SteelOverseerReprint487 = Printing(
    oracleId = "986ae327-f433-4c58-93dc-afc544b9bfcb",
    name = "Steel Overseer",
    setCode = "PIP",
    collectorNumber = "487",
    scryfallId = "60cace8d-3618-4c10-b59e-dbb51fb46ad5",
    artist = "Daarken",
    imageUri = "https://cards.scryfall.io/normal/front/6/0/60cace8d-3618-4c10-b59e-dbb51fb46ad5.jpg?1783912247",
    releaseDate = "2024-03-08",
    rarity = Rarity.RARE,
    frameEffects = listOf("extendedart"),
)
