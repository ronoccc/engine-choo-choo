package com.wingedsheep.mtg.sets.definitions.ala.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Invincible Hymn
 * {6}{W}{W}
 * Sorcery
 * Count the number of cards in your library. Your life total becomes that number.
 */
val InvincibleHymn = card("Invincible Hymn") {
    manaCost = "{6}{W}{W}"
    colorIdentity = "W"
    typeLine = "Sorcery"
    oracleText = "Count the number of cards in your library. Your life total becomes that number."

    spell {
        effect = Effects.SetLifeTotal(DynamicAmount.AggregateZone(Player.You, Zone.LIBRARY))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "14"
        artist = "Matt Stewart"
        flavorText = "Bantians believe those who are born into the highest caste and live a life of " +
            "discipline and virtue can ascend to become angels."
        imageUri = "https://cards.scryfall.io/normal/front/7/1/712c64bc-ba0c-4bab-9329-54805f4efa8a.jpg?1783942581"
    }
}
