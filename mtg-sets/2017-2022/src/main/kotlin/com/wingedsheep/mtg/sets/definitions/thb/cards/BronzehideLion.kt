// Missing SDK primitives: becomesAuraOnAttach, SourceReturnedAsAura (dies, return as Aura with granted ability).
package com.wingedsheep.mtg.sets.definitions.thb.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val BronzehideLion = card("Bronzehide Lion") {
    manaCost = "{G}{W}"
    colorIdentity = "GW"
    typeLine = "Creature — Cat"
    power = 3
    toughness = 3
    oracleText = "{G}{W}: This creature gains indestructible until end of turn.\n" +
        "When this creature dies, return it to the battlefield. It's an Aura enchantment with " +
        "enchant creature you control and \"{G}{W}: Enchanted creature gains indestructible " +
        "until end of turn,\" and it loses all other abilities."

    activatedAbility {
        cost = Costs.Mana("{G}{W}")
        effect = Effects.GrantKeyword(Keyword.INDESTRUCTIBLE, EffectTarget.Self)
    }

    triggeredAbility {
        trigger = Triggers.self.dies()
        effect = Effects.PutOntoBattlefieldAttachedToChosen(
            target = EffectTarget.Self,
            hostFilter = GameObjectFilter.Creature.youControl(),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "210"
        artist = "Alayna Danner"
        imageUri = "https://cards.scryfall.io/normal/front/8/f/8fdafadb-fa04-4282-b576-b85e79c242c9.jpg?1783931524"
        ruling(
            "2020-01-24",
            "If you control but don't own Bronzehide Lion, you'll return it to the battlefield " +
                "when it dies, not its owner. It'll enchant a creature you control.",
        )
        ruling(
            "2020-01-24",
            "You choose a creature you control for Bronzehide Lion to enchant as it returns to " +
                "the battlefield. If there's nothing it can legally enchant, it remains in its " +
                "owner's graveyard.",
        )
        ruling(
            "2020-01-24",
            "If a token is a copy of Bronzehide Lion, it won't return from its owner's graveyard.",
        )
    }
}
