package com.wingedsheep.mtg.sets.definitions.afr.cards

import com.wingedsheep.sdk.core.Counters
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EventPattern
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyLifeGain
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.TargetCreature
import com.wingedsheep.sdk.scripting.targets.TargetObject
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty
import com.wingedsheep.sdk.scripting.values.EntityReference

/**
 * Cleric Class
 * {W}
 * Enchantment — Class
 * (Gain the next level as a sorcery to add its ability.)
 * If you would gain life, you gain that much life plus 1 instead.
 * {3}{W}: Level 2
 * Whenever you gain life, put a +1/+1 counter on target creature you control.
 * {4}{W}: Level 3
 * When this Class becomes level 3, return target creature card from your graveyard to the
 * battlefield. You gain life equal to that creature's toughness.
 *
 * Same `classLevel(level, cost) { }` shape Builder's Talent already established: "When this
 * Class becomes level N" is modeled as an ordinary `Triggers.EntersBattlefield` placed inside
 * that level's block — the engine specifically interprets an ETB-shaped trigger authored at a
 * class level as the level-reached trigger (TriggerDetector.detectClassLevelUpTriggers), so no
 * new SDK vocabulary is needed.
 */
val ClericClass = card("Cleric Class") {
    manaCost = "{W}"
    colorIdentity = "W"
    typeLine = "Enchantment — Class"
    oracleText = "(Gain the next level as a sorcery to add its ability.)\n" +
        "If you would gain life, you gain that much life plus 1 instead.\n" +
        "{3}{W}: Level 2\n" +
        "Whenever you gain life, put a +1/+1 counter on target creature you control.\n" +
        "{4}{W}: Level 3\n" +
        "When this Class becomes level 3, return target creature card from your graveyard to " +
        "the battlefield. You gain life equal to that creature's toughness."

    replacementEffect(
        ModifyLifeGain(
            multiplier = 1,
            modifier = 1,
            appliesTo = EventPattern.LifeGainEvent(player = Player.You)
        )
    )

    classLevel(2, "{3}{W}") {
        triggeredAbility {
            trigger = Triggers.YouGainLife
            val creature = target("target creature you control", TargetCreature(filter = TargetFilter.Creature.youControl()))
            effect = Effects.AddCounters(Counters.PLUS_ONE_PLUS_ONE, 1, creature)
        }
    }

    classLevel(3, "{4}{W}") {
        triggeredAbility {
            trigger = Triggers.EntersBattlefield
            val creatureCard = target(
                "target creature card in your graveyard",
                TargetObject(filter = TargetFilter(GameObjectFilter.Creature, zone = Zone.GRAVEYARD))
            )
            effect = Effects.PutOntoBattlefield(creatureCard) then
                Effects.GainLife(DynamicAmount.EntityProperty(EntityReference.Target(0), EntityNumericProperty.Toughness))
        }
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "6"
        artist = "Alayna Danner"
        imageUri = "https://cards.scryfall.io/normal/front/4/7/47ce8b7e-d8e1-489a-a69e-99089eeb8739.jpg?1783926538"
    }
}
