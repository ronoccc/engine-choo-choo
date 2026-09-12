package com.wingedsheep.mtg.sets.definitions.ncc.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Naya Panorama reprint in New Capenna Commander. The canonical
 * [com.wingedsheep.sdk.model.CardDefinition] lives in Shards of Alara's `cards/` package; this
 * file contributes only the New Capenna Commander presentation row.
 */
val NayaPanoramaReprint = Printing(
    oracleId = "71e28800-c42c-48c0-95e5-0296be54a4e8",
    name = "Naya Panorama",
    setCode = "NCC",
    collectorNumber = "417",
    scryfallId = "6a546a28-8df5-4080-9f50-af86d84a066f",
    artist = "Hideaki Takamura",
    imageUri = "https://cards.scryfall.io/normal/front/6/a/6a546a28-8df5-4080-9f50-af86d84a066f.jpg?1783923191",
    releaseDate = "2022-04-29",
    rarity = Rarity.COMMON,
)
