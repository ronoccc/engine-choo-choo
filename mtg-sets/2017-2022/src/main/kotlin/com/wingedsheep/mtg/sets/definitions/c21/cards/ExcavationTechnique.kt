package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Excavation Technique
 * {3}{W}
 * Sorcery
 * Demonstrate (When you cast this spell, you may copy it. If you do, choose an opponent to also
 * copy it. Players may choose new targets for their copies.)
 * Destroy target nonland permanent. Its controller creates two Treasure tokens.
 */
val ExcavationTechnique = card("Excavation Technique") {
    manaCost = "{3}{W}"
    colorIdentity = "W"
    typeLine = "Sorcery"
    keywords(Keyword.DEMONSTRATE)
    oracleText = "Demonstrate (When you cast this spell, you may copy it. If you do, choose an " +
        "opponent to also copy it. Players may choose new targets for their copies.)\n" +
        "Destroy target nonland permanent. Its controller creates two Treasure tokens."

    spell {
        val t = target("target nonland permanent", Targets.NonlandPermanent)
        // Create the Treasures while the permanent is still on the battlefield so
        // TargetController can read its controller — same order as An Offer You Can't Refuse.
        effect = Effects.CreateTreasure(2, controller = EffectTarget.TargetController)
            .then(Effects.Destroy(t))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "16"
        artist = "Francisco Miyara"
        imageUri = "https://cards.scryfall.io/normal/front/3/2/32ffc8eb-9518-455c-ada5-8b7596896dcf.jpg?1783927611"
    }
}
