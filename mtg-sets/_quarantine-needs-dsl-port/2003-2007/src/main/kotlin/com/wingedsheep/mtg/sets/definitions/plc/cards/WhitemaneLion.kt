package com.wingedsheep.mtg.sets.definitions.plc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Whitemane Lion — Planar Chaos #22.
 *
 * The Man-o'-War shape (`mtg-sets/1993-1999/.../vis/cards/ManOWar.kt`), with the target filter
 * narrowed from "target creature" to "a creature you control" ([Targets.CreatureYouControl]),
 * which is *not* "up to one" — the bounce is mandatory and, with no other creature in play, the
 * Lion returns itself.
 */
val WhitemaneLion = card("Whitemane Lion") {
    manaCost = "{1}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Cat"
    oracleText = "Flash\n" +
        "When this creature enters, return a creature you control to its owner's hand."
    power = 2
    toughness = 2

    keywords(Keyword.FLASH)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val t = target("a creature you control", Targets.CreatureYouControl)
        effect = Effects.Move(t, Zone.HAND)
        description = "When this creature enters, return a creature you control to its owner's hand."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "22"
        artist = "Zoltan Boros & Gabor Szikszai"
        flavorText = "Saltfield nomads call a sudden storm a \"whitemane.\""
        imageUri = "https://cards.scryfall.io/normal/front/c/b/cb1214ff-e959-43ca-8415-79a98d398490.jpg?1783943166"
    }
}
