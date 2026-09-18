package com.wingedsheep.mtg.sets.definitions.snc.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Halo Fountain
 * {2}{W}
 * Artifact
 * {W}, {T}, Untap a tapped creature you control: Create a 1/1 green and white Citizen creature
 * token.
 * {W}{W}, {T}, Untap two tapped creatures you control: Draw a card.
 * {W}{W}{W}{W}{W}, {T}, Untap fifteen tapped creatures you control: You win the game.
 *
 * Three escalating costs built from the same [Costs.UntapPermanents] primitive — the untap-cost
 * twin of [Costs.TapPermanents], added for this card.
 */
val HaloFountain = card("Halo Fountain") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Artifact"
    oracleText = "{W}, {T}, Untap a tapped creature you control: Create a 1/1 green and white " +
        "Citizen creature token.\n{W}{W}, {T}, Untap two tapped creatures you control: Draw a " +
        "card.\n{W}{W}{W}{W}{W}, {T}, Untap fifteen tapped creatures you control: You win the game."

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{W}"), Costs.Tap, Costs.UntapPermanents(count = 1))
        effect = Effects.CreateToken(
            power = 1,
            toughness = 1,
            colors = setOf(Color.GREEN, Color.WHITE),
            creatureTypes = setOf("Citizen"),
            name = "Citizen",
            imageUri = "https://cards.scryfall.io/normal/front/f/a/fa605271-03c0-401b-a470-4301898c2124.jpg?1783909751"
        )
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{W}{W}"), Costs.Tap, Costs.UntapPermanents(count = 2))
        effect = Effects.DrawCards(1)
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{W}{W}{W}{W}{W}"), Costs.Tap, Costs.UntapPermanents(count = 15))
        effect = Effects.WinGame()
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "15"
        artist = "Anastasia Ovchinnikova"
        imageUri = "https://cards.scryfall.io/normal/front/0/e/0ee79399-715c-4c46-9fa1-e76b1087f009.jpg?1783923158"
    }
}
