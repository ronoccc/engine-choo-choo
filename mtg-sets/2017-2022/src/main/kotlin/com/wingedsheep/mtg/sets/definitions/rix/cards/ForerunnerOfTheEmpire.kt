package com.wingedsheep.mtg.sets.definitions.rix.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.SearchDestination
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Forerunner of the Empire
 * {3}{R}
 * Creature — Human Soldier
 * When this creature enters, you may search your library for a Dinosaur card, reveal it, then
 * shuffle and put that card on top.
 * Whenever a Dinosaur you control enters, you may have this creature deal 1 damage to each
 * creature.
 *
 * ETB search mirrors Forerunner of the Coalition's exactly (same cycle, [Patterns.Library]'s
 * top-of-library + reveal). The second ability wraps a mass [Effects.DealDamage] to
 * [GroupFilter.AllCreatures] in [MayEffect] for the "you may have this creature deal" wording.
 */
val ForerunnerOfTheEmpire = card("Forerunner of the Empire") {
    manaCost = "{3}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Human Soldier"
    power = 1
    toughness = 3
    oracleText = "When this creature enters, you may search your library for a Dinosaur card, " +
        "reveal it, then shuffle and put that card on top.\n" +
        "Whenever a Dinosaur you control enters, you may have this creature deal 1 damage to " +
        "each creature."

    triggeredAbility {
        trigger = Triggers.self.enters()
        optional = true
        effect = Patterns.Library.searchLibrary(
            filter = GameObjectFilter.Any.withSubtype("Dinosaur"),
            destination = SearchDestination.TOP_OF_LIBRARY,
            reveal = true
        )
    }

    triggeredAbility {
        trigger = Triggers.a(GameObjectFilter.Permanent.withSubtype("Dinosaur").youControl()).enters()
        optional = true
        effect = Effects.DealDamage(
            1,
            EffectTarget.GroupRef(GroupFilter.AllCreatures),
            damageSource = EffectTarget.Self
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "102"
        artist = "Dan Murayama Scott"
        imageUri = "https://cards.scryfall.io/normal/front/9/4/947644ab-02b3-4ebe-b62a-c087ab205ab0.jpg?1783935298"
    }
}
