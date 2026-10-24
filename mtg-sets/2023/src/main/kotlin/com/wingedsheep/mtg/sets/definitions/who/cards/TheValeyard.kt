package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * The Valeyard {2}{U}{B}{R}
 * Legendary Creature — Time Lord Noble
 * 4/5
 *
 * "If an opponent would face a villainous choice, they face that choice an additional time.
 *  (They can make the same or different choices.)
 *  While voting, you may vote an additional time."
 *
 * TODO: Ability 1 ("If an opponent would face a villainous choice, they face that choice an
 * additional time") is not implemented — no [VillainousChoiceExtraForOpponents] static ability
 * exists in the current mtg-sdk.
 *
 * TODO: Ability 2 ("While voting, you may vote an additional time") is not implemented — voting
 * has no engine representation in this corpus yet.
 */
val TheValeyard = card("The Valeyard") {
    manaCost = "{2}{U}{B}{R}"
    colorIdentity = "UBR"
    typeLine = "Legendary Creature — Time Lord Noble"
    power = 4
    toughness = 5
    oracleText = "If an opponent would face a villainous choice, they face that choice an " +
        "additional time. (They can make the same or different choices.)\n" +
        "While voting, you may vote an additional time."

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "165"
        artist = "Tyukina Tatiana"
        flavorText = "\"The most damning is still to come. And when I have finished, this court " +
            "will demand your life!\""
        imageUri = "https://cards.scryfall.io/normal/front/d/6/d63218a4-afaf-4ad8-9ca4-4f9af87877b9.jpg?1783914619"
    }
}
