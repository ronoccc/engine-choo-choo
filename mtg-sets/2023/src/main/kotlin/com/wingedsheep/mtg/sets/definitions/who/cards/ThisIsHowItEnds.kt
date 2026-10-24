package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.EffectChoice
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.predicates.ControllerPredicate
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * This Is How It Ends
 * {3}{B}
 * Instant
 *
 * Target creature's owner shuffles it into their library, then faces a villainous choice — They
 * lose 5 life, or they shuffle another creature they own into their library.
 */
val ThisIsHowItEnds = card("This Is How It Ends") {
    manaCost = "{3}{B}"
    colorIdentity = "B"
    typeLine = "Instant"
    oracleText = "Target creature's owner shuffles it into their library, then faces a villainous choice — " +
        "They lose 5 life, or they shuffle another creature they own into their library."

    spell {
        val creature = target(TargetFilter.Creature)

        val shuffleAnotherOwnedCreature: com.wingedsheep.sdk.scripting.effects.Effect = Effects.Composite(
            listOf(
                GatherCardsEffect(
                    source = CardSource.BattlefieldMatching(
                        filter = GameObjectFilter.Creature.withControllerPredicate(
                            ControllerPredicate.OwnedByReferencedPlayer(EffectTarget.TargetController)
                        )
                    ),
                    storeAs = "thisIsHowItEnds_gathered"
                ),
                SelectFromCollectionEffect(
                    from = "thisIsHowItEnds_gathered",
                    selection = SelectionMode.ChooseExactly(DynamicAmount.Fixed(1)),
                    chooser = com.wingedsheep.sdk.scripting.effects.Chooser.ControllerOfTarget,
                    storeSelected = "thisIsHowItEnds_chosen",
                    useTargetingUI = true,
                    prompt = "Choose another creature you own to shuffle into your library"
                ),
                MoveCollectionEffect(
                    from = "thisIsHowItEnds_chosen",
                    destination = CardDestination.ToZone(
                        Zone.LIBRARY,
                        player = Player.OwnerOf("target creature"),
                        placement = ZonePlacement.Shuffled
                    )
                )
            )
        )

        effect = Effects.Composite(
            listOf(
                Effects.ShuffleIntoLibrary(creature),
                Effects.ChooseAction(
                    choices = listOf(
                        EffectChoice(
                            label = "They lose 5 life",
                            effect = Effects.LoseLife(5, EffectTarget.TargetController)
                        ),
                        EffectChoice(
                            label = "They shuffle another creature they own into their library",
                            effect = shuffleAnotherOwnedCreature
                        )
                    ),
                    player = EffectTarget.TargetController
                )
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "70"
        artist = "Eliz Roxs"
        flavorText = "\"There's a gravestone here for someone with the same name as me.\""
        imageUri = "https://cards.scryfall.io/normal/front/2/5/250f6265-c0c8-4f51-bea8-408caf0a58d8.jpg?1783914657"
    }
}
