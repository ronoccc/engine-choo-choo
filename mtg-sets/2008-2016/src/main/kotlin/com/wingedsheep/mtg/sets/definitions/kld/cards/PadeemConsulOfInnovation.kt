package com.wingedsheep.mtg.sets.definitions.kld.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Padeem, Consul of Innovation
 * {3}{U}
 * Legendary Creature — Vedalken Artificer
 * 1/4
 * Artifacts you control have hexproof.
 * At the beginning of your upkeep, if you control the artifact with the greatest mana value or
 * tied for the greatest mana value, draw a card.
 *
 * Static half is Leonin Abunas' shape (`GrantKeyword(HEXPROOF, Artifact.youControl())`). The
 * upkeep half is an intervening-if (CR 603.4) gated on the new general-purpose
 * `StatePredicate.HasGreatestManaValueAmong(candidates)` — the filter-parameterized sibling of
 * `HasGreatestManaValueAmongAllCreatures`, added alongside this card and wired the same way as
 * its existing `HasLeastManaValueAmong` counterpart (PredicateEvaluator + the three exhaustive
 * `when`s in BeginningPhaseManager/TriggerMatcher/AffectsFilterResolver). Ties match every
 * maximum-mana-value artifact, which is exactly "or tied for the greatest mana value" — no
 * "you choose one" step needed since the condition is a yes/no gate, not a selection.
 */
val PadeemConsulOfInnovation = card("Padeem, Consul of Innovation") {
    manaCost = "{3}{U}"
    colorIdentity = "U"
    typeLine = "Legendary Creature — Vedalken Artificer"
    power = 1
    toughness = 4
    oracleText = "Artifacts you control have hexproof. (They can't be the targets of spells or " +
        "abilities your opponents control.)\nAt the beginning of your upkeep, if you control the " +
        "artifact with the greatest mana value or tied for the greatest mana value, draw a card."

    staticAbility {
        ability = GrantKeyword(Keyword.HEXPROOF, GroupFilter(GameObjectFilter.Artifact.youControl()))
    }

    triggeredAbility {
        trigger = Triggers.YourUpkeep
        interveningIf = Conditions.YouControl(
            GameObjectFilter.Artifact.hasGreatestManaValueAmong(GameObjectFilter.Artifact)
        )
        effect = Effects.DrawCards(1)
        description = "At the beginning of your upkeep, if you control the artifact with the " +
            "greatest mana value or tied for the greatest mana value, draw a card."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "59"
        artist = "Matt Stewart"
        flavorText = "\"Impress me.\""
        imageUri = "https://cards.scryfall.io/normal/front/e/3/e31b30a7-13e8-408e-a758-60e6e9290808.jpg?1783937217"
    }
}
