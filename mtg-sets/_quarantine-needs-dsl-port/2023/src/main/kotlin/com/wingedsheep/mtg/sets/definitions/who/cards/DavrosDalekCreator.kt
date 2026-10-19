package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.conditions.Compare
import com.wingedsheep.sdk.scripting.conditions.ComparisonOperator
import com.wingedsheep.sdk.scripting.effects.ChooseActionEffect
import com.wingedsheep.sdk.scripting.effects.ConditionalEffect
import com.wingedsheep.sdk.scripting.effects.DrawCardsEffect
import com.wingedsheep.sdk.scripting.effects.EffectChoice
import com.wingedsheep.sdk.scripting.effects.FeasibilityCheck
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.TurnTracker

/**
 * Davros, Dalek Creator {1}{U}{B}{R}
 * Legendary Artifact Creature — Alien Scientist
 * 3/4
 *
 * "Menace
 *  At the beginning of your end step, create a 3/3 black Dalek artifact creature token with
 *  menace if an opponent lost 3 or more life this turn. Then each opponent who lost 3 or more
 *  life this turn faces a villainous choice — You draw a card, or that player discards a card."
 *
 * Modeling notes:
 *  - "If an opponent lost 3 or more life this turn" gates the token half via `ConditionalEffect`
 *    on `Compare(TurnTracking(EachOpponent, LIFE_LOST_AMOUNT), GTE, Fixed(3))`. `EachOpponent`
 *    *sums* every opponent's life lost (`DynamicAmountEvaluator`), matching this codebase's
 *    existing convention for "if an opponent X this turn" boolean gates (`Conditions.OpponentLostLifeThisTurn`
 *    is built the same way for its own threshold-1 case) — a multiplayer table where no single
 *    opponent lost 3 but the total across opponents does will diverge from a strict per-opponent
 *    reading; documented here rather than silently assumed.
 *  - "Then each opponent who lost 3 or more life this turn faces a villainous choice" *is* exactly
 *    per-opponent, and gets it for free: `Effects.ForEachPlayer(Player.EachOpponent, …)` rebinds
 *    `Player.You` to the iterated opponent for each pass (per its own doc comment), so the same
 *    `Compare(TurnTracking(You, LIFE_LOST_AMOUNT), GTE, Fixed(3))` condition, evaluated inside the
 *    iteration, is a true per-opponent existential check.
 *  - The villainous choice is `ChooseActionEffect(isVillainousChoice = true, player = <the
 *    iterated opponent>)` — see [com.wingedsheep.sdk.scripting.VillainousChoiceExtraForOpponents]
 *    for how The Valeyard doubles it.
 */
val DavrosDalekCreator = card("Davros, Dalek Creator") {
    manaCost = "{1}{U}{B}{R}"
    colorIdentity = "UBR"
    typeLine = "Legendary Artifact Creature — Alien Scientist"
    power = 3
    toughness = 4
    oracleText = "Menace\n" +
        "At the beginning of your end step, create a 3/3 black Dalek artifact creature token " +
        "with menace if an opponent lost 3 or more life this turn. Then each opponent who lost " +
        "3 or more life this turn faces a villainous choice — You draw a card, or that player " +
        "discards a card."

    keywords(Keyword.MENACE)

    triggeredAbility {
        trigger = Triggers.YourEndStep
        effect = Effects.Composite(
            ConditionalEffect(
                condition = Compare(
                    DynamicAmount.TurnTracking(Player.EachOpponent, TurnTracker.LIFE_LOST_AMOUNT),
                    ComparisonOperator.GTE,
                    DynamicAmount.Fixed(3)
                ),
                effect = Effects.CreateToken(
                    power = 3,
                    toughness = 3,
                    colors = setOf(Color.BLACK),
                    creatureTypes = setOf("Dalek"),
                    keywords = setOf(Keyword.MENACE),
                    artifactToken = true,
                    imageUri = "https://cards.scryfall.io/normal/front/f/1/f176f6ec-1ff8-4cd4-b7a6-b95830532f6a.jpg?1783914717"
                )
            ),
            Effects.ForEachPlayer(
                Player.EachOpponent,
                listOf(
                    ConditionalEffect(
                        condition = Compare(
                            DynamicAmount.TurnTracking(Player.You, TurnTracker.LIFE_LOST_AMOUNT),
                            ComparisonOperator.GTE,
                            DynamicAmount.Fixed(3)
                        ),
                        effect = ChooseActionEffect(
                            choices = listOf(
                                EffectChoice(
                                    label = "You draw a card",
                                    effect = DrawCardsEffect(1, EffectTarget.PlayerRef(Player.ControllerOfSource))
                                ),
                                EffectChoice(
                                    label = "That player discards a card",
                                    effect = Effects.Discard(1, EffectTarget.PlayerRef(Player.You)),
                                    feasibilityCheck = FeasibilityCheck.HasCardsInZone(com.wingedsheep.sdk.core.Zone.HAND)
                                )
                            ),
                            player = EffectTarget.PlayerRef(Player.You),
                            isVillainousChoice = true
                        )
                    )
                )
            )
        )
        description = "At the beginning of your end step, create a 3/3 black Dalek artifact " +
            "creature token with menace if an opponent lost 3 or more life this turn. Then each " +
            "opponent who lost 3 or more life this turn faces a villainous choice — You draw a " +
            "card, or that player discards a card."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "1"
        artist = "Simon Dominic"
        imageUri = "https://cards.scryfall.io/normal/front/5/9/59c5bd2b-cf68-4e5e-819d-53917a6c5275.jpg?1783914687"
    }
}
