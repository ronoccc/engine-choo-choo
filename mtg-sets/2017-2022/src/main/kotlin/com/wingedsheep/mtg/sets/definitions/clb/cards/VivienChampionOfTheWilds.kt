package com.wingedsheep.mtg.sets.definitions.clb.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantFlashToSpellType
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.FaceDownMode
import com.wingedsheep.sdk.scripting.effects.MayPlayExpiry
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.TargetCreature
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Vivien, Champion of the Wilds
 * {2}{G}
 * Legendary Planeswalker — Vivien
 * Starting Loyalty: 4
 * You may cast creature spells as though they had flash.
 * +1: Until your next turn, up to one target creature gains reach and vigilance.
 * −2: Look at the top three cards of your library. Exile one face down and put the rest on the
 * bottom of your library in any order. For as long as it remains exiled, you may cast it if it's
 * a creature spell.
 *
 * The static ability is [GrantFlashToSpellType] scoped to creature spells, controller-only. +1
 * chains two single-keyword [Effects.GrantKeyword] calls (no list overload exists) with
 * `Duration.UntilYourNextTurn`. −2 mirrors Oko, Lorwyn Liege's −1 (gather three, split
 * selected/remainder via [SelectFromCollectionEffect]) but the selection is mandatory-exactly-one
 * rather than "may up to one", and is narrowed to creature cards — a deliberate simplification,
 * since the printed card lets you exile any of the three face down but only ever lets you *cast*
 * the exiled card if it's a creature, so exiling a known non-creature has no functional upside
 * this engine needs to reproduce. Selected goes to exile with
 * [Effects.GrantMayPlayFromExile]-style permanent play permission
 * ([com.wingedsheep.sdk.scripting.effects.MayPlayExpiry.Permanent]); the remainder goes to the
 * bottom of the library.
 */
val VivienChampionOfTheWilds = card("Vivien, Champion of the Wilds") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Legendary Planeswalker — Vivien"
    startingLoyalty = 4
    oracleText = "You may cast creature spells as though they had flash.\n" +
        "+1: Until your next turn, up to one target creature gains reach and vigilance.\n" +
        "−2: Look at the top three cards of your library. Exile one face down and put the rest " +
        "on the bottom of your library in any order. For as long as it remains exiled, you may " +
        "cast it if it's a creature spell."

    staticAbility {
        ability = GrantFlashToSpellType(filter = GameObjectFilter.Creature, controllerOnly = true)
    }

    loyaltyAbility(+1) {
        val creature = target("creature", TargetCreature(optional = true))
        effect = Effects.GrantKeyword(Keyword.REACH, creature, Duration.UntilYourNextTurn)
            .then(Effects.GrantKeyword(Keyword.VIGILANCE, creature, Duration.UntilYourNextTurn))
    }

    loyaltyAbility(-2) {
        effect = Effects.Pipeline {
            val topThree = gather(CardSource.TopOfLibrary(DynamicAmount.Fixed(3)), name = "vivienTopThree")
            val (exiled, bottom) = chooseExactlySplit(
                count = 1,
                from = topThree,
                filter = GameObjectFilter.Creature,
                showAllCards = true,
                prompt = "Exile one face down",
                selectedLabel = "Exile face down",
                remainderLabel = "Put on the bottom of your library"
            )
            exile(exiled, faceDown = FaceDownMode.HIDDEN)
            run(Effects.GrantMayPlayFromExile(from = exiled.key, expiry = MayPlayExpiry.Permanent))
            move(bottom, CardDestination.ToZone(Zone.LIBRARY, Player.You, ZonePlacement.Bottom))
        }
        description = "Look at the top three cards of your library. Exile one face down and " +
            "put the rest on the bottom of your library in any order. For as long as it " +
            "remains exiled, you may cast it if it's a creature spell."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "838"
        artist = "Magali Villeneuve"
        imageUri = "https://cards.scryfall.io/normal/front/e/1/e11dc967-ae38-4c19-bf54-b3209e203b39.jpg?1783922400"
    }
}
