package com.wingedsheep.mtg.sets.definitions.m3c.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeywordToOwnSpells

/**
 * Silverquill Lecturer
 * {4}{W}
 * Creature — Kor Wizard
 * Creature spells you cast have demonstrate. (Whenever you cast a creature spell, you may copy
 * it. If you do, choose an opponent to also copy it. Each copy becomes a token.)
 *
 * [GrantKeywordToOwnSpells] with no `keywordParameter` — demonstrate is a plain boolean keyword,
 * unlike Casualty's threshold. The granted-demonstrate reflexive trigger CastSpellHandler
 * synthesizes reads [com.wingedsheep.sdk.model.CardScript.spellEffect] off the cast creature
 * card, which is `null` for an ordinary creature with no spell-resolution effect of its own —
 * `StormCopyEffect.spellEffect` accepts that (the copy resolves through the card definition
 * itself, the same as the original), and a resolving copy of a permanent spell becomes a token
 * as it enters the battlefield (CR 706.9), same as any other spell copy.
 */
val SilverquillLecturer = card("Silverquill Lecturer") {
    manaCost = "{4}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Kor Wizard"
    oracleText = "Creature spells you cast have demonstrate. (Whenever you cast a creature spell, " +
        "you may copy it. If you do, choose an opponent to also copy it. Each copy becomes a token.)"
    power = 3
    toughness = 3

    staticAbility {
        ability = GrantKeywordToOwnSpells(
            keyword = Keyword.DEMONSTRATE,
            spellFilter = GameObjectFilter.Creature
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "96"
        artist = "Ryan Pancoast"
        flavorText = "\"Did you bring enough to share with the rest of the class?\""
        imageUri = "https://cards.scryfall.io/normal/front/5/0/507e34d0-cdc5-4226-9664-b725b702044d.jpg?1783911411"
    }
}
