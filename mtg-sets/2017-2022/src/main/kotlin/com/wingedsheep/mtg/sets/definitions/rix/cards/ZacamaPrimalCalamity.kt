package com.wingedsheep.mtg.sets.definitions.rix.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.GroupPatterns
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Zacama, Primal Calamity
 * {6}{R}{G}{W}
 * Legendary Creature — Elder Dinosaur
 * Reach, vigilance, trample
 * When Zacama enters, if you cast it, untap all lands you control.
 * {2}{R}: Zacama deals 3 damage to target creature.
 * {2}{G}: Destroy target artifact or enchantment.
 * {2}{W}: You gain 3 life.
 *
 * "If you cast it" (not "from your hand") is [Conditions.WasCast] — true from any zone, so
 * casting Zacama from the command zone as a commander still untaps lands. The three activated
 * abilities are independent, each with their own mana cost and no tap symbol (repeatable in the
 * same turn as long as mana allows).
 */
val ZacamaPrimalCalamity = card("Zacama, Primal Calamity") {
    manaCost = "{6}{R}{G}{W}"
    colorIdentity = "GRW"
    typeLine = "Legendary Creature — Elder Dinosaur"
    power = 9
    toughness = 9
    oracleText = "Reach, vigilance, trample\n" +
        "When Zacama enters, if you cast it, untap all lands you control.\n" +
        "{2}{R}: Zacama deals 3 damage to target creature.\n" +
        "{2}{G}: Destroy target artifact or enchantment.\n" +
        "{2}{W}: You gain 3 life."

    keywords(Keyword.REACH, Keyword.VIGILANCE, Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        interveningIf = Conditions.WasCast
        effect = GroupPatterns.untapGroup(GroupFilter(GameObjectFilter.Land.youControl()))
    }

    activatedAbility {
        cost = Costs.Mana("{2}{R}")
        val creature = target("target creature", Targets.Creature)
        effect = Effects.DealDamage(3, creature)
        description = "Zacama deals 3 damage to target creature."
    }

    activatedAbility {
        cost = Costs.Mana("{2}{G}")
        val permanent = target(
            "target artifact or enchantment",
            TargetObject(filter = TargetFilter.ArtifactOrEnchantment)
        )
        effect = Effects.Destroy(permanent)
        description = "Destroy target artifact or enchantment."
    }

    activatedAbility {
        cost = Costs.Mana("{2}{W}")
        effect = Effects.GainLife(3)
        description = "You gain 3 life."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "174"
        artist = "Jaime Jones"
        imageUri = "https://cards.scryfall.io/normal/front/5/a/5aa75f2b-53c5-47c5-96d2-ab796358a96f.jpg?1783935269"
    }
}
