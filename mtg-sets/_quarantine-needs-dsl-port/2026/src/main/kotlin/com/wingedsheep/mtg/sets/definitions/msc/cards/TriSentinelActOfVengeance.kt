package com.wingedsheep.mtg.sets.definitions.msc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetCreature

/**
 * Tri-Sentinel, Act of Vengeance
 * {7}
 * Legendary Artifact Creature — Robot Villain
 * 7/7
 *
 * Flying, menace, trample
 * When Tri-Sentinel enters, for each opponent, Tri-Sentinel deals 3 damage to up to one target
 * creature that player controls.
 * Unearth {7} ({7}: Return this card from your graveyard to the battlefield. It gains haste.
 * Exile it at the beginning of the next end step or if it would leave the battlefield. Unearth
 * only as a sorcery.)
 *
 * "For each opponent, … up to one target … that player controls" follows the corpus convention
 * for this wording (Riptide Gearhulk, Blatant Thievery, Omega, Heartless Evolution): one
 * *optional* target scoped by an opponent-controls filter — exactly right in 1v1, the shape the
 * multiplayer per-opponent targeting work generalizes.
 *
 * TODO: **Unearth is not implemented.** No card in this corpus has needed the mechanic before
 * (CR 702.108: an activated ability, sorcery-speed-only, from the graveyard, that returns the
 * card to the battlefield with haste and a replacement effect exiling it at the next end step or
 * whenever it would otherwise leave the battlefield). Building it correctly needs a new
 * graveyard-activated-ability primitive plus the "exile instead of leaving" replacement wired to
 * it specifically (not the same shape as Warp's `WarpExileEffect`, which returns to hand rather
 * than exiling permanently, and is cast-from-hand rather than graveyard-activated). The printed
 * line is preserved verbatim in `oracleText`; only the ability itself is left unimplemented
 * pending that mechanic's addition.
 */
val TriSentinelActOfVengeance = card("Tri-Sentinel, Act of Vengeance") {
    manaCost = "{7}"
    colorIdentity = ""
    typeLine = "Legendary Artifact Creature — Robot Villain"
    power = 7
    toughness = 7
    oracleText = "Flying, menace, trample\n" +
        "When Tri-Sentinel enters, for each opponent, Tri-Sentinel deals 3 damage to up to one " +
        "target creature that player controls.\n" +
        "Unearth {7} ({7}: Return this card from your graveyard to the battlefield. It gains " +
        "haste. Exile it at the beginning of the next end step or if it would leave the " +
        "battlefield. Unearth only as a sorcery.)"

    keywords(Keyword.FLYING, Keyword.MENACE, Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        val victim = target(
            "up to one target creature that player controls",
            TargetCreature(optional = true, filter = TargetFilter.CreatureOpponentControls)
        )
        effect = Effects.DealDamage(3, victim)
        description = "When Tri-Sentinel enters, for each opponent, Tri-Sentinel deals 3 damage " +
            "to up to one target creature that player controls."
    }

    // TODO: Unearth {7} — see the class-doc note above. Not implemented; oracle text preserved.

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "115"
        artist = "Nathaniel Himawan"
        imageUri = "https://cards.scryfall.io/normal/front/a/4/a4960ab8-fac3-4a11-a3f6-46527cf228ea.jpg?1783903254"
    }
}
