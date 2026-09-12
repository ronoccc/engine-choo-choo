package com.wingedsheep.mtg.sets.definitions.ecc.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Return of the Wildspeaker
 * {4}{G}
 * Instant
 * Choose one —
 * • Draw cards equal to the greatest power among non-Human creatures you control.
 * • Non-Human creatures you control get +3/+3 until end of turn.
 *
 * Mode 1 is [DynamicAmounts.battlefield]'s `maxPower()` scoped to a non-Human filter. Mode 2 is
 * the Goring Ceratops [Effects.ForEachInGroup] shape with a fixed [Effects.ModifyStats] instead of
 * a keyword grant.
 */
val ReturnOfTheWildspeaker = card("Return of the Wildspeaker") {
    manaCost = "{4}{G}"
    colorIdentity = "G"
    typeLine = "Instant"
    oracleText = "Choose one —\n" +
        "• Draw cards equal to the greatest power among non-Human creatures you control.\n" +
        "• Non-Human creatures you control get +3/+3 until end of turn."

    spell {
        val nonHuman = GameObjectFilter.Creature.youControl().notSubtype(Subtype("Human"))

        modal(chooseCount = 1) {
            mode("Draw cards equal to the greatest power among non-Human creatures you control") {
                effect = Effects.DrawCards(DynamicAmounts.battlefield(Player.You, nonHuman).maxPower())
            }
            mode("Non-Human creatures you control get +3/+3 until end of turn") {
                effect = Effects.ForEachInGroup(
                    GroupFilter(nonHuman),
                    Effects.ModifyStats(power = 3, toughness = 3, target = EffectTarget.Self)
                )
            }
        }
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "115"
        artist = "Chris Rallis"
        imageUri = "https://cards.scryfall.io/normal/front/2/f/2fa1dac5-51ba-403e-b48b-d2c0d23a8146.jpg?1783904544"
    }
}
