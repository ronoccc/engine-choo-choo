package com.wingedsheep.mtg.sets.definitions.eld.cards

import com.wingedsheep.sdk.core.CardType
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.MayCastFromGraveyard
import com.wingedsheep.sdk.scripting.effects.GrantStaticAbilityEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Emry, Lurker of the Loch
 * {1}{U}
 * Legendary Creature — Merfolk Wizard
 * 1/2
 *
 * Affinity for artifacts (This spell costs {1} less to cast for each artifact you control.)
 * When Emry enters, mill four cards.
 * {T}: Choose target artifact card in your graveyard. You may cast that card this turn.
 *
 * The tap ability's grant is anchored to the chosen graveyard *card*, not to Emry — the
 * "one card's permission" shape from the language reference: `GrantStaticAbility(
 * MayCastFromGraveyard(GameObjectFilter.Any), target, Duration.EndOfTurn)`. Because the ability
 * doesn't say "without paying its mana cost," the card is still cast paying its normal cost.
 */
val EmryLurkerOfTheLoch = card("Emry, Lurker of the Loch") {
    manaCost = "{1}{U}"
    colorIdentity = "U"
    typeLine = "Legendary Creature — Merfolk Wizard"
    power = 1
    toughness = 2
    oracleText = "Affinity for artifacts (This spell costs {1} less to cast for each artifact you control.)\n" +
        "When Emry enters, mill four cards.\n" +
        "{T}: Choose target artifact card in your graveyard. You may cast that card this turn."

    keywordAbility(KeywordAbility.Affinity(CardType.ARTIFACT))

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        effect = Patterns.Library.mill(4)
        description = "When Emry enters, mill four cards."
    }

    activatedAbility {
        cost = Costs.Tap
        val chosen = target("target artifact card in your graveyard", TargetObject(filter = TargetFilter.ArtifactInYourGraveyard))
        effect = GrantStaticAbilityEffect(
            ability = MayCastFromGraveyard(filter = GameObjectFilter.Any),
            target = chosen,
            duration = Duration.EndOfTurn
        )
        description = "Choose target artifact card in your graveyard. You may cast that card this turn."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "43"
        artist = "Livia Prima"
        imageUri = "https://cards.scryfall.io/normal/front/b/f/bf4b9a8a-b42a-46fb-b0d0-9cf800f63c8a.jpg?1783932660"
    }
}
