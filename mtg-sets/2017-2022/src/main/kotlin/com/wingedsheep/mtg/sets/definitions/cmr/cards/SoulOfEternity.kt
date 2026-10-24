// Missing SDK primitive: Encore (graveyard exile activated ability creating per-opponent attacking copies).
package com.wingedsheep.mtg.sets.definitions.cmr.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.SetBasePowerToughnessDynamicStatic
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

val SoulOfEternity = card("Soul of Eternity") {
    manaCost = "{5}{W}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Avatar"
    power = 0
    toughness = 0
    oracleText = "Soul of Eternity's power and toughness are each equal to your life total.\n" +
        "Encore {7}{W}{W} ({7}{W}{W}, Exile this card from your graveyard: For each opponent, " +
        "create a token copy that attacks that opponent this turn if able. They gain haste. " +
        "Sacrifice them at the beginning of the next end step. Activate only as a sorcery.)"

    staticAbility {
        ability = SetBasePowerToughnessDynamicStatic(
            power = DynamicAmounts.yourLifeTotal(),
            toughness = DynamicAmounts.yourLifeTotal(),
            filter = GroupFilter.source(),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "50"
        artist = "Yigit Koroglu"
        imageUri = "https://cards.scryfall.io/normal/front/0/8/08241f94-8b5e-4f9b-8120-8175e3256e35.jpg?1783928870"
    }
}
