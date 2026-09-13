package com.wingedsheep.mtg.sets.definitions.rna.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Guardian Project
 * {3}{G}
 * Enchantment
 *
 * Whenever a nontoken creature you control enters, if it doesn't have the same name as another
 * creature you control or a creature card in your graveyard, draw a card.
 *
 * ANY binding: the trigger fires off *any* matching creature entering, not just Guardian Project
 * itself (it's an enchantment, so it can never be the triggering creature anyway). The intervening
 * "if" (CR 603.4) is checked once when the trigger would go on the stack and again on resolution —
 * [Conditions.SameNameAsAnotherControlledPermanentOrGraveyardCard] scans the controller's
 * battlefield and graveyard for a same-named creature/creature card in one pass, wrapped in
 * [Conditions.Not] for "doesn't have the same name". `EffectTarget.TriggeringEntity` names the
 * creature that just entered — this trigger has no target to check, which is exactly the gap that
 * condition's `entity: EffectTarget` generality closes over the older, target-only
 * `AnotherPermanentWithSameNameAsTarget`.
 */
val GuardianProject = card("Guardian Project") {
    manaCost = "{3}{G}"
    colorIdentity = "G"
    typeLine = "Enchantment"
    oracleText = "Whenever a nontoken creature you control enters, if it doesn't have the same " +
        "name as another creature you control or a creature card in your graveyard, draw a card."

    triggeredAbility {
        trigger = Triggers.entersBattlefield(
            filter = GameObjectFilter.Creature.youControl().nontoken(),
            binding = TriggerBinding.ANY,
        )
        interveningIf = Conditions.Not(
            Conditions.SameNameAsAnotherControlledPermanentOrGraveyardCard(EffectTarget.TriggeringEntity)
        )
        effect = Effects.DrawCards(1)
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "130"
        artist = "Chris Rallis"
        flavorText = "Simic's strength comes from its diversity."
        imageUri = "https://cards.scryfall.io/normal/front/c/c/ccad6ce0-ddf0-458d-bdae-3d7805fdc775.jpg?1783933671"
    }
}
