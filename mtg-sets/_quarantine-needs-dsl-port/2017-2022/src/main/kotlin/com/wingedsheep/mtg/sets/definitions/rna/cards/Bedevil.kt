package com.wingedsheep.mtg.sets.definitions.rna.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Bedevil — Ravnica Allegiance #157
 * {B}{B}{R} · Instant
 *
 * A single target drawn from the "flexible removal" family (artifact, creature, or
 * planeswalker) — see `Targets.ArtifactCreatureOrPlaneswalker`.
 */
val Bedevil = card("Bedevil") {
    manaCost = "{B}{B}{R}"
    colorIdentity = "BR"
    typeLine = "Instant"
    oracleText = "Destroy target artifact, creature, or planeswalker."

    spell {
        val victim = target("target", Targets.ArtifactCreatureOrPlaneswalker)
        effect = Effects.Destroy(victim)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "157"
        artist = "Seb McKinnon"
        flavorText = "\"It's easy to get taken in by the spectacle, to enjoy a bit of naughty amusement. " +
            "But make no mistake: the Cult of Rakdos is a danger.\"\n" +
            "—Tajic"
        imageUri = "https://cards.scryfall.io/normal/front/8/1/81e2b96b-ecf2-4dd9-bc9d-3c46ee8c59e6.jpg?1783933657"
    }
}
