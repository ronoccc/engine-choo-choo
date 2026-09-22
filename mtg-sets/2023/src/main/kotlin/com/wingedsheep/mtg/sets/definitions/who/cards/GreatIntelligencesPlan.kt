package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Great Intelligence's Plan
 * {4}{U}{B}
 * Sorcery
 *
 * Draw three cards. Then target opponent faces a villainous choice — They discard three cards, or
 * you may cast a spell from your hand without paying its mana cost.
 *
 * The Doctor Who "villainous choice" mechanic, [Effects.VillainousChoice] with
 * `Chooser.TargetPlayer` — the *targeted opponent* decides, not the caster (unlike a CR 700.2
 * modal spell). The two options are performed by two different players: "They discard" targets
 * the chosen opponent (`EffectTarget.ContextTarget(0)`, the spell's own target), while "you may
 * cast a spell" is the original caster — `Effects.CastFromCollectionWithoutPayingCost` defaults
 * its `caster` to `Chooser.Controller`, and `VillainousChoice` never rebinds the resolving
 * context to the chooser, so both readings land on the right player without any special-casing.
 * Modelled on Twinning Glass's Gather(hand) → Select(spell) → Cast-without-paying pipeline.
 */
val GreatIntelligencesPlan = card("Great Intelligence's Plan") {
    manaCost = "{4}{U}{B}"
    colorIdentity = "UB"
    typeLine = "Sorcery"
    oracleText = "Draw three cards. Then target opponent faces a villainous choice — They discard three " +
        "cards, or you may cast a spell from your hand without paying its mana cost."

    spell {
        val opponent = target("opponent", Targets.Opponent)

        effect = Effects.Composite(
            listOf(
                Effects.DrawCards(3),
                Effects.VillainousChoice(
                    chooser = Chooser.TargetPlayer,
                    Mode.noTarget(
                        Effects.Discard(3, opponent),
                        "They discard three cards"
                    ),
                    Mode.noTarget(
                        Effects.Composite(
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
                        ),
                        "You may cast a spell from your hand without paying its mana cost"
                    ),
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
