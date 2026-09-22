package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.effects.ForEachPlayerEffect
import com.wingedsheep.sdk.scripting.effects.MayEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Ruin Grinder
 * {5}{R}
 * Artifact Creature — Construct
 * 7/4
 * Menace
 * When this creature dies, each player may discard their hand and draw seven cards.
 * Mountaincycling {2} ({2}, Discard this card: Search your library for a Mountain card, reveal
 * it, put it into your hand, then shuffle.)
 *
 * The dies trigger is a per-player optional wheel: [ForEachPlayerEffect] over [Player.Each]
 * rebinds the controller to the currently-iterated player, and each one's
 * `Patterns.Hand.discardHand` + `Effects.DrawCards(7)` is wrapped in [MayEffect] with
 * `decisionMaker = Controller`, so every player independently chooses yes/no — same shape as
 * Raphael's Technique. Mountaincycling is `KeywordAbility.typecycling("Mountain", cost)`, the
 * same Cycling-variant plumbing as Ash Barrens' basic landcycling, but filtered to the
 * "Mountain" subtype rather than any basic land (a nonbasic Mountain is a legal search hit too).
 */
val RuinGrinder = card("Ruin Grinder") {
    manaCost = "{5}{R}"
    colorIdentity = "R"
    typeLine = "Artifact Creature — Construct"
    power = 7
    toughness = 4
    oracleText = "Menace\nWhen this creature dies, each player may discard their hand and draw " +
        "seven cards.\nMountaincycling {2} ({2}, Discard this card: Search your library for a " +
        "Mountain card, reveal it, put it into your hand, then shuffle.)"

    keywords(Keyword.MENACE)

    triggeredAbility {
        trigger = Triggers.Dies
        effect = ForEachPlayerEffect(
            players = Player.Each,
            effects = listOf(
                MayEffect(
                    decisionMaker = EffectTarget.Controller,
                    effect = Effects.Composite(
                        Patterns.Hand.discardHand(EffectTarget.Controller),
                        Effects.DrawCards(7),
                    ),
                ),
            ),
        )
    }

    keywordAbility(KeywordAbility.typecycling("Mountain", ManaCost.parse("{2}")))

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "57"
        artist = "Hector Ortiz"
        imageUri = "https://cards.scryfall.io/normal/front/5/6/56dd8c18-052b-4684-8ffa-f7d49af25759.jpg?1783927591"
    }
}
