package com.wingedsheep.mtg.sets.definitions.c15.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Command Beacon
 * Land
 * {T}: Add {C}.
 * {T}, Sacrifice Command Beacon: Put your commander into your hand from the command zone.
 *
 * The second ability is not a target: it gathers commanders from your command zone and you
 * pick one (partners). Dungeons and other command-zone objects are left out by [GameObjectFilter.commander].
 */
val CommandBeacon = card("Command Beacon") {
    colorIdentity = ""
    typeLine = "Land"
    oracleText = "{T}: Add {C}.\n" +
        "{T}, Sacrifice this land: Put your commander into your hand from the command zone."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddColorlessMana(1)
        manaAbility = true
        timing = TimingRule.ManaAbility
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Tap, Costs.SacrificeSelf)
        effect = Effects.Pipeline {
            val commanders = gather(
                CardSource.FromZone(Zone.COMMAND, Player.You, GameObjectFilter.Any.commander()),
                name = "commanders"
            )
            val chosen = chooseExactly(
                1,
                from = commanders,
                prompt = "Put your commander into your hand",
                name = "commander"
            )
            toHand(chosen)
        }
        description = "Put your commander into your hand from the command zone."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "55"
        artist = "James Arnold"
    }
}
