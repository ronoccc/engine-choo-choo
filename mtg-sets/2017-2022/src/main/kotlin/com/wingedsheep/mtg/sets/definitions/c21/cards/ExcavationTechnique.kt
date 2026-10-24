// Missing SDK primitive: Demonstrate (optional spell copy for you and a chosen opponent).
package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val ExcavationTechnique = card("Excavation Technique") {
    manaCost = "{3}{W}"
    colorIdentity = "W"
    typeLine = "Sorcery"
    oracleText = "Demonstrate (When you cast this spell, you may copy it. If you do, choose an " +
        "opponent to also copy it. Players may choose new targets for their copies.)\n" +
        "Destroy target nonland permanent. Its controller creates two Treasure tokens."

    spell {
        val t = target(TargetFilter.NonlandPermanent)
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
