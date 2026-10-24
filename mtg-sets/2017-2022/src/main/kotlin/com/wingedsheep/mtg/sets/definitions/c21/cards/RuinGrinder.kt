package com.wingedsheep.mtg.sets.definitions.c21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.KeywordAbility
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val RuinGrinder = card("Ruin Grinder") {
    manaCost = "{5}{R}"
    colorIdentity = "R"
    typeLine = "Artifact Creature — Construct"
    power = 7
    toughness = 4
    oracleText = "Menace\nWhen this creature dies, each player may discard their hand and draw " +
        "seven cards.\nMountaincycling {2} ({2}, Discard this card: Search your library for a " +
        "Mountain card, reveal it, put it into your hand, then shuffle.)"

    keywords(Keyword.MENACE)

    triggeredAbility {
        trigger = Triggers.self.dies()
        effect = Effects.ForEachPlayer(
            players = Player.Each,
            effect = Effects.May(
                effect = Patterns.Hand.discardHand(EffectTarget.Controller) then Effects.DrawCards(7),
            ),
        )
    }

    keywordAbility(KeywordAbility.typecycling("Mountain", ManaCost.parse("{2}")))

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "57"
        artist = "Hector Ortiz"
        imageUri = "https://cards.scryfall.io/normal/front/5/6/56dd8c18-052b-4684-8ffa-f7d49af25759.jpg?1783927591"
    }
}
