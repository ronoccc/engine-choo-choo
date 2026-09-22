package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Raiders' Wake
 * {3}{B}
 * Enchantment
 * Whenever an opponent discards a card, that player loses 2 life.
 * Raid — At the beginning of your end step, if you attacked this turn, target opponent discards a card.
 *
 * Two independent triggered abilities:
 * - [Triggers.AnyOpponentDiscards] fires once per discarded card, binding it as the triggering
 *   entity (it's a public object now sitting in a graveyard, CR 400.7e). "That player" is the
 *   discarding player, i.e. the controller/owner of that card —
 *   [EffectTarget.ControllerOfTriggeringEntity].
 * - Raid is [Triggers.YourEndStep] with an intervening-if on [Conditions.YouAttackedThisTurn],
 *   targeting an opponent who discards a card. "Raid" isn't a modelled keyword ability in this
 *   SDK (it's reminder-text flavor over a plain intervening-if, matching how Bellowing Saddlebrute
 *   and other Raid cards are modelled elsewhere in the corpus) — the oracle text keeps the
 *   "Raid —" header, but no `keywords(...)` call is needed.
 */
val RaidersWake = card("Raiders' Wake") {
    manaCost = "{3}{B}"
    colorIdentity = "B"
    typeLine = "Enchantment"
    oracleText = "Whenever an opponent discards a card, that player loses 2 life.\n" +
        "Raid — At the beginning of your end step, if you attacked this turn, target opponent discards a card."

    triggeredAbility {
        trigger = Triggers.AnyOpponentDiscards
        effect = Effects.LoseLife(2, EffectTarget.ControllerOfTriggeringEntity)
    }

    triggeredAbility {
        trigger = Triggers.YourEndStep
        interveningIf = Conditions.YouAttackedThisTurn
        target = Targets.Opponent
        effect = Effects.Discard(1, EffectTarget.ContextTarget(0))
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "116"
        artist = "Zack Stella"
        flavorText = "One was spared to tell the gruesome tale."
        imageUri = "https://cards.scryfall.io/normal/front/f/d/fd81dd41-a982-46d2-a572-432580be73c5.jpg?1783935756"
    }
}
