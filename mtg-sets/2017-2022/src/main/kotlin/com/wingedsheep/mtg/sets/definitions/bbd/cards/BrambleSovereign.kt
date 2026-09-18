package com.wingedsheep.mtg.sets.definitions.bbd.cards

import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.effects.MayPayManaEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Bramble Sovereign
 * {2}{G}{G}
 * Creature — Dryad
 * 4/4
 * Whenever another nontoken creature enters, you may pay {1}{G}. If you do, that creature's
 * controller creates a token that's a copy of that creature.
 */
val BrambleSovereign = card("Bramble Sovereign") {
    manaCost = "{2}{G}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Dryad"
    power = 4
    toughness = 4
    oracleText = "Whenever another nontoken creature enters, you may pay {1}{G}. If you do, that " +
        "creature's controller creates a token that's a copy of that creature."

    triggeredAbility {
        trigger = Triggers.entersBattlefield(
            filter = GameObjectFilter.Creature.nontoken(),
            binding = TriggerBinding.OTHER
        )
        effect = MayPayManaEffect(
            cost = ManaCost.parse("{1}{G}"),
            effect = CreateTokenCopyOfTargetEffect(
                target = EffectTarget.TriggeringEntity,
                controller = EffectTarget.ControllerOfTriggeringEntity
            )
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "65"
        artist = "Lucas Graciano"
        flavorText = "\"Within this grove, you belong to me.\""
        imageUri = "https://cards.scryfall.io/normal/front/c/c/cc2d644f-6fb9-406b-ba57-a9b3d1c9646b.jpg?1783934855"
    }
}
