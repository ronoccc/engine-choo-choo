package com.wingedsheep.sdk.model

import com.wingedsheep.sdk.core.Subtype
import kotlinx.serialization.Serializable

/**
 * A companion's deckbuilding-time restriction (CR 702.139a / 103.2b): the condition a player's
 * *starting deck* must fulfil before they may reveal this card as their companion pregame.
 *
 * Pure data — evaluated by [CompanionRestrictionEvaluator] against a finished decklist's
 * [CardDefinition]s, never against live `GameState`. This mirrors how [Condition]
 * ([com.wingedsheep.sdk.scripting.conditions.Condition]) stays a data object with evaluation
 * split out: a companion's restriction needs no game state at all (CR 702.139b — "starting deck"
 * is fixed before the game begins), so its evaluator lives here in the SDK rather than in
 * `rules-engine`.
 *
 * Every real companion has a different shape of restriction (creature types, mana-value parity,
 * minimum deck size, no two cards sharing a name, ...). Each subtype below models one shape, so a
 * future companion adds a case here instead of a bespoke hardcoded name check in the validator.
 * Only the shape Kaheera, the Orphanguard actually uses is implemented; add siblings as new
 * companions need them.
 */
@Serializable
sealed interface CompanionRestriction {
    /** Human-readable statement of the restriction, for deck-validation error messages. */
    val description: String
}

/**
 * "Each creature card in your starting deck is a [subtype], [subtype], ... card." — Kaheera, the
 * Orphanguard's restriction. Noncreature cards are unrestricted (per Scryfall's 2020-04-17 ruling:
 * "Noncreature cards may have any subtypes"), and a creature card may carry additional subtypes
 * beyond the listed ones as long as at least one matches.
 */
@Serializable
data class EveryCreatureCardHasSubtype(
    val allowedSubtypes: Set<Subtype>,
) : CompanionRestriction {
    override val description: String
        get() = "Each creature card in your starting deck is a " +
            allowedSubtypes.joinToString(", ") { it.value } + " card."
}

/**
 * A companion keyword ability (CR 702.139a): pairs the deckbuilding [restriction] with the
 * reminder/oracle text printed on the card. Carried on [CardDefinition.companion] — null for
 * every card without the ability.
 */
@Serializable
data class CompanionAbility(
    val restriction: CompanionRestriction,
)

/**
 * Evaluates a [CompanionRestriction] against a finished starting deck — pure data in, pure
 * boolean out, no engine or game-state dependency (CR 702.139b: the check is against the
 * *starting deck*, fixed at deck-construction time, not against anything that can change during
 * a game).
 *
 * Callers decide what "starting deck" means for their format: [com.wingedsheep.gameserver.deck.DeckValidator]
 * passes the constructed library (plus the designated commander in Commander, per CR 702.139b's
 * "before you've set aside your commander"); `GameInitializer` passes the same set of cards it is
 * about to shuffle into the library.
 */
object CompanionRestrictionEvaluator {
    fun isSatisfiedBy(restriction: CompanionRestriction, startingDeck: List<CardDefinition>): Boolean =
        when (restriction) {
            is EveryCreatureCardHasSubtype -> startingDeck
                .filter { it.isCreature }
                .all { card -> card.typeLine.subtypes.any { it in restriction.allowedSubtypes } }
        }
}
