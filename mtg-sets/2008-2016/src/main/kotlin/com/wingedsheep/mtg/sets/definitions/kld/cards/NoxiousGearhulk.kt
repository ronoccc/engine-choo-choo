package com.wingedsheep.mtg.sets.definitions.kld.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetCreature
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Noxious Gearhulk
 * {4}{B}{B}
 * Artifact Creature — Construct
 * 5/4
 *
 * Menace
 * When this creature enters, you may destroy another target creature. If a creature is destroyed
 * this way, you gain life equal to its toughness.
 *
 * "You may destroy" is the ordinary optional-target shape: `optional = true` on the triggered
 * ability skips the target entirely when declined. The life gain reads `targetToughness(0)`,
 * which is the target's last-known toughness once it has been moved to the graveyard by the
 * `Destroy` that precedes it in the composite.
 *
 * Fidelity gap: the life gain does not check whether the destroy actually happened (e.g. the
 * target had indestructible), so an indestructible creature still nets its controller life equal
 * to its toughness even though nothing died. The engine has no "if a permanent was destroyed this
 * way" gate to hang that check on.
 */
val NoxiousGearhulk = card("Noxious Gearhulk") {
    manaCost = "{4}{B}{B}"
    colorIdentity = "B"
    typeLine = "Artifact Creature — Construct"
    power = 5
    toughness = 4
    oracleText = "Menace\n" +
        "When this creature enters, you may destroy another target creature. If a creature is " +
        "destroyed this way, you gain life equal to its toughness."

    keywords(Keyword.MENACE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        optional = true
        target = TargetCreature(filter = TargetFilter.OtherCreature)
        effect = Effects.Composite(
            Effects.Destroy(EffectTarget.ContextTarget(0)),
            Effects.GainLife(DynamicAmounts.targetToughness(0))
        )
        description = "When this creature enters, you may destroy another target creature. If a " +
            "creature is destroyed this way, you gain life equal to its toughness."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "96"
        artist = "Lius Lasahido"
        imageUri = "https://cards.scryfall.io/normal/front/9/f/9f86e5fe-8723-4494-b4cc-b7ac3a047bd1.jpg?1783937202"
    }
}
