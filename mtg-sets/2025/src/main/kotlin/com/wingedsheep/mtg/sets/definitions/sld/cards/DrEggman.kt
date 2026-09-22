package com.wingedsheep.mtg.sets.definitions.sld.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.ForEachPlayerEffect
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.effects.PlayerChoiceEffect
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Dr. Eggman
 * {2}{U}{B}{R}
 * Legendary Creature — Human Scientist
 * 3/6
 *
 * Flying
 * At the beginning of your end step, draw a card. Then each opponent faces a villainous choice —
 * That player discards a card, or you may put a Construct, Robot, or Vehicle card from your hand
 * onto the battlefield.
 *
 * Composition: `Triggers.YourEndStep` → draw a card → `ForEachPlayerEffect(EachOpponent)`, whose
 * body's `PlayerChoiceEffect(chooser = Chooser.Controller)` resolves the chooser to *the iterated
 * opponent* (the loop rebinds `controllerId` per iteration — see
 * [ForEachPlayerEffect]'s own doc). "That player discards a card" reads that rebound controller
 * (`EffectTarget.Controller`); "you may put a … card from your hand onto the battlefield" reaches
 * back out to the real caster via `Player.ControllerOfSource` (the Davros, Dalek Creator
 * convention for "you" inside the same shape of loop), gathering from *that* player's hand rather
 * than the iterated opponent's.
 */
val DrEggman = card("Dr. Eggman") {
    manaCost = "{2}{U}{B}{R}"
    colorIdentity = "UBR"
    typeLine = "Legendary Creature — Human Scientist"
    power = 3
    toughness = 6
    oracleText = "Flying\n" +
        "At the beginning of your end step, draw a card. Then each opponent faces a villainous " +
        "choice — That player discards a card, or you may put a Construct, Robot, or Vehicle " +
        "card from your hand onto the battlefield."

    keywords(Keyword.FLYING)

    val theyDiscard = Effects.Discard(1, EffectTarget.Controller)

    val youMayPutOneOntoBattlefield = Effects.Composite(
        listOf(
            GatherCardsEffect(
                source = CardSource.FromZone(
                    Zone.HAND,
                    Player.ControllerOfSource,
                    filter = GameObjectFilter.Nonland.withAnySubtype("Construct", "Robot", "Vehicle")
                ),
                storeAs = "eggman_eligible"
            ),
            SelectFromCollectionEffect(
                from = "eggman_eligible",
                selection = SelectionMode.ChooseUpTo(DynamicAmount.Fixed(1)),
                chooser = Chooser.SourceController,
                storeSelected = "eggman_toPlay",
                showAllCards = true,
                prompt = "You may put a Construct, Robot, or Vehicle card from your hand onto the battlefield",
                selectedLabel = "Put onto the battlefield"
            ),
            MoveCollectionEffect(
                from = "eggman_toPlay",
                destination = CardDestination.ToZone(Zone.BATTLEFIELD, player = Player.ControllerOfSource)
            )
        )
    )

    triggeredAbility {
        trigger = Triggers.YourEndStep
        effect = Effects.Composite(
            listOf(
                Effects.DrawCards(1),
                ForEachPlayerEffect(
                    players = Player.EachOpponent,
                    effects = listOf(
                        PlayerChoiceEffect(
                            chooser = Chooser.Controller,
                            options = listOf(
                                Mode.noTarget(theyDiscard, "That player discards a card"),
                                Mode.noTarget(
                                    youMayPutOneOntoBattlefield,
                                    "You may put a Construct, Robot, or Vehicle card from your hand onto the battlefield"
                                )
                            )
                        )
                    )
                )
            )
        )
        description = "At the beginning of your end step, draw a card. Then each opponent faces " +
            "a villainous choice — That player discards a card, or you may put a Construct, " +
            "Robot, or Vehicle card from your hand onto the battlefield."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "2084"
        artist = "Nathalie Fourdraine"
        imageUri = "https://cards.scryfall.io/normal/front/1/8/18602e92-69be-4626-a28a-463913108f42.jpg?1783906091"
    }
}
