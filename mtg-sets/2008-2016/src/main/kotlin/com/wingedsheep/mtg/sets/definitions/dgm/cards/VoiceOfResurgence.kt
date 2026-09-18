package com.wingedsheep.mtg.sets.definitions.dgm.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.SetBasePowerToughnessDynamicStatic
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Voice of Resurgence
 * {G}{W}
 * Creature — Elemental
 * 2/2
 * Whenever an opponent casts a spell during your turn and when this creature dies, create a green
 * and white Elemental creature token with "This token's power and toughness are each equal to the
 * number of creatures you control."
 *
 * The token's characteristic-defining P/T is a self-referential [SetBasePowerToughnessDynamicStatic]
 * CDA on the token itself ([GroupFilter.source]), so it updates continuously rather than
 * snapshotting at creation (Hallowed Haunting's Spirit Cleric token idiom).
 */
private val creaturesYouControl = DynamicAmounts.creaturesYouControl()

val VoiceOfResurgence = card("Voice of Resurgence") {
    manaCost = "{G}{W}"
    colorIdentity = "GW"
    typeLine = "Creature — Elemental"
    power = 2
    toughness = 2
    oracleText = "Whenever an opponent casts a spell during your turn and when this creature dies, " +
        "create a green and white Elemental creature token with \"This token's power and toughness " +
        "are each equal to the number of creatures you control.\""

    val tokenEffect = Effects.CreateToken(
        power = 0,
        toughness = 0,
        colors = setOf(Color.GREEN, Color.WHITE),
        creatureTypes = setOf("Elemental"),
        imageUri = "https://cards.scryfall.io/normal/front/5/b/5bfb1440-d4c1-42cf-a777-ee1644dbbac7.jpg?1783940045",
        staticAbilities = listOf(
            SetBasePowerToughnessDynamicStatic(
                power = creaturesYouControl,
                toughness = creaturesYouControl,
                filter = GroupFilter.source(),
            ),
        ),
    )

    triggeredAbility {
        trigger = Triggers.OpponentCastsSpell
        triggerRestriction = Conditions.IsYourTurn
        effect = tokenEffect
        description = "Whenever an opponent casts a spell during your turn, create a green and white " +
            "Elemental creature token with \"This token's power and toughness are each equal to the " +
            "number of creatures you control.\""
    }

    triggeredAbility {
        trigger = Triggers.Dies
        effect = tokenEffect
        description = "When this creature dies, create a green and white Elemental creature token " +
            "with \"This token's power and toughness are each equal to the number of creatures you " +
            "control.\""
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "114"
        artist = "Winona Nelson"
        imageUri = "https://cards.scryfall.io/normal/front/0/7/07246783-d475-4f61-99ac-e2b574072349.jpg?1783940019"
    }
}
