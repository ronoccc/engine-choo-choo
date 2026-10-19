package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.effects.MoveType
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.effects.ZonePlacement
import com.wingedsheep.sdk.scripting.predicates.ControllerPredicate
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Midnight Crusader Shuttle
 * {4}
 * Artifact — Vehicle
 * 3/4
 *
 * Midnight Entity — Whenever this Vehicle attacks, defending player faces a villainous choice —
 * That player sacrifices a creature of their choice, or you gain control of a creature of their
 * choice that player controls until end of turn. If you gain control of a creature this way, tap
 * it, and it's attacking that player.
 * Crew 2
 *
 * Modeling notes:
 *  - "Midnight Entity" is flavor/typal text for the Doctor Who "Midnight" subset, not a keyword;
 *    it is preserved verbatim in oracleText but has no separate mechanical effect.
 *  - `Chooser.DefendingPlayer` for the villainous choice itself (they decide which branch);
 *    within the sacrifice branch, the sacrificed creature is *their own* choice too (Gather →
 *    Select(Chooser.DefendingPlayer) → Move as Sacrifice, mirroring This Is How It Ends' "another
 *    creature they own" shape). Within the gain-control branch, the creature is *your* choice
 *    (Chooser.Controller) among the defending player's creatures.
 *  - Fidelity gap: "tap it, and it's attacking that player" — the tap is modelled
 *    (`Effects.Tap`), but the engine has no primitive for retroactively adding a permanent to the
 *    current combat as an attacker mid-resolution (no card in this corpus needed that shape
 *    before). The gained creature becomes an ordinary permanent under the attacker's control,
 *    tapped, but does not deal combat damage this combat. Documented here; left as a TODO rather
 *    than silently dropped.
 */
val MidnightCrusaderShuttle = card("Midnight Crusader Shuttle") {
    manaCost = "{4}"
    colorIdentity = ""
    typeLine = "Artifact — Vehicle"
    power = 3
    toughness = 4
    oracleText = "Midnight Entity — Whenever this Vehicle attacks, defending player faces a " +
        "villainous choice — That player sacrifices a creature of their choice, or you gain " +
        "control of a creature of their choice that player controls until end of turn. If you " +
        "gain control of a creature this way, tap it, and it's attacking that player.\n" +
        "Crew 2"

    keywordAbility(KeywordAbility.crew(2))

    val theyChooseAndSacrifice = Effects.Composite(
        listOf(
            GatherCardsEffect(
                source = CardSource.BattlefieldMatching(
                    filter = GameObjectFilter.Creature.withControllerPredicate(
                        ControllerPredicate.ControlledByReferencedPlayer(EffectTarget.PlayerRef(Player.DefendingPlayer))
                    )
                ),
                storeAs = "midnightShuttle_theirCreatures"
            ),
            SelectFromCollectionEffect(
                from = "midnightShuttle_theirCreatures",
                selection = SelectionMode.ChooseExactly(DynamicAmount.Fixed(1)),
                chooser = Chooser.DefendingPlayer,
                storeSelected = "midnightShuttle_toSacrifice",
                useTargetingUI = true,
                prompt = "Choose a creature to sacrifice"
            ),
            MoveCollectionEffect(
                // MoveType.Sacrifice routes each card to its own owner's graveyard regardless
                // of `player` here (CR 701.21a) — see the enum doc on MoveType.Sacrifice.
                from = "midnightShuttle_toSacrifice",
                destination = CardDestination.ToZone(Zone.GRAVEYARD),
                moveType = MoveType.Sacrifice
            )
        )
    )

    // TODO: the gained creature does not retroactively join this combat as an attacker — see the
    // fidelity-gap note above. Everything else (your choice of their creature, gaining control
    // until end of turn, tapping it) is modelled.
    val youGainControlAndTap = Effects.Composite(
        listOf(
            GatherCardsEffect(
                source = CardSource.BattlefieldMatching(
                    filter = GameObjectFilter.Creature.withControllerPredicate(
                        ControllerPredicate.ControlledByReferencedPlayer(EffectTarget.PlayerRef(Player.DefendingPlayer))
                    )
                ),
                storeAs = "midnightShuttle_theirCreatures2"
            ),
            SelectFromCollectionEffect(
                from = "midnightShuttle_theirCreatures2",
                selection = SelectionMode.ChooseExactly(DynamicAmount.Fixed(1)),
                chooser = Chooser.Controller,
                storeSelected = "midnightShuttle_toSteal",
                useTargetingUI = true,
                prompt = "Choose a creature to gain control of"
            ),
            Effects.GainControl(
                EffectTarget.PipelineTarget("midnightShuttle_toSteal"),
                com.wingedsheep.sdk.scripting.Duration.EndOfTurn
            ),
            Effects.Tap(EffectTarget.PipelineTarget("midnightShuttle_toSteal"))
        )
    )

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = com.wingedsheep.sdk.scripting.effects.PlayerChoiceEffect(
            chooser = Chooser.DefendingPlayer,
            options = listOf(
                Mode.noTarget(theyChooseAndSacrifice, "That player sacrifices a creature of their choice"),
                Mode.noTarget(
                    youGainControlAndTap,
                    "You gain control of a creature of their choice that player controls until end of turn"
                )
            )
        )
        description = "Whenever this Vehicle attacks, defending player faces a villainous choice " +
            "— That player sacrifices a creature of their choice, or you gain control of a " +
            "creature of their choice that player controls until end of turn."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "179"
        artist = "Evan Shipard"
        imageUri = "https://cards.scryfall.io/normal/front/0/a/0a8dcf01-8e2c-439c-81c4-650dd01ecb65.jpg?1783914615"
    }
}
