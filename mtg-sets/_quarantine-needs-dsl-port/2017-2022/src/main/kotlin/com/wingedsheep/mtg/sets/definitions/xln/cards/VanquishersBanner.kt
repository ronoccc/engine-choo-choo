package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ChoiceType
import com.wingedsheep.sdk.scripting.EntersWithChoice
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Vanquisher's Banner
 * {5}
 * Artifact
 * As this artifact enters, choose a creature type.
 * Creatures you control of the chosen type get +1/+1.
 * Whenever you cast a creature spell of the chosen type, draw a card.
 *
 * Same [EntersWithChoice]/[GameObjectFilter.withChosenSubtype] pattern as Metallic Mimic for the
 * anthem (static [ModifyStats] over a [GroupFilter] scoped by `chosenSubtypeKey`), plus a
 * [Triggers.youCastSpell] filtered to creature spells with the chosen subtype for the draw.
 */
val VanquishersBanner = card("Vanquisher's Banner") {
    manaCost = "{5}"
    colorIdentity = ""
    typeLine = "Artifact"
    oracleText = "As this artifact enters, choose a creature type.\n" +
        "Creatures you control of the chosen type get +1/+1.\n" +
        "Whenever you cast a creature spell of the chosen type, draw a card."

    replacementEffect(EntersWithChoice(ChoiceType.CREATURE_TYPE))

    staticAbility {
        ability = ModifyStats(
            powerBonus = 1,
            toughnessBonus = 1,
            filter = GroupFilter(
                baseFilter = GameObjectFilter.Creature.youControl(),
                chosenSubtypeKey = "chosenCreatureType"
            )
        )
    }

    triggeredAbility {
        trigger = Triggers.youCastSpell(
            spellFilter = GameObjectFilter.Creature.withChosenSubtype()
        )
        effect = Effects.DrawCards(1)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "251"
        artist = "Milivoj Ćeran"
        imageUri = "https://cards.scryfall.io/normal/front/6/0/60b7a85f-3a30-4ece-9bbb-61f3c4b796b8.jpg?1783935699"
    }
}
