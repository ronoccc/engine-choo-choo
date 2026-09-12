package com.wingedsheep.mtg.sets.definitions.mom.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.effects.Mode
import com.wingedsheep.sdk.scripting.effects.ModalEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Ghalta and Mavren
 * {3}{G}{G}{W}{W}
 * Legendary Creature — Dinosaur Vampire
 * Trample
 * Whenever you attack, choose one —
 * • Create a tapped and attacking X/X green Dinosaur creature token with trample, where X is the
 *   greatest power among other attacking creatures.
 * • Create X 1/1 white Vampire creature tokens with lifelink, where X is the number of other
 *   attacking creatures.
 *
 * `TriggeredAbilityBuilder` has no modal sugar of its own (`modal { }` is `SpellBuilder`-only), so
 * the choose-one is built directly from the underlying [ModalEffect]/[Mode] primitives it lowers
 * to — a plain [Effect], usable in any builder. "Other attacking creatures" excludes this
 * permanent itself ([GameObjectFilter.notSourceItself]); mode 1 feeds that count's `maxPower()`
 * into [CreateTokenEffect.dynamicPower]/`dynamicToughness`, mode 2 feeds its `count()` into
 * [CreateTokenEffect.count].
 */
val GhaltaAndMavren = card("Ghalta and Mavren") {
    manaCost = "{3}{G}{G}{W}{W}"
    colorIdentity = "GW"
    typeLine = "Legendary Creature — Dinosaur Vampire"
    power = 12
    toughness = 12
    oracleText = "Trample\n" +
        "Whenever you attack, choose one —\n" +
        "• Create a tapped and attacking X/X green Dinosaur creature token with trample, where " +
        "X is the greatest power among other attacking creatures.\n" +
        "• Create X 1/1 white Vampire creature tokens with lifelink, where X is the number of " +
        "other attacking creatures."

    keywords(Keyword.TRAMPLE)

    val otherAttackers = GameObjectFilter.Creature.youControl().attacking().notSourceItself()

    triggeredAbility {
        trigger = Triggers.YouAttack
        effect = ModalEffect.chooseOne(
            Mode.noTarget(
                CreateTokenEffect(
                    count = DynamicAmount.Fixed(1),
                    power = 0,
                    toughness = 0,
                    dynamicPower = DynamicAmounts.battlefield(Player.You, otherAttackers).maxPower(),
                    dynamicToughness = DynamicAmounts.battlefield(Player.You, otherAttackers).maxPower(),
                    colors = setOf(Color.GREEN),
                    creatureTypes = setOf("Dinosaur"),
                    keywords = setOf(Keyword.TRAMPLE),
                    tapped = true,
                    attacking = true,
                ),
                "Create a tapped and attacking X/X green Dinosaur creature token with trample, " +
                    "where X is the greatest power among other attacking creatures."
            ),
            Mode.noTarget(
                CreateTokenEffect(
                    count = DynamicAmounts.battlefield(Player.You, otherAttackers).count(),
                    power = 1,
                    toughness = 1,
                    colors = setOf(Color.WHITE),
                    creatureTypes = setOf("Vampire"),
                    keywords = setOf(Keyword.LIFELINK),
                ),
                "Create X 1/1 white Vampire creature tokens with lifelink, where X is the " +
                    "number of other attacking creatures."
            ),
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "225"
        artist = "Zezhou Chen"
        imageUri = "https://cards.scryfall.io/normal/front/7/c/7ccb21e9-5169-4934-83c3-78ac00e5ffc7.jpg?1783916879"
    }
}
