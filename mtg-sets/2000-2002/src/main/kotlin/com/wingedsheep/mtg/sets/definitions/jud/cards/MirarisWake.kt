package com.wingedsheep.mtg.sets.definitions.jud.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AdditionalManaOnSourceTap
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Mirari's Wake
 * {3}{G}{W}
 * Enchantment
 * Creatures you control get +1/+1.
 * Whenever you tap a land for mana, add one mana of any type that land produced.
 *
 * The mana-doubling half is the same primitive established for Vorinclex, Voice of Hunger and
 * Lavaleaper: [AdditionalManaOnSourceTap] with `color = null` ("mirror the produced color").
 */
val MirarisWake = card("Mirari's Wake") {
    manaCost = "{3}{G}{W}"
    colorIdentity = "GW"
    typeLine = "Enchantment"
    oracleText = "Creatures you control get +1/+1.\nWhenever you tap a land for mana, add one " +
        "mana of any type that land produced."

    staticAbility {
        ability = ModifyStats(
            powerBonus = 1,
            toughnessBonus = 1,
            filter = GroupFilter.AllCreaturesYouControl,
        )
    }

    staticAbility {
        ability = AdditionalManaOnSourceTap(
            sourceFilter = GameObjectFilter.Land.youControl(),
            color = null,
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "139"
        artist = "David Martin"
        flavorText = "The land drank power from the Mirari as though it had thirsted forever."
        imageUri = "https://cards.scryfall.io/normal/front/b/5/b5ddad46-5e2e-43c9-8c91-7aca6ca23562.jpg?1783945107"
    }
}
