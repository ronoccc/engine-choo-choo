package com.wingedsheep.mtg.sets.definitions.cmr.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.encore
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.SetBasePowerToughnessDynamicStatic
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Soul of Eternity
 * {5}{W}{W}
 * Creature — Avatar
 * Soul of Eternity's power and toughness are each equal to your life total.
 * Encore {7}{W}{W} (see [encoreAbility][com.wingedsheep.sdk.dsl.encoreAbility]).
 */
val SoulOfEternity = card("Soul of Eternity") {
    manaCost = "{5}{W}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Avatar"
    // Printed P/T is */* — placeholder 0/0 base is overwritten in Layer 7b by the CDA below.
    power = 0
    toughness = 0
    oracleText = "Soul of Eternity's power and toughness are each equal to your life total.\n" +
        "Encore {7}{W}{W} ({7}{W}{W}, Exile this card from your graveyard: For each opponent, " +
        "create a token copy that attacks that opponent this turn if able. They gain haste. " +
        "Sacrifice them at the beginning of the next end step. Activate only as a sorcery.)"

    // CDA (CR 613.3d): functions in every zone, not just the battlefield.
    staticAbility {
        ability = SetBasePowerToughnessDynamicStatic(
            power = DynamicAmount.YourLifeTotal,
            toughness = DynamicAmount.YourLifeTotal,
            filter = GroupFilter.source()
        )
    }

    encore("{7}{W}{W}")

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "50"
        artist = "Yigit Koroglu"
        imageUri = "https://cards.scryfall.io/normal/front/0/8/08241f94-8b5e-4f9b-8120-8175e3256e35.jpg?1783928870"
    }
}
