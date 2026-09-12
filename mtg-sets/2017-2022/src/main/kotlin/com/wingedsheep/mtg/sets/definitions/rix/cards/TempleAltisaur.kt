package com.wingedsheep.mtg.sets.definitions.rix.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.CapDamage
import com.wingedsheep.sdk.scripting.EventPattern
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.events.RecipientFilter

/**
 * Temple Altisaur
 * {4}{W}
 * Creature — Dinosaur
 * If a source would deal damage to another Dinosaur you control, prevent all but 1 of that
 * damage.
 *
 * Same [CapDamage] replacement as Divine Presence, `maxAmount = 1` reproducing "prevent all but
 * 1" exactly, scoped via [RecipientFilter.Matching] to Dinosaurs you control excluding this
 * creature itself ([GameObjectFilter.notSourceItself]).
 */
val TempleAltisaur = card("Temple Altisaur") {
    manaCost = "{4}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dinosaur"
    power = 3
    toughness = 4
    oracleText = "If a source would deal damage to another Dinosaur you control, prevent all " +
        "but 1 of that damage."

    replacementEffect(
        CapDamage(
            maxAmount = 1,
            appliesTo = EventPattern.DamageEvent(
                recipient = RecipientFilter.Matching(
                    GameObjectFilter.Creature.youControl().withSubtype("Dinosaur").notSourceItself()
                )
            )
        )
    )

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "28"
        artist = "Daarken"
        imageUri = "https://cards.scryfall.io/normal/front/f/a/fa8f8d61-51d6-479b-a812-6cbacc7ea1fc.jpg?1783935330"
    }
}
