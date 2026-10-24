package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.conditions.Compare
import com.wingedsheep.sdk.scripting.conditions.ComparisonOperator
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

    val opponentLostThreeOrMore = Compare(
        left = DynamicAmount.TurnTracking(Player.EachOpponent, TurnTracker.LIFE_LOST_AMOUNT),
        operator = ComparisonOperator.GTE,
        right = DynamicAmount.Fixed(3)
    )

    val perOpponentLostThreeOrMore = Compare(
        left = DynamicAmount.TurnTracking(Player.You, TurnTracker.LIFE_LOST_AMOUNT),
        operator = ComparisonOperator.GTE,
        right = DynamicAmount.Fixed(3)
    )

    triggeredAbility {
        trigger = Triggers.you.beginningOf(Step.END)
        effect = Effects.Composite(
            listOf(
                Effects.If(
                    condition = opponentLostThreeOrMore,
                    then = Effects.CreateToken(
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
                    Effects.If(
                        condition = perOpponentLostThreeOrMore,
                        then = Effects.ChooseAction(
                            choices = listOf(
                                EffectChoice(
                                    label = "You draw a card",
                                    effect = Effects.DrawCards(1, EffectTarget.PlayerRef(Player.ControllerOfSource))
                                ),
                                EffectChoice(
                                    label = "That player discards a card",
                                    effect = Effects.Discard(1, EffectTarget.Controller),
                                    feasibilityCheck = FeasibilityCheck.HasCardsInZone(Zone.HAND)
                                )
                            ),
                            player = EffectTarget.Controller
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
