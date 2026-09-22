package com.wingedsheep.mtg.sets.definitions.kld.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.scripting.effects.Gate
import com.wingedsheep.sdk.scripting.effects.GatedEffect
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Combustible Gearhulk
 * {4}{R}{R}
 * Artifact Creature — Construct
 * 6/6
 *
 * First strike
 * When this creature enters, target opponent may have you draw three cards. If the player
 * doesn't, you mill three cards, then this creature deals damage to that player equal to the
 * total mana value of those cards.
 *
 * The Palantír-of-Orthanc shape from the language reference: a `GatedEffect` with
 * `Gate.MayDecide` delegated to the target opponent (`decisionMaker`), `then` = you draw three,
 * `otherwise` = mill three then deal damage equal to `ManaValueSumOfCollection` of the mill
 * (`Patterns.Library.mill` always stores its cards under the `"milled"` collection name).
 */
val CombustibleGearhulk = card("Combustible Gearhulk") {
    manaCost = "{4}{R}{R}"
    colorIdentity = "R"
    typeLine = "Artifact Creature — Construct"
    power = 6
    toughness = 6
    oracleText = "First strike\n" +
        "When this creature enters, target opponent may have you draw three cards. If the player " +
        "doesn't, you mill three cards, then this creature deals damage to that player equal to " +
        "the total mana value of those cards."

    keywords(Keyword.FIRST_STRIKE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val opponent = target("target opponent", Targets.Opponent)
        effect = GatedEffect(
            gate = Gate.MayDecide(
                prompt = "You may have this player's opponent draw three cards."
            ),
            then = Effects.DrawCards(3, EffectTarget.Controller),
            otherwise = Effects.Composite(
                Patterns.Library.mill(3),
                Effects.DealDamage(
                    DynamicAmount.ManaValueSumOfCollection("milled"),
                    opponent
                )
            ),
            decisionMaker = opponent
        )
        description = "When this creature enters, target opponent may have you draw three cards. " +
            "If the player doesn't, you mill three cards, then this creature deals damage to " +
            "that player equal to the total mana value of those cards."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "112"
        artist = "Daarken"
        imageUri = "https://cards.scryfall.io/normal/front/e/0/e0f43147-4552-48fd-be0a-0629b9a0ad69.jpg?1783937195"
    }
}
