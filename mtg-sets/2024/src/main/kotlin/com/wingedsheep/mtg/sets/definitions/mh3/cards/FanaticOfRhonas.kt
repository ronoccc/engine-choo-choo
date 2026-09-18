package com.wingedsheep.mtg.sets.definitions.mh3.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.ActivationRestriction
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.costs.CostAtom
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Fanatic of Rhonas
 * {1}{G}
 * Creature — Snake Druid
 * 1/4
 * {T}: Add {G}.
 * Ferocious — {T}: Add {G}{G}{G}{G}. Activate only if you control a creature with power 4 or
 * greater.
 * Eternalize {2}{G}{G} ({2}{G}{G}, Exile this card from your graveyard: Create a token that's a
 * copy of it, except it's a 4/4 black Zombie Snake Druid with no mana cost. Eternalize only as a
 * sorcery.)
 *
 * Eternalize (CR 702.129) needs no engine subsystem of its own, same as embalm — an
 * exile-from-graveyard activated ability whose effect is [CreateTokenCopyOfTargetEffect] with the
 * three printed exceptions (4/4, black, +Zombie, no mana cost). Modeled directly on
 * `com.wingedsheep.sdk.dsl.embalmAbility`, swapping the fixed P/T and color overrides Eternalize
 * prints instead of Embalm's.
 */
val FanaticOfRhonas = card("Fanatic of Rhonas") {
    manaCost = "{1}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Snake Druid"
    power = 1
    toughness = 4
    oracleText = "{T}: Add {G}.\n" +
        "Ferocious — {T}: Add {G}{G}{G}{G}. Activate only if you control a creature with power 4 " +
        "or greater.\n" +
        "Eternalize {2}{G}{G} ({2}{G}{G}, Exile this card from your graveyard: Create a token " +
        "that's a copy of it, except it's a 4/4 black Zombie Snake Druid with no mana cost. " +
        "Eternalize only as a sorcery.)"

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddMana(Color.GREEN)
        manaAbility = true
    }

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddMana(Color.GREEN, 4)
        manaAbility = true
        restrictions = listOf(
            ActivationRestriction.OnlyIfCondition(
                Conditions.YouControlAtLeast(1, GameObjectFilter.Creature.powerAtLeast(4))
            )
        )
        description = "Ferocious — {T}: Add {G}{G}{G}{G}. Activate only if you control a creature " +
            "with power 4 or greater."
    }

    activatedAbility {
        cost = AbilityCost.Composite(
            listOf(AbilityCost.Atom(CostAtom.Mana(ManaCost.parse("{2}{G}{G}"))), AbilityCost.ExileSelf)
        )
        effect = CreateTokenCopyOfTargetEffect(
            target = EffectTarget.Self,
            overridePower = 4,
            overrideToughness = 4,
            overrideColors = setOf(Color.BLACK),
            addedSubtypes = setOf(Subtype.ZOMBIE),
            noManaCost = true,
        )
        timing = TimingRule.SorcerySpeed
        activateFromZone = Zone.GRAVEYARD
        description = "Eternalize {2}{G}{G} (Exile this card from your graveyard and pay its " +
            "eternalize cost: Create a token that's a copy of it, except it's a 4/4 black Zombie " +
            "in addition to its other types and has no mana cost. Eternalize only as a sorcery.)"
    }
    keywords(Keyword.ETERNALIZE)

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "152"
        artist = "Scott Murphy"
        imageUri = "https://cards.scryfall.io/normal/front/1/f/1f9fb33a-3b39-4aff-93b8-aedafe0ea694.jpg?1783911261"
    }
}
