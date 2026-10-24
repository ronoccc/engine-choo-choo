package com.wingedsheep.mtg.sets.definitions.mar.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AdditionalSourceTriggers
import com.wingedsheep.sdk.scripting.ChoiceType
import com.wingedsheep.sdk.scripting.EntersWithChoice
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantChosenSubtype
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.effects.WardCost

/**
 * Doom Variant — the Marvel Universe printing of Roaming Throne.
 * Decks that list the card by the name printed on that face ("Doom Variant") need this
 * name; the Lost Caverns printing is already registered as "Roaming Throne".
 *
 * {4}
 * Artifact Creature — Golem
 * 4/4
 * Ward {2}
 * As this creature enters, choose a creature type.
 * This creature is the chosen type in addition to its other types.
 * If a triggered ability of another creature you control of the chosen type triggers,
 * it triggers an additional time.
 */
val DoomVariant = card("Doom Variant") {
    manaCost = "{4}"
    colorIdentity = ""
    typeLine = "Artifact Creature — Golem"
    power = 4
    toughness = 4
    oracleText = "Ward {2}\n" +
        "As this creature enters, choose a creature type.\n" +
        "This creature is the chosen type in addition to its other types.\n" +
        "If a triggered ability of another creature you control of the chosen type triggers, it triggers an additional time."

    keywordAbility(KeywordAbility.Ward(WardCost.Mana("{2}")))
    replacementEffect(EntersWithChoice(ChoiceType.CREATURE_TYPE))

    staticAbility {
        ability = GrantChosenSubtype()
    }

    staticAbility {
        ability = AdditionalSourceTriggers(
            sourceFilter = GameObjectFilter.Creature.withChosenSubtype(),
            excludeSelf = true,
            description = "If a triggered ability of another creature you control of the chosen type triggers, it triggers an additional time"
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "99"
        artist = "Alex Ross"
    }
}
