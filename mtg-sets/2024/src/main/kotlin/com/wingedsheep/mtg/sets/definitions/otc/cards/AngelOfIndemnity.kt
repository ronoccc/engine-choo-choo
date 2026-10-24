package com.wingedsheep.mtg.sets.definitions.otc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Angel of Indemnity
 * {5}{W}
 * Creature — Angel Warrior
 * Flying, lifelink
 * When this creature enters, return target permanent card with mana value 4 or less from your
 * graveyard to the battlefield.
 *
 * TODO: **Encore {6}{W}{W} is not implemented** — the `encore` DSL helper does not exist in the
 * current mtg-sdk. Oracle text is preserved below.
 */
val AngelOfIndemnity = card("Angel of Indemnity") {
    manaCost = "{5}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Angel Warrior"
    keywords(Keyword.FLYING, Keyword.LIFELINK)
    power = 5
    toughness = 5
    oracleText = "Flying, lifelink\n" +
        "When this creature enters, return target permanent card with mana value 4 or less from " +
        "your graveyard to the battlefield.\n" +
        "Encore {6}{W}{W} ({6}{W}{W}, Exile this card from your graveyard: For each opponent, " +
        "create a token copy that attacks that opponent this turn if able. They gain haste. " +
        "Sacrifice them at the beginning of the next end step. Activate only as a sorcery.)"

    triggeredAbility {
        trigger = Triggers.self.enters()
        val card = target(
            TargetFilter(
                GameObjectFilter.Permanent.ownedByYou().manaValueAtMost(4),
                zone = Zone.GRAVEYARD
            )
        )
        effect = Effects.PutOntoBattlefieldFromGraveyard(card)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "45"
        artist = "Denman Rooke"
        imageUri = "https://cards.scryfall.io/normal/front/8/1/816ee5e5-c833-437b-b031-1abd72269149.jpg?1783911959"
    }
}
