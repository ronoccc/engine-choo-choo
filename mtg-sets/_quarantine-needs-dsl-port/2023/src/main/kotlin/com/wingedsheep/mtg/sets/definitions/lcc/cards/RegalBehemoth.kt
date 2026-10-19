package com.wingedsheep.mtg.sets.definitions.lcc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Regal Behemoth
 * {4}{G}{G}
 * Creature — Dinosaur
 * Trample
 * When this creature enters, you become the monarch.
 * Whenever you tap a land for mana while you're the monarch, add an additional one mana of any
 * color.
 *
 * The ETB is [Effects.BecomeMonarch] (CR 716.1). The second ability is worded "Whenever … add",
 * which is a genuine triggered ability (unlike the static-replacement shape Badgermole Cub uses for
 * its "add an additional {G}" — that one has no color choice to make and no monarch gate), built
 * from [Triggers.landTappedForMana] gated by [Conditions.YouAreMonarch] as an intervening-if. This
 * engine has no "triggered mana ability skips the stack" modeling (CR 605.1b), so functionally the
 * extra mana resolves off the stack like any other triggered ability rather than instantaneously —
 * a minor, accepted approximation; the mana itself is added correctly either way.
 */
val RegalBehemoth = card("Regal Behemoth") {
    manaCost = "{4}{G}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Dinosaur"
    power = 5
    toughness = 5
    oracleText = "Trample\n" +
        "When this creature enters, you become the monarch.\n" +
        "Whenever you tap a land for mana while you're the monarch, add an additional one mana " +
        "of any color."

    keywords(Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        effect = Effects.BecomeMonarch()
    }

    triggeredAbility {
        trigger = Triggers.landTappedForMana(player = Player.You)
        interveningIf = Conditions.YouAreMonarch
        effect = Effects.AddAnyColorMana(1)
        description = "Whenever you tap a land for mana while you're the monarch, add an " +
            "additional one mana of any color."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "251"
        artist = "Jakub Kasper"
        imageUri = "https://cards.scryfall.io/normal/front/e/3/e3a8bdfa-a39c-4ca7-b591-1c6aca4cc5ac.jpg?1783913856"
    }
}
