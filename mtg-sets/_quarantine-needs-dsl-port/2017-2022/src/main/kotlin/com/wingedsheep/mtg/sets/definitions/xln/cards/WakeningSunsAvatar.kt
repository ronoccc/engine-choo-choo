package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter

/**
 * Wakening Sun's Avatar
 * {5}{W}{W}{W}
 * Creature — Dinosaur Avatar
 * When this creature enters, if you cast it from your hand, destroy all non-Dinosaur creatures.
 *
 * `interveningIf = Conditions.WasCastFromHand` (the Phage the Untouchable idiom) gates the
 * trigger itself — a permanent put onto the battlefield some other way (reanimation, a token
 * copy) never puts the ability on the stack at all.
 */
val WakeningSunsAvatar = card("Wakening Sun's Avatar") {
    manaCost = "{5}{W}{W}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dinosaur Avatar"
    power = 7
    toughness = 7
    oracleText = "When this creature enters, if you cast it from your hand, destroy all " +
        "non-Dinosaur creatures."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        interveningIf = Conditions.WasCastFromHand
        effect = Effects.DestroyAll(GameObjectFilter.Creature.notSubtype(Subtype("Dinosaur")))
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "44"
        artist = "Tyler Jacobson"
        imageUri = "https://cards.scryfall.io/normal/front/7/4/7434abe4-87eb-4709-a26d-4e23154b4d31.jpg?1783935787"
    }
}
