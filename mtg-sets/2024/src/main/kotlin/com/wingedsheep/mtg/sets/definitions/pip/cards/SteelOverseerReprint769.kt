package com.wingedsheep.mtg.sets.definitions.pip.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Steel Overseer reprint in PIP (collector #769, surge foil). Canonical
 * [com.wingedsheep.sdk.model.CardDefinition] lives in M11's `cards/` package; this file
 * contributes only presentation data.
 */
val SteelOverseerReprint769 = Printing(
    oracleId = "986ae327-f433-4c58-93dc-afc544b9bfcb",
    name = "Steel Overseer",
    setCode = "PIP",
    collectorNumber = "769",
    scryfallId = "32f194b1-463b-4e82-961a-61be31fae8ab",
    artist = "Daarken",
    imageUri = "https://cards.scryfall.io/normal/front/3/2/32f194b1-463b-4e82-961a-61be31fae8ab.jpg?1783912147",
    releaseDate = "2024-03-08",
    rarity = Rarity.RARE,
)
