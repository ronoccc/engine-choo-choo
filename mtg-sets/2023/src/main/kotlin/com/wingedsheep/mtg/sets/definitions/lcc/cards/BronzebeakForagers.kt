package com.wingedsheep.mtg.sets.definitions.lcc.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.CollectionFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.TargetObject
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Bronzebeak Foragers
 * {3}{W}
 * Creature — Dinosaur
 * When this creature enters, for each opponent, exile up to one target nonland permanent that
 * player controls until this creature leaves the battlefield.
 * {X}{W}: Put target card with mana value X exiled with this creature into its owner's
 * graveyard. You gain X life.
 *
 * The ETB is the Kaya, Spirits' Justice "-2" shape: [TargetPermanent] with
 * `dynamicMaxCount = DynamicAmount.PlayerCount(Player.EachOpponent)`, `optional = true`, and
 * `differentControllers = true` distributes one optional exile target per opponent, fed into
 * [Effects.ExileUntilLeaves] + a leaves-trigger [Effects.ReturnLinkedExileUnderOwnersControl].
 *
 * The activated ability has no true spell-target primitive for "a card among several linked-exile
 * cards" (unlike a cast-time target, there's no engine support for targeting into a linked-exile
 * pile by a runtime filter), so it is modeled as a pipeline choice instead:
 * [CardSource.FromLinkedExile] gathers the whole pile, [CollectionFilter.ManaValueEquals] narrows
 * it to mana value X, and the controller picks one via `chooseExactly`. This is a deliberate
 * approximation — a real "target card exiled with this" would be visible to hexproof/Stifle-style
 * interaction that a pipeline choice isn't — accepted here since this is local, non-tournament
 * software and every other primitive (linked exile, mana-value-X filtering, per-owner graveyard
 * routing via [Player.OwnersOfLinkedExile]) already exists and composes correctly.
 */
val BronzebeakForagers = card("Bronzebeak Foragers") {
    manaCost = "{3}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Dinosaur"
    power = 3
    toughness = 4
    oracleText = "When this creature enters, for each opponent, exile up to one target nonland " +
        "permanent that player controls until this creature leaves the battlefield.\n" +
        "{X}{W}: Put target card with mana value X exiled with this creature into its owner's " +
        "graveyard. You gain X life."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val exiled = target(
            "up to one target nonland permanent that player controls",
            TargetObject(
                filter = TargetFilter.NonlandPermanentOpponentControls,
                optional = true,
                dynamicMaxCount = DynamicAmount.PlayerCount(Player.EachOpponent),
                differentControllers = true,
            ),
        )
        effect = Effects.ExileUntilLeaves(exiled)
    }

    triggeredAbility {
        trigger = Triggers.LeavesBattlefield
        effect = Effects.ReturnLinkedExileUnderOwnersControl()
    }

    activatedAbility {
        cost = Costs.Mana("{X}{W}")
        effect = Effects.Pipeline {
            val exiledPile = gather(CardSource.FromLinkedExile())
            val matchingManaValue = filter(
                exiledPile,
                CollectionFilter.ManaValueEquals(DynamicAmount.XValue),
                name = "matchingManaValue",
            )
            val chosen = chooseExactly(
                1,
                from = matchingManaValue,
                prompt = "Put a card with mana value X exiled with this creature into its owner's graveyard",
                selectedLabel = "Put into graveyard",
                name = "bronzebeakGraveyard",
            )
            move(
                chosen,
                CardDestination.ToZone(Zone.GRAVEYARD, Player.OwnersOfLinkedExile),
            )
            run(Effects.GainLife(DynamicAmount.XValue))
        }
        description = "Put target card with mana value X exiled with this creature into its " +
            "owner's graveyard. You gain X life."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "69"
        artist = "Brian Valeza"
        imageUri = "https://cards.scryfall.io/normal/front/d/a/dadfcd91-3000-448e-a304-be425ff68644.jpg?1783913914"
    }
}
