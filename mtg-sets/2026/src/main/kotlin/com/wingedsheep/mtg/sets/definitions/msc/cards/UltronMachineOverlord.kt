package com.wingedsheep.mtg.sets.definitions.msc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Ultron, Machine Overlord
 * {5}
 * Legendary Artifact Creature — Robot Villain
 * 4/4
 *
 * Flying
 * Other Robots and Constructs you control get +2/+2.
 *
 * The Lord of the Accursed shape: a static [ModifyStats] over Robots/Constructs you control with
 * `excludeSelf = true` ("other"), matching either subtype via `withAnySubtype`.
 */
val UltronMachineOverlord = card("Ultron, Machine Overlord") {
    manaCost = "{5}"
    colorIdentity = ""
    typeLine = "Legendary Artifact Creature — Robot Villain"
    power = 4
    toughness = 4
    oracleText = "Flying\nOther Robots and Constructs you control get +2/+2."

    keywords(Keyword.FLYING)

    staticAbility {
        ability = ModifyStats(
            powerBonus = 2,
            toughnessBonus = 2,
            filter = GroupFilter(
                GameObjectFilter.Permanent.withAnySubtype("Robot", "Construct").youControl(),
                excludeSelf = true
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "507"
        artist = "Kieran Yanner"
        imageUri = "https://cards.scryfall.io/normal/front/b/3/b38f8149-0ab8-42cb-9af6-8c647550ab68.jpg?1783903113"
    }
}
