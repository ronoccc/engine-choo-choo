package com.wingedsheep.mtg.sets.definitions.nph.cards

import com.wingedsheep.sdk.core.AbilityFlag
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AdditionalManaOnSourceTap
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.effects.GrantKeywordEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Vorinclex, Voice of Hunger
 * {6}{G}{G}
 * Legendary Creature — Phyrexian Praetor
 * 7/6
 * Trample
 * Whenever you tap a land for mana, add one mana of any type that land produced.
 * Whenever an opponent taps a land for mana, that land doesn't untap during its controller's next
 * untap step.
 *
 * The mana-doubling first ability is the same primitive Lavaleaper already established:
 * [AdditionalManaOnSourceTap] with `color = null` ("mirror the produced color").
 */
val VorinclexVoiceOfHunger = card("Vorinclex, Voice of Hunger") {
    manaCost = "{6}{G}{G}"
    colorIdentity = "G"
    typeLine = "Legendary Creature — Phyrexian Praetor"
    power = 7
    toughness = 6
    oracleText = "Trample\n" +
        "Whenever you tap a land for mana, add one mana of any type that land produced.\n" +
        "Whenever an opponent taps a land for mana, that land doesn't untap during its " +
        "controller's next untap step."

    keywords(Keyword.TRAMPLE)

    staticAbility {
        ability = AdditionalManaOnSourceTap(
            sourceFilter = GameObjectFilter.Land.youControl(),
            color = null,
        )
    }

    triggeredAbility {
        trigger = Triggers.landTappedForMana(player = Player.EachOpponent, binding = TriggerBinding.ANY)
        effect = GrantKeywordEffect(
            AbilityFlag.DOESNT_UNTAP.name,
            EffectTarget.TriggeringEntity,
            Duration.UntilAfterAffectedControllersNextUntap,
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "127"
        artist = "Karl Kopinski"
        imageUri = "https://cards.scryfall.io/normal/front/0/8/0806adab-6a08-411b-b249-e1c58ade354b.jpg?1783941297"
    }
}
