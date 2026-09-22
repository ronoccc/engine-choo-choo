package com.wingedsheep.mtg.sets.definitions.msc.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Bedevil reprints in Marvel Super Heroes Commander (two collector numbers: the surge-foil
 * showcase and the extended-art version). The canonical [com.wingedsheep.sdk.model.CardDefinition]
 * lives in Ravnica Allegiance's `cards/` package; these files contribute only presentation data.
 */
val BedevilReprint = Printing(
    oracleId = "bceecc64-96f1-4e7b-8904-0aef90377764",
    name = "Bedevil",
    setCode = "MSC",
    collectorNumber = "182",
    scryfallId = "985b9779-b8f6-4d7e-9490-5d2b91ee8db2",
    artist = "Björn Barends",
    imageUri = "https://cards.scryfall.io/normal/front/9/8/985b9779-b8f6-4d7e-9490-5d2b91ee8db2.jpg?1783903227",
    releaseDate = "2026-06-26",
    rarity = Rarity.RARE,
)

val BedevilExtendedArtReprint = Printing(
    oracleId = "bceecc64-96f1-4e7b-8904-0aef90377764",
    name = "Bedevil",
    setCode = "MSC",
    collectorNumber = "393",
    scryfallId = "38498fb1-b3ca-4dd4-8605-92a859b875ba",
    artist = "Björn Barends",
    imageUri = "https://cards.scryfall.io/normal/front/3/8/38498fb1-b3ca-4dd4-8605-92a859b875ba.jpg?1783903150",
    releaseDate = "2026-06-26",
    rarity = Rarity.RARE,
)
