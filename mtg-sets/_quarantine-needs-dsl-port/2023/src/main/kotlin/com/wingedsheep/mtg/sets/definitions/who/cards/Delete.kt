package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.DealDamageEffect
import com.wingedsheep.sdk.scripting.effects.ForEachPlayerEffect
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Delete
 * {X}{R}{R}
 * Sorcery
 * Delete deals X damage to each nonartifact creature and each player.
 *
 * Modelled the same way as Inferno ("deals N damage to each creature and each player"), but with
 * two differences: the damage amount is X (bound at cast time, [DynamicAmount.XValue]) and the
 * creature half is filtered to nonartifact creatures ([GameObjectFilter.Creature] `and`
 * [GameObjectFilter.Nonartifact]) so an opponent's artifact creatures survive the sweep. Damage to
 * creatures and damage to players are independent `Composite` branches, matching the card's two
 * simultaneous "each" clauses.
 */
val Delete = card("Delete") {
    manaCost = "{X}{R}{R}"
    colorIdentity = "R"
    typeLine = "Sorcery"
    oracleText = "Delete deals X damage to each nonartifact creature and each player."

    spell {
        effect = Effects.Composite(
            Effects.ForEachInGroup(
                GroupFilter(GameObjectFilter.Creature and GameObjectFilter.Nonartifact),
                DealDamageEffect(DynamicAmount.XValue, EffectTarget.Self)
            ),
            ForEachPlayerEffect(Player.Each, listOf(DealDamageEffect(DynamicAmount.XValue, EffectTarget.Controller)))
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
