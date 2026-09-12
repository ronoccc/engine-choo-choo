package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * Burning Sun's Avatar
 * {3}{R}{R}{R}
 * Creature — Dinosaur Avatar
 * When this creature enters, it deals 3 damage to target opponent or planeswalker and 3 damage
 * to up to one target creature.
 *
 * Two independent targets on the same ETB trigger: [Targets.OpponentOrPlaneswalker] (mandatory)
 * and an optional [TargetCreature] (the Oko, Lorwyn Liege "up to one target creature" shape).
 */
val BurningSunsAvatar = card("Burning Sun's Avatar") {
    manaCost = "{3}{R}{R}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Dinosaur Avatar"
    power = 6
    toughness = 6
    oracleText = "When this creature enters, it deals 3 damage to target opponent or " +
        "planeswalker and 3 damage to up to one target creature."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val opponentOrPlaneswalker = target("target opponent or planeswalker", Targets.OpponentOrPlaneswalker)
        val creature = target("target creature", TargetCreature(optional = true))
        effect = Effects.DealDamage(3, opponentOrPlaneswalker)
            .then(Effects.DealDamage(3, creature))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "135"
        artist = "Randy Vargas"
        imageUri = "https://cards.scryfall.io/normal/front/1/4/146c0cab-5f6b-425d-9f50-33d8da266235.jpg?1783935749"
    }
}
