package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

val RinAndSeriInseparable = card("Rin and Seri, Inseparable") {
    manaCost = "{1}{R}{G}{W}"
    colorIdentity = "RGW"
    typeLine = "Legendary Creature — Dog Cat"
    oracleText = "Whenever you cast a Dog spell, create a 1/1 green Cat creature token.\n" +
        "Whenever you cast a Cat spell, create a 1/1 white Dog creature token.\n" +
        "{R}{G}{W}, {T}: Rin and Seri, Inseparable deals damage to any target equal to the number " +
        "of Dogs you control. You gain life equal to the number of Cats you control."
    power = 4
    toughness = 4

    triggeredAbility {
        trigger = Triggers.you.casts(GameObjectFilter.Any.withSubtype(Subtype.DOG))
        effect = Effects.CreateToken(
            power = 1,
            toughness = 1,
            colors = setOf(Color.GREEN),
            creatureTypes = setOf("Cat"),
        )
        description = "Whenever you cast a Dog spell, create a 1/1 green Cat creature token."
    }

    triggeredAbility {
        trigger = Triggers.you.casts(GameObjectFilter.Any.withSubtype(Subtype.CAT))
        effect = Effects.CreateToken(
            power = 1,
            toughness = 1,
            colors = setOf(Color.WHITE),
            creatureTypes = setOf("Dog"),
        )
        description = "Whenever you cast a Cat spell, create a 1/1 white Dog creature token."
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{R}{G}{W}"), Costs.Tap)
        val t = target(Targets.Any)
        effect = Effects.DealDamage(
            amount = DynamicAmounts.battlefield(
                Player.You,
                GameObjectFilter.Permanent.withSubtype(Subtype.DOG),
            ).count(),
            target = t,
        ) then Effects.GainLife(
            amount = DynamicAmounts.battlefield(
                Player.You,
                GameObjectFilter.Permanent.withSubtype(Subtype.CAT),
            ).count(),
            target = EffectTarget.Controller,
        )
        description = "Rin and Seri, Inseparable deals damage to any target equal to the number of " +
            "Dogs you control. You gain life equal to the number of Cats you control."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "278"
        artist = "Leesha Hannigan"
        imageUri = "https://cards.scryfall.io/normal/front/d/6/d605c780-a42a-4816-8fb9-63e3114a8246.jpg?1784483577"
    }
}
