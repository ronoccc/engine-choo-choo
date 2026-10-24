package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.EffectChoice
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Great Intelligence's Plan
 * {4}{U}{B}
 * Sorcery
 *
 * Draw three cards. Then target opponent faces a villainous choice — They discard three cards, or
 * you may cast a spell from your hand without paying its mana cost.
 */
val GreatIntelligencesPlan = card("Great Intelligence's Plan") {
    manaCost = "{4}{U}{B}"
    colorIdentity = "UB"
    typeLine = "Sorcery"
    oracleText = "Draw three cards. Then target opponent faces a villainous choice — They discard three " +
        "cards, or you may cast a spell from your hand without paying its mana cost."

    spell {
        val opponent = target(Targets.Opponent)

        effect = Effects.Composite(
            listOf(
                Effects.DrawCards(3),
                Effects.ChooseAction(
                    choices = listOf(
                        EffectChoice(
                            label = "They discard three cards",
                            effect = Effects.Discard(3, opponent)
                        ),
                        EffectChoice(
                            label = "You may cast a spell from your hand without paying its mana cost",
                            effect = Effects.Composite(
                                listOf(
                                    GatherCardsEffect(CardSource.FromZone(Zone.HAND, Player.You), "greatIntelligencesPlan_hand"),
                                    SelectFromCollectionEffect(
                                        from = "greatIntelligencesPlan_hand",
                                        selection = SelectionMode.ChooseSpell,
                                        filter = GameObjectFilter.Nonland,
                                        storeSelected = "greatIntelligencesPlan_spellToCast",
                                        showAllCards = true,
                                        prompt = "You may cast a spell from your hand without paying its mana cost",
                                        selectedLabel = "Cast for free"
                                    ),
                                    Effects.CastFromCollectionWithoutPayingCost("greatIntelligencesPlan_spellToCast"),
                                )
                            )
                        )
                    ),
                    player = opponent
                )
            )
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "133"
        artist = "Piotr Dura"
        flavorText = "\"The great swarm is approaching. As humanity celebrates, so shall it end.\"\n—The Great Intelligence"
        imageUri = "https://cards.scryfall.io/normal/front/a/7/a798feb4-561b-4bb9-bdc9-771325faec57.jpg?1783914633"
    }
}
