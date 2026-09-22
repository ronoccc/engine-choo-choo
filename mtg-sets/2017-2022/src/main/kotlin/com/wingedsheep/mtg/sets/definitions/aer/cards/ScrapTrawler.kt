package com.wingedsheep.mtg.sets.definitions.aer.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.EventPattern.ZoneChangeEvent
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggerSpec
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetObject
import com.wingedsheep.sdk.scripting.values.DynamicAmount
import com.wingedsheep.sdk.scripting.values.EntityNumericProperty
import com.wingedsheep.sdk.scripting.values.EntityReference

/**
 * Scrap Trawler
 * {3}
 * Artifact Creature — Construct
 * 3/2
 *
 * Whenever this creature dies or another artifact you control is put into a graveyard from the
 * battlefield, return to your hand target artifact card in your graveyard with lesser mana value.
 *
 * - Trigger: "this creature dies or another artifact you control is put into a graveyard from the
 *   battlefield" is one dying-artifact watcher — [GameObjectFilter.Artifact] with `.youControl()`
 *   on a battlefield-to-graveyard [ZoneChangeEvent], [TriggerBinding.ANY] so it matches both Scrap
 *   Trawler itself and any other artifact it controls (mirrors [Triggers.AnyOpponentDiscards]-style
 *   composition, e.g. Blood Artist's "this creature or another creature dies").
 * - "With lesser mana value": modelled faithfully as *strictly less than* the mana value of the
 *   artifact that triggered the ability, not "any artifact" and not "equal or lesser". The
 *   triggering permanent is already in the graveyard by the time this resolves, so its mana value
 *   is read via [EntityReference.Triggering] (last-known-information-backed) and compared with
 *   `manaValueAtMostDynamic(triggeringManaValue - 1)` — the general "at most a resolved dynamic
 *   amount" cap composed down to strict inequality, since the SDK does not have a dedicated
 *   "strictly less than an entity" predicate.
 * - The target pool is "artifact card in your graveyard" with no exclusion of the just-died
 *   artifact by identity; excluding it is automatic because a card can never have a mana value
 *   strictly less than its own.
 */
val ScrapTrawler = card("Scrap Trawler") {
    manaCost = "{3}"
    colorIdentity = ""
    typeLine = "Artifact Creature — Construct"
    power = 3
    toughness = 2
    oracleText = "Whenever this creature dies or another artifact you control is put into a " +
        "graveyard from the battlefield, return to your hand target artifact card in your " +
        "graveyard with lesser mana value."

    triggeredAbility {
        trigger = TriggerSpec(
            event = ZoneChangeEvent(
                filter = GameObjectFilter.Artifact.youControl(),
                from = Zone.BATTLEFIELD,
                to = Zone.GRAVEYARD
            ),
            binding = TriggerBinding.ANY
        )
        val lesserManaValue = DynamicAmount.Subtract(
            DynamicAmount.EntityProperty(EntityReference.Triggering, EntityNumericProperty.ManaValue),
            DynamicAmount.Fixed(1)
        )
        val t = target(
            "target",
            TargetObject(
                filter = TargetFilter(
                    GameObjectFilter.Artifact.ownedByYou().manaValueAtMostDynamic(lesserManaValue),
                    zone = Zone.GRAVEYARD
                )
            )
        )
        effect = Effects.Move(t, Zone.HAND)
        description = "Whenever this creature dies or another artifact you control is put into a " +
            "graveyard from the battlefield, return to your hand target artifact card in your " +
            "graveyard with lesser mana value."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "175"
        artist = "Daarken"
        imageUri = "https://cards.scryfall.io/normal/front/6/8/68abc75f-596f-4169-96fc-ada941ef47ed.jpg?1783936719"
    }
}
