package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.effects.CardDestination
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.FaceDownMode
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.MoveCollectionEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Cybership
 * {6}
 * Artifact — Vehicle
 * 8/8
 *
 * Flying
 * Whenever this Vehicle deals combat damage to a player, put the top two cards of that player's
 * library onto the battlefield face down under your control. They're 2/2 Cyberman artifact
 * creatures.
 * Crew 4
 *
 * Modelled on [com.wingedsheep.sdk.dsl.LibraryPatterns.manifest]'s Gather(top N of a library) →
 * Move(battlefield, face down) shape, but reading from [Player.TriggeringPlayer]'s library (the
 * damaged player, "that player") instead of the caster's own, landing under the ability's
 * controller (the default for [CardDestination.ToZone]'s `player`, which is the battlefield
 * *controller* for that destination).
 *
 * Fidelity gap: reuses [FaceDownMode.MANIFEST] as the closest existing face-down-2/2-from-library
 * primitive. Two divergences from the printed card: (1) the engine's manifest lets a manifested
 * *creature* card be turned face up later by paying its mana cost (CR 701.40b) — Cybership's
 * oracle text grants no such option; (2) the manifested permanents are blank 2/2s (CR 701.40a),
 * not typed "Cyberman artifact creature" as printed. Both stats (2/2) already match by
 * coincidence. No card-specific engine code was added to special-case this; documented here as a
 * TODO rather than silently dropped.
 */
val Cybership = card("Cybership") {
    manaCost = "{6}"
    colorIdentity = ""
    typeLine = "Artifact — Vehicle"
    power = 8
    toughness = 8
    oracleText = "Flying\n" +
        "Whenever this Vehicle deals combat damage to a player, put the top two cards of that " +
        "player's library onto the battlefield face down under your control. They're 2/2 " +
        "Cyberman artifact creatures.\n" +
        "Crew 4"

    keywords(Keyword.FLYING)
    keywordAbility(KeywordAbility.crew(4))

    // TODO: see the fidelity-gap note above — manifest is reused as the closest available
    // face-down-from-library primitive; the printed "Cyberman artifact creature" typing and the
    // absence of a turn-face-up option are not modelled.
    triggeredAbility {
        trigger = Triggers.DealsCombatDamageToPlayer
        effect = Effects.Composite(
            listOf(
                GatherCardsEffect(
                    source = CardSource.TopOfLibrary(DynamicAmount.Fixed(2), player = Player.TriggeringPlayer),
                    storeAs = "cybership_manifested",
                    revealed = false
                ),
                MoveCollectionEffect(
                    from = "cybership_manifested",
                    destination = CardDestination.ToZone(Zone.BATTLEFIELD),
                    faceDown = FaceDownMode.MANIFEST
                )
            )
        )
        description = "Whenever this Vehicle deals combat damage to a player, put the top two " +
            "cards of that player's library onto the battlefield face down under your control. " +
            "They're 2/2 Cyberman artifact creatures."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "177"
        artist = "JB Casacop"
        imageUri = "https://cards.scryfall.io/normal/front/b/0/b0b9d006-7179-4d59-88ef-34b4d4b2e2fc.jpg?1783914616"
    }
}
