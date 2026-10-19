package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
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
 *
 * The Doctor Who "villainous choice" mechanic: unlike a modal spell (CR 700.2), the pick here is
 * made by the *target's owner*, not the caster — [Effects.VillainousChoice] with
 * `Chooser.ControllerOfTarget`, whose owner-fallback resolves correctly once the creature has
 * already left the battlefield (it was shuffled away by the composite's first step, exactly the
 * "target already left earlier in the same resolution" case that chooser documents).
 *
 * Both options reference `EffectTarget.TargetController` for "they" (the same controller/owner
 * resolution the chooser used), not `EffectTarget.Controller` — this card never rebinds the
 * resolving context to the chooser, so "you" would still mean the caster if this card had a
 * caster-side option (it doesn't, but Great Intelligence's Plan does, and shares the primitive).
 *
 * "Another creature they own" is composed as a Gather → Select → Move pipeline scoped by
 * [ControllerPredicate.OwnedByReferencedPlayer] (a new predicate added alongside this card,
 * mirroring the existing [ControllerPredicate.OwnedByTargetPlayer]) rather than a raw target,
 * since the printed text never says "target" for the second creature — it is a plain
 * resolution-time choice, like Drop of Honey's `destroyLeastPowerCreature` pattern.
 */
val ThisIsHowItEnds = card("This Is How It Ends") {
    manaCost = "{3}{B}"
    colorIdentity = "B"
    typeLine = "Instant"
    oracleText = "Target creature's owner shuffles it into their library, then faces a villainous choice — " +
        "They lose 5 life, or they shuffle another creature they own into their library."

    spell {
        val creature = target("creature", Targets.Creature)

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
                    chooser = Chooser.ControllerOfTarget,
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
                Effects.VillainousChoice(
                    chooser = Chooser.ControllerOfTarget,
                    Mode.noTarget(
                        Effects.LoseLife(5, EffectTarget.TargetController),
                        "They lose 5 life"
                    ),
                    Mode.noTarget(
                        shuffleAnotherOwnedCreature,
                        "They shuffle another creature they own into their library"
                    ),
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
