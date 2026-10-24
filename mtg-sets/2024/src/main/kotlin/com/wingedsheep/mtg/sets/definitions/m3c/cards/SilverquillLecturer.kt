package com.wingedsheep.mtg.sets.definitions.m3c.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Silverquill Lecturer
 * {4}{W}
 * Creature — Kor Wizard
 * Creature spells you cast have demonstrate. (Whenever you cast a creature spell, you may copy
 * it. If you do, choose an opponent to also copy it. Each copy becomes a token.)
 *
 * TODO: **Demonstrate is not implemented** — [com.wingedsheep.sdk.core.Keyword.DEMONSTRATE] does
 * not exist in the current mtg-sdk, so the static grant is omitted. Oracle text is preserved below.
 */
val SilverquillLecturer = card("Silverquill Lecturer") {
    manaCost = "{4}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Kor Wizard"
    oracleText = "Creature spells you cast have demonstrate. (Whenever you cast a creature spell, " +
        "you may copy it. If you do, choose an opponent to also copy it. Each copy becomes a token.)"
    power = 3
    toughness = 3

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "96"
        artist = "Ryan Pancoast"
        flavorText = "\"Did you bring enough to share with the rest of the class?\""
        imageUri = "https://cards.scryfall.io/normal/front/5/0/507e34d0-cdc5-4226-9664-b725b702044d.jpg?1783911411"
    }
}
