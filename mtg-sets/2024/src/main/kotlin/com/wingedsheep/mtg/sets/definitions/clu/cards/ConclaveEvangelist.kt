package com.wingedsheep.mtg.sets.definitions.clu.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Conclave Evangelist
 * {3}{G/W}{G/W}
 * Creature — Elephant Cleric
 * Myriad (Whenever this creature attacks, for each opponent other than defending player, you may
 * create a token copy that's tapped and attacking that player or a planeswalker they control.
 * Exile the tokens at end of combat.)
 * Whenever this creature deals combat damage to a player, create a token that's a copy of this
 * creature.
 */
val ConclaveEvangelist = card("Conclave Evangelist") {
    manaCost = "{3}{G/W}{G/W}"
    colorIdentity = "GW"
    typeLine = "Creature — Elephant Cleric"
    keywords(Keyword.MYRIAD)
    power = 4
    toughness = 4
    oracleText = "Myriad (Whenever this creature attacks, for each opponent other than defending " +
        "player, you may create a token copy that's tapped and attacking that player or a " +
        "planeswalker they control. Exile the tokens at end of combat.)\n" +
        "Whenever this creature deals combat damage to a player, create a token that's a copy of " +
        "this creature."

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = CreateTokenCopyOfTargetEffect(
            target = EffectTarget.Self,
            myriadPerOpponent = true,
            exileAtStep = Step.END_COMBAT,
        )
    }

    triggeredAbility {
        trigger = Triggers.DealsCombatDamageToPlayer
        effect = CreateTokenCopyOfTargetEffect(target = EffectTarget.Self)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "27"
        artist = "John Tedrick"
        imageUri = "https://cards.scryfall.io/normal/front/0/d/0dcccbd8-2135-45e6-8802-8dcaa382a398.jpg?1783912577"
    }
}
