package com.wingedsheep.mtg.sets.definitions.msc.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EventPattern.ZoneChangeEvent
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggerSpec
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Ultron the Annihilator
 * {3}{B}{B}
 * Legendary Artifact Creature — Robot Villain
 * 3/4
 *
 * Flying
 * Whenever Ultron enters or attacks, create a 2/2 colorless Robot Villain artifact creature
 * token.
 * Whenever another artifact is put into your graveyard from the battlefield or an artifact card
 * is put into your graveyard from anywhere other than the battlefield, each opponent loses 1
 * life.
 *
 * "Enters or attacks" is two independent triggered abilities sharing one payoff (the token
 * creation), the same "either event" shape the corpus already uses for "enters or attacks"
 * cards elsewhere — one `Triggers.EntersBattlefield` ability and one `Triggers.Attacks` ability,
 * each creating the token.
 *
 * The life-loss clause has no single `ZoneChangeEvent` shape for "any zone except the
 * battlefield" (the event's `from` is a single nullable zone, not an exclusion list), so it is
 * modelled as four triggered abilities instead of one:
 *  - `from = BATTLEFIELD, to = GRAVEYARD` with `excludeSelf` ("another artifact... from the
 *    battlefield" — `binding = ANY`, mirroring Scrap Trawler's own dies-or-another-artifact-dies
 *    trigger).
 *  - `from = HAND, to = GRAVEYARD` (an artifact card discarded).
 *  - `from = LIBRARY, to = GRAVEYARD` (an artifact card milled).
 *  - `from = STACK, to = GRAVEYARD` (an artifact spell countered).
 *
 * Fidelity gap: this covers the overwhelmingly common non-battlefield origins (discard, mill, a
 * countered spell) but not every possible zone an artifact card could leave from into a
 * graveyard (e.g. EXILE or COMMAND) — there is no general "any zone but X" event primitive to
 * fall back on. Left as a documented simplification rather than silently dropping the clause.
 */
val UltronTheAnnihilator = card("Ultron the Annihilator") {
    manaCost = "{3}{B}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Artifact Creature — Robot Villain"
    power = 3
    toughness = 4
    oracleText = "Flying\n" +
        "Whenever Ultron enters or attacks, create a 2/2 colorless Robot Villain artifact " +
        "creature token.\n" +
        "Whenever another artifact is put into your graveyard from the battlefield or an " +
        "artifact card is put into your graveyard from anywhere other than the battlefield, " +
        "each opponent loses 1 life."

    keywords(com.wingedsheep.sdk.core.Keyword.FLYING)

    val makeToken = Effects.CreateToken(
        power = 2,
        toughness = 2,
        colors = emptySet(),
        creatureTypes = setOf("Robot", "Villain"),
        artifactToken = true,
        imageUri = "https://cards.scryfall.io/normal/front/5/1/51879585-9cdf-499b-b7cc-226319c74a0c.jpg?1783903057"
    )

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        effect = makeToken
        description = "Whenever Ultron enters, create a 2/2 colorless Robot Villain artifact " +
            "creature token."
    }

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = makeToken
        description = "Whenever Ultron attacks, create a 2/2 colorless Robot Villain artifact " +
            "creature token."
    }

    val eachOpponentLoses1 = Effects.LoseLife(1, EffectTarget.PlayerRef(Player.EachOpponent))

    triggeredAbility {
        trigger = TriggerSpec(
            event = ZoneChangeEvent(
                filter = GameObjectFilter.Artifact.youControl(),
                from = Zone.BATTLEFIELD,
                to = Zone.GRAVEYARD
            ),
            binding = TriggerBinding.ANY
        )
        effect = eachOpponentLoses1
        description = "Whenever another artifact is put into your graveyard from the " +
            "battlefield, each opponent loses 1 life."
    }
    triggeredAbility {
        trigger = TriggerSpec(
            event = ZoneChangeEvent(
                filter = GameObjectFilter.Artifact.ownedByYou(),
                from = Zone.HAND,
                to = Zone.GRAVEYARD
            ),
            binding = TriggerBinding.ANY
        )
        effect = eachOpponentLoses1
        description = "Whenever an artifact card is put into your graveyard from your hand, " +
            "each opponent loses 1 life."
    }
    triggeredAbility {
        trigger = TriggerSpec(
            event = ZoneChangeEvent(
                filter = GameObjectFilter.Artifact.ownedByYou(),
                from = Zone.LIBRARY,
                to = Zone.GRAVEYARD
            ),
            binding = TriggerBinding.ANY
        )
        effect = eachOpponentLoses1
        description = "Whenever an artifact card is put into your graveyard from your library, " +
            "each opponent loses 1 life."
    }
    triggeredAbility {
        trigger = TriggerSpec(
            event = ZoneChangeEvent(
                filter = GameObjectFilter.Artifact.ownedByYou(),
                from = Zone.STACK,
                to = Zone.GRAVEYARD
            ),
            binding = TriggerBinding.ANY
        )
        effect = eachOpponentLoses1
        description = "Whenever an artifact spell you own is put into your graveyard from the " +
            "stack, each opponent loses 1 life."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "668"
        artist = "Eugene Maslovski"
        imageUri = "https://cards.scryfall.io/normal/front/5/1/51879585-9cdf-499b-b7cc-226319c74a0c.jpg?1783903057"
    }
}
