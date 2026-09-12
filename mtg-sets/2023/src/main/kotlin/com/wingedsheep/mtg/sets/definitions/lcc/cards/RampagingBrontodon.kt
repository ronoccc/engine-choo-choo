package com.wingedsheep.mtg.sets.definitions.lcc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Rampaging Brontodon
 * {5}{G}{G}
 * Creature — Dinosaur
 * Trample
 * Whenever this creature attacks, it gets +1/+1 until end of turn for each land you control.
 *
 * [DynamicAmounts.battlefield] count of [GameObjectFilter.Land] you control feeds
 * [Effects.ModifyStats]'s dynamic-amount overload, targeting [EffectTarget.Self].
 */
val RampagingBrontodon = card("Rampaging Brontodon") {
    manaCost = "{5}{G}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Dinosaur"
    power = 7
    toughness = 7
    oracleText = "Trample\n" +
        "Whenever this creature attacks, it gets +1/+1 until end of turn for each land you control."

    keywords(Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = Effects.ModifyStats(
            power = DynamicAmounts.battlefield(Player.You, GameObjectFilter.Land).count(),
            toughness = DynamicAmounts.battlefield(Player.You, GameObjectFilter.Land).count(),
            target = EffectTarget.Self
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "247"
        artist = "Lars Grant-West"
        imageUri = "https://cards.scryfall.io/normal/front/e/a/ea3f71d6-e333-4e50-9545-deb73749413c.jpg?1783913858"
    }
}
