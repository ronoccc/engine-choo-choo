package com.wingedsheep.mtg.sets.definitions.msc.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Overwhelming Stampede reprint in Marvel Super Heroes Commander (extended art, #385). Canonical
 * [com.wingedsheep.sdk.model.CardDefinition] lives in Magic 2011's `cards/` package; this file
 * contributes only presentation data.
 */
val OverwhelmingStampedeExtendedArtReprint = Printing(
    oracleId = "e1e96802-fd0c-41f1-aa21-3287d75a0e88",
    name = "Overwhelming Stampede",
    setCode = "MSC",
    collectorNumber = "385",
    scryfallId = "938fb368-6b54-4884-b52a-0be3ccf2c9fb",
    artist = "Daniel Landerman",
    imageUri = "https://cards.scryfall.io/normal/front/9/3/938fb368-6b54-4884-b52a-0be3ccf2c9fb.jpg?1783903151",
    releaseDate = "2026-06-26",
    rarity = Rarity.RARE,
    frameEffects = listOf("extendedart"),
)
