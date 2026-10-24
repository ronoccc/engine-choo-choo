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
        trigger = Triggers.self.enters()
        effect = Patterns.Library.mill(4)
        description = "When Emry enters, mill four cards."
    }

    activatedAbility {
        cost = Costs.Tap
        val chosen = target(TargetFilter.ArtifactInYourGraveyard)
        effect = GrantStaticAbilityEffect(
            ability = MayCastFromGraveyard(filter = GameObjectFilter.Any),
            target = chosen,
            duration = Duration.EndOfTurn,
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
