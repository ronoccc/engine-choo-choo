package com.wingedsheep.mtg.sets.definitions.acr.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.predicates.CardPredicate
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Desynchronization
 * {2}{U}{U}
 * Instant
 *
 * Return each nonland permanent that's not historic to its owner's hand. (Artifacts,
 * legendaries, and Sagas are historic.)
 *
 * "Not historic" is the negation of the same union [GameObjectFilter.Historic] already models
 * (artifact, legendary, or Saga), ANDed with [GameObjectFilter.NonlandPermanent] so lands are
 * untouched. No target — this is a symmetrical board-wide bounce, so it is modelled as
 * `ForEachInGroup` returning every matching permanent (both players' included) to its owner's
 * hand, mirroring how `Cleansing` iterates `ForEachInGroup(GroupFilter.AllLands, ...)`.
 */
val Desynchronization = card("Desynchronization") {
    manaCost = "{2}{U}{U}"
    colorIdentity = "U"
    typeLine = "Instant"
    oracleText = "Return each nonland permanent that's not historic to its owner's hand. " +
        "(Artifacts, legendaries, and Sagas are historic.)"

    val nonHistoricNonland = GameObjectFilter.NonlandPermanent and GameObjectFilter(
        cardPredicates = listOf(
            CardPredicate.Not(
                CardPredicate.Or(
                    listOf(
                        CardPredicate.IsArtifact,
                        CardPredicate.IsLegendary,
                        CardPredicate.HasSubtype(Subtype("Saga")),
                    )
                )
            )
        )
    )

    spell {
        effect = Effects.ForEachInGroup(
            GroupFilter(nonHistoricNonland),
            Effects.ReturnToHand(EffectTarget.Self),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "16"
        artist = "Lie Setiawan"
        flavorText = "History cannot be changed, and the Animus is not kind to those who try."
        imageUri = "https://cards.scryfall.io/normal/front/3/d/3ddd3561-0143-40b7-81bc-640a978b8b15.jpg?1783910985"
    }
}
