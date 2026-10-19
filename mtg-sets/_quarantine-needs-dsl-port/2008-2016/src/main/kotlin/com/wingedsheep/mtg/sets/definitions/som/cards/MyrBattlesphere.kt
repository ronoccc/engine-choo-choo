package com.wingedsheep.mtg.sets.definitions.som.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser
import com.wingedsheep.sdk.scripting.effects.ConditionalOnCollectionEffect
import com.wingedsheep.sdk.scripting.effects.GatherCardsEffect
import com.wingedsheep.sdk.scripting.effects.SelectFromCollectionEffect
import com.wingedsheep.sdk.scripting.effects.SelectionMode
import com.wingedsheep.sdk.scripting.effects.TapUntapCollectionEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * Myr Battlesphere
 * {7}
 * Artifact Creature — Myr Construct
 * 4/7
 *
 * When this creature enters, create four 1/1 colorless Myr artifact creature tokens.
 * Whenever this creature attacks, you may tap X untapped Myr you control. If you do, this
 * creature gets +X/+0 until end of turn and deals X damage to the player or planeswalker it's
 * attacking.
 *
 * The attack trigger is a Gather → Select → Tap pipeline: gather the controller's untapped Myr
 * (`CardSource.ControlledPermanents` filtered to the Myr subtype and untapped), let the
 * controller choose any number of them (`SelectionMode.ChooseAnyNumber`, which is the "may tap X"
 * — zero selected is the "don't" branch), tap the chosen set, then pump and burn for
 * `DistinctEntitiesInCollections(["tappedMyr"])` (= X) gated on the selection being non-empty.
 *
 * Fidelity gap: the damage is dealt to `Player.DefendingPlayer`, which — per the SDK's own
 * documented CR 802.2a resolution — is the player *behind* the attacked object (so attacking a
 * planeswalker still resolves to that planeswalker's controller). The engine has no "the
 * planeswalker or battle this creature is attacking" damage target, so a planeswalker being
 * attacked is not itself dealt the damage; it lands on its controller instead.
 */
val MyrBattlesphere = card("Myr Battlesphere") {
    manaCost = "{7}"
    colorIdentity = ""
    typeLine = "Artifact Creature — Myr Construct"
    power = 4
    toughness = 7
    oracleText = "When this creature enters, create four 1/1 colorless Myr artifact creature tokens.\n" +
        "Whenever this creature attacks, you may tap X untapped Myr you control. If you do, this " +
        "creature gets +X/+0 until end of turn and deals X damage to the player or planeswalker " +
        "it's attacking."

    triggeredAbility {
        trigger = Triggers.EntersBattlefield
        effect = Effects.CreateToken(
            power = 1,
            toughness = 1,
            colors = emptySet(),
            creatureTypes = setOf("Myr"),
            count = 4,
            artifactToken = true,
            imageUri = "https://cards.scryfall.io/normal/front/b/0/b0ae94ed-7314-470b-baba-f2f58bbc894a.jpg?1783941703"
        )
        description = "When this creature enters, create four 1/1 colorless Myr artifact creature tokens."
    }

    triggeredAbility {
        trigger = Triggers.Attacks
        effect = Effects.Composite(
            GatherCardsEffect(
                source = CardSource.ControlledPermanents(
                    player = Player.You,
                    filter = GameObjectFilter.Creature.withSubtype("Myr").untapped()
                ),
                storeAs = "untappedMyr"
            ),
            SelectFromCollectionEffect(
                from = "untappedMyr",
                selection = SelectionMode.ChooseAnyNumber,
                chooser = Chooser.Controller,
                storeSelected = "tappedMyr",
                prompt = "You may tap X untapped Myr you control",
                alwaysPrompt = true
            ),
            TapUntapCollectionEffect(collectionName = "tappedMyr", tap = true),
            ConditionalOnCollectionEffect(
                collection = "tappedMyr",
                ifNotEmpty = Effects.Composite(
                    Effects.ModifyStats(
                        DynamicAmount.DistinctEntitiesInCollections(listOf("tappedMyr")),
                        DynamicAmount.Fixed(0),
                        EffectTarget.Self
                    ),
                    Effects.DealDamage(
                        DynamicAmount.DistinctEntitiesInCollections(listOf("tappedMyr")),
                        EffectTarget.PlayerRef(Player.DefendingPlayer)
                    )
                )
            )
        )
        description = "Whenever this creature attacks, you may tap X untapped Myr you control. If " +
            "you do, this creature gets +X/+0 until end of turn and deals X damage to the player " +
            "or planeswalker it's attacking."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "180"
        artist = "Franz Vohwinkel"
        imageUri = "https://cards.scryfall.io/normal/front/b/0/b0ae94ed-7314-470b-baba-f2f58bbc894a.jpg?1783941703"
    }
}
