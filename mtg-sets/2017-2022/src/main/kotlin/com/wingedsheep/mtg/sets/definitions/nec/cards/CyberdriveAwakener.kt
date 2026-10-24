package com.wingedsheep.mtg.sets.definitions.nec.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.values.DynamicAmount

val CyberdriveAwakener = card("Cyberdrive Awakener") {
    manaCost = "{5}{U}"
    colorIdentity = "U"
    typeLine = "Artifact Creature — Construct"
    power = 4
    toughness = 4
    oracleText = "Flying\n" +
        "Other artifact creatures you control have flying.\n" +
        "When this creature enters, each noncreature artifact you control becomes a 4/4 artifact " +
        "creature until end of turn."

    keywords(Keyword.FLYING)

    staticAbility {
        ability = GrantKeyword(
            Keyword.FLYING,
            GroupFilter(GameObjectFilter.ArtifactCreature.youControl().notSourceItself()),
        )
    }

    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = Effects.MassAnimate(
            filter = GameObjectFilter.Artifact.notCreature().youControl(),
            power = DynamicAmount.Fixed(4),
            toughness = DynamicAmount.Fixed(4),
            loseAllAbilities = false,
            duration = Duration.EndOfTurn,
        )
        description = "When this creature enters, each noncreature artifact you control becomes " +
            "a 4/4 artifact creature until end of turn."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "12"
        artist = "Zezhou Chen"
        imageUri = "https://cards.scryfall.io/normal/front/2/a/2a2c0a2f-07fb-4cc0-9b99-e5ff4303028d.jpg?1783923995"
    }
}
