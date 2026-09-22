package com.wingedsheep.mtg.sets.definitions.cmr.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Commander's Sphere reprint in CMR. Canonical [com.wingedsheep.sdk.model.CardDefinition]
 * lives in C14's `cards/` package; this file contributes only presentation data.
 */
val CommandersSphereReprint = Printing(
    oracleId = "0b67c4e2-f88b-4e01-85a1-9d5f5b8db13b",
    name = "Commander's Sphere",
    setCode = "CMR",
    collectorNumber = "306",
    scryfallId = "a01c16a5-bc50-406f-9ab1-e8346acffbca",
    artist = "Ryan Alexander Lee",
    imageUri = "https://cards.scryfall.io/normal/front/a/0/a01c16a5-bc50-406f-9ab1-e8346acffbca.jpg?1783928762",
    releaseDate = "2020-11-20",
    rarity = Rarity.COMMON,
)
