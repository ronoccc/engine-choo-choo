package com.wingedsheep.mtg.sets.definitions.aer.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.GrantNextSpellFreeCastEffect
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Rishkar's Expertise
 * {4}{G}{G}
 * Sorcery
 * Draw cards equal to the greatest power among creatures you control.
 * You may cast a spell with mana value 5 or less from your hand without paying its mana cost.
 *
 * The draw is [DynamicAmounts.battlefield]'s `maxPower()` (Prime Speaker Zegana's own aggregate
 * shape). The free cast is [GrantNextSpellFreeCastEffect] scoped to a mana-value-5-or-less
 * filter — "the next matching spell you cast" is exactly the "you may cast ... without paying"
 * permission window an Expertise card opens for the rest of the turn.
 */
val RishkarsExpertise = card("Rishkar's Expertise") {
    manaCost = "{4}{G}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    oracleText = "Draw cards equal to the greatest power among creatures you control.\n" +
        "You may cast a spell with mana value 5 or less from your hand without paying its mana cost."

    spell {
        effect = Effects.DrawCards(DynamicAmounts.battlefield(Player.You, GameObjectFilter.Creature).maxPower())
            .then(GrantNextSpellFreeCastEffect(GameObjectFilter.Any.manaValueAtMost(5)))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "123"
        artist = "Magali Villeneuve"
        imageUri = "https://cards.scryfall.io/normal/front/5/d/5d58ba68-05c1-4cd8-a93d-321cd1739ccb.jpg?1783936738"
    }
}
