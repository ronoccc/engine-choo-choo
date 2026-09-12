package com.wingedsheep.mtg.sets.definitions.mkc.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Farewell
 * {4}{W}{W}
 * Sorcery
 * Choose one or more —
 * • Exile all artifacts.
 * • Exile all creatures.
 * • Exile all enchantments.
 * • Exile all graveyards.
 *
 * "Choose one or more" out of exactly the four printed modes is `modal(chooseCount = 4,
 * minChooseCount = 1)` — no additional cost per extra mode, unlike Collective Brutality. The first
 * three modes are mass [Effects.Exile] over an [EffectTarget.GroupRef] (the Burning Sun's Avatar
 * "each creature" idiom, generalized to artifacts/enchantments). "Exile all graveyards" has no
 * battlefield group to scan, so it's a small [Effects.Pipeline]: gather every card from every
 * player's graveyard ([CardSource.FromZone] with [Player.Each]) and exile the whole collection.
 */
val Farewell = card("Farewell") {
    manaCost = "{4}{W}{W}"
    colorIdentity = "W"
    typeLine = "Sorcery"
    oracleText = "Choose one or more —\n" +
        "• Exile all artifacts.\n" +
        "• Exile all creatures.\n" +
        "• Exile all enchantments.\n" +
        "• Exile all graveyards."

    spell {
        modal(chooseCount = 4, minChooseCount = 1) {
            mode("Exile all artifacts") {
                effect = Effects.Exile(EffectTarget.GroupRef(GroupFilter.AllArtifacts))
            }
            mode("Exile all creatures") {
                effect = Effects.Exile(EffectTarget.GroupRef(GroupFilter.AllCreatures))
            }
            mode("Exile all enchantments") {
                effect = Effects.Exile(EffectTarget.GroupRef(GroupFilter.AllEnchantments))
            }
            mode("Exile all graveyards") {
                effect = Effects.Pipeline {
                    val allGraveyards = gather(
                        CardSource.FromZone(Zone.GRAVEYARD, Player.Each, GameObjectFilter.Any)
                    )
                    exile(allGraveyards)
                }
            }
        }
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "64"
        artist = "Seb McKinnon"
        imageUri = "https://cards.scryfall.io/normal/front/1/1/114d2180-093b-4838-97ad-badbc8ee50b0.jpg?1783913032"
    }
}
