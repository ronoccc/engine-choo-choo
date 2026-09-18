package com.wingedsheep.mtg.sets.definitions.lcc.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.ContextPropertyKey
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Elenda's Hierophant
 * {2}{W}
 * Creature — Vampire Cleric
 * 1/1
 * Flying
 * Whenever you gain life, put a +1/+1 counter on this creature.
 * When this creature dies, create X 1/1 white Vampire creature tokens with lifelink, where X is
 * its power.
 *
 * "Its power" at death is base power (1) plus the +1/+1 counters it carried — the only way this
 * card's own text ever changes its power — read via the Hooded Hydra
 * [ContextPropertyKey.LAST_KNOWN_PLUS_ONE_COUNTER_COUNT] idiom rather than a live board read, since
 * the creature is already in the graveyard when the trigger resolves.
 */
val ElendasHierophant = card("Elenda's Hierophant") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Creature — Vampire Cleric"
    power = 1
    toughness = 1
    oracleText = "Flying\n" +
        "Whenever you gain life, put a +1/+1 counter on this creature.\n" +
        "When this creature dies, create X 1/1 white Vampire creature tokens with lifelink, where " +
        "X is its power."

    keywords(Keyword.FLYING)

    triggeredAbility {
        trigger = Triggers.YouGainLife
        effect = Effects.AddCounters(Counters.PLUS_ONE_PLUS_ONE, 1, EffectTarget.Self)
    }

    triggeredAbility {
        trigger = Triggers.Dies
        effect = CreateTokenEffect(
            count = DynamicAmount.Add(
                DynamicAmount.Fixed(1),
                DynamicAmount.ContextProperty(ContextPropertyKey.LAST_KNOWN_PLUS_ONE_COUNTER_COUNT)
            ),
            power = 1,
            toughness = 1,
            colors = setOf(Color.WHITE),
            creatureTypes = setOf("Vampire"),
            keywords = setOf(Keyword.LIFELINK)
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "39"
        artist = "Miranda Meeks"
        imageUri = "https://cards.scryfall.io/normal/front/d/1/d12f728a-1bbb-4bee-96d4-d721292997ba.jpg?1783913913"
    }
}
