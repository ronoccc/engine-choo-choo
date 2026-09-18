package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Healing Technique
 * {3}{G}
 * Sorcery
 * Demonstrate (When you cast this spell, you may copy it. If you do, choose an opponent to also
 * copy it. Players may choose new targets for their copies.)
 * Return target card from your graveyard to your hand. You gain life equal to that card's mana
 * value. Exile Healing Technique.
 *
 * "Exile Healing Technique" uses the `spell { selfExile() }` builder flag
 * (CardScript.selfExileOnResolve) rather than an `Effects.Exile(EffectTarget.Self)` step in the
 * effect list — a mid-resolution self-move doesn't stick, because StackResolver's post-resolution
 * graveyard placement runs afterward regardless (see Avatar's Wrath).
 */
val HealingTechnique = card("Healing Technique") {
    manaCost = "{3}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    keywords(Keyword.DEMONSTRATE)
    oracleText = "Demonstrate (When you cast this spell, you may copy it. If you do, choose an " +
        "opponent to also copy it. Players may choose new targets for their copies.)\n" +
        "Return target card from your graveyard to your hand. You gain life equal to that card's " +
        "mana value. Exile Healing Technique."

    spell {
        val t = target(
            "target card in your graveyard",
            TargetObject(filter = TargetFilter.CardInGraveyard.ownedByYou())
        )
        effect = Effects.Move(t, Zone.HAND) then Effects.GainLife(DynamicAmounts.targetManaValue(0))
        selfExile()
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "63"
        artist = "Michele Parisi"
        imageUri = "https://cards.scryfall.io/normal/front/3/3/33897125-a1df-4d7a-a45a-9c049cb662f6.jpg?1783927590"
    }
}
