package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Delete
 * {X}{R}{R}
 * Sorcery
 * Delete deals X damage to each nonartifact creature and each player.
 */
val Delete = card("Delete") {
    manaCost = "{X}{R}{R}"
    colorIdentity = "R"
    typeLine = "Sorcery"
    oracleText = "Delete deals X damage to each nonartifact creature and each player."

    spell {
        effect = Effects.ForEachInGroup(
            GroupFilter(GameObjectFilter.Creature and GameObjectFilter.Nonartifact),
            Effects.DealDamage(DynamicAmount.XValue, EffectTarget.IterationEntity)
        ) then Effects.ForEachPlayer(
            Player.Each,
            Effects.DealDamage(DynamicAmount.XValue, EffectTarget.Controller)
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "81"
        artist = "Steve Argyle"
        flavorText = "\"You will be deleted.\""
        imageUri = "https://cards.scryfall.io/normal/front/8/7/875c7337-78af-4e13-89e5-3255ba2d4053.jpg?1783914653"
    }
}
