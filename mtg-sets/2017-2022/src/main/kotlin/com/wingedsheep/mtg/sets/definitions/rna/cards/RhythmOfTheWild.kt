package com.wingedsheep.mtg.sets.definitions.rna.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantCantBeCountered
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Rhythm of the Wild
 * {1}{R}{G}
 * Enchantment
 * Creature spells you control can't be countered.
 * Nontoken creatures you control have riot. (They enter with your choice of a +1/+1 counter or
 * haste.)
 *
 * [GrantCantBeCountered] scoped to your creature spells, plus a static [GrantKeyword] of
 * [Keyword.RIOT] to nontoken creatures you control — riot is explicitly grant-aware (see the
 * keyword's own doc comment), so the engine synthesizes the enters-with-choice for every matching
 * permanent as it enters, exactly as if it were printed.
 */
val RhythmOfTheWild = card("Rhythm of the Wild") {
    manaCost = "{1}{R}{G}"
    colorIdentity = "RG"
    typeLine = "Enchantment"
    oracleText = "Creature spells you control can't be countered.\n" +
        "Nontoken creatures you control have riot. (They enter with your choice of a +1/+1 " +
        "counter or haste.)"

    staticAbility {
        ability = GrantCantBeCountered(filter = GameObjectFilter.Creature.youControl())
    }

    staticAbility {
        ability = GrantKeyword(
            keyword = Keyword.RIOT,
            filter = GroupFilter(GameObjectFilter.Creature.youControl().nontoken())
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "201"
        artist = "Tomasz Jedruszek"
        imageUri = "https://cards.scryfall.io/normal/front/8/4/84062ce2-fea2-4e06-b83b-7cc597fb2a1b.jpg?1783933638"
    }
}
