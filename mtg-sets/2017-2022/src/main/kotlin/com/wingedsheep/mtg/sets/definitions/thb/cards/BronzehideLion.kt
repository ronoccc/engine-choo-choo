package com.wingedsheep.mtg.sets.definitions.thb.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ActivatedAbility
import com.wingedsheep.sdk.scripting.CompositeStaticAbility
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GrantActivatedAbility
import com.wingedsheep.sdk.scripting.LoseAllAbilities
import com.wingedsheep.sdk.scripting.TransformPermanent
import com.wingedsheep.sdk.scripting.conditions.SourceReturnedAsAura
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Bronzehide Lion
 * {G}{W}
 * Creature — Cat
 * 3/3
 *
 * {G}{W}: This creature gains indestructible until end of turn.
 * When this creature dies, return it to the battlefield. It's an Aura enchantment with enchant
 * creature you control and "{G}{W}: Enchanted creature gains indestructible until end of turn,"
 * and it loses all other abilities.
 *
 * Not a named keyword mechanic — Theros Beyond Death prints exactly one card this shape, so it's
 * hand-composed like Witness Protection/Sugar Coat rather than a `dsl/mechanics/` helper. Reuses
 * the new `becomesAuraOnAttach` return primitive (see `docs/card-sdk-language-reference.md` §
 * "Dies, returns as an Aura"):
 *  - The dies trigger returns the Lion attached to a creature its controller chooses at
 *    resolution (CR 303.4f). If no legal host exists, the return doesn't happen at all — the card
 *    stays in its owner's graveyard (CR 303.4g, and Scryfall's ruling: "If there's nothing it can
 *    legally enchant, it remains in its owner's graveyard").
 *  - A marker-gated `CompositeStaticAbility` bundle (Layer 4 type change + Layer 6 full ability
 *    strip) turns the returned instance into a plain Aura enchantment with none of its printed
 *    abilities — so the original `{G}{W}: This creature gains indestructible` line is gone and
 *    the Lion can never re-trigger its own dies clause a second time.
 *  - A separate marker-gated `GrantActivatedAbility` adds the granted ability
 *    ("{G}{W}: Enchanted creature gains indestructible until end of turn") back onto the Aura,
 *    targeting whatever it's attached to.
 *  - `UnattachedAurasCheck` (CR 704.5m) then sweeps the returned Aura to the graveyard exactly
 *    like a printed Aura, including if the enchanted creature is later removed or if control of
 *    the two ever splits (CR 303.4c) — using the marker's own host filter instead of a
 *    `script.auraTarget`, since the printed type line here is Creature, not Aura, and
 *    `CardValidator` requires the latter for that field.
 */
val BronzehideLion = card("Bronzehide Lion") {
    manaCost = "{G}{W}"
    colorIdentity = "GW"
    typeLine = "Creature — Cat"
    power = 3
    toughness = 3
    oracleText = "{G}{W}: This creature gains indestructible until end of turn.\n" +
        "When this creature dies, return it to the battlefield. It's an Aura enchantment with " +
        "enchant creature you control and \"{G}{W}: Enchanted creature gains indestructible " +
        "until end of turn,\" and it loses all other abilities."

    activatedAbility {
        cost = Costs.Mana("{G}{W}")
        effect = Effects.GrantKeyword(Keyword.INDESTRUCTIBLE, EffectTarget.Self)
    }

    triggeredAbility {
        trigger = Triggers.Dies
        effect = Effects.PutOntoBattlefieldAttachedToChosen(
            target = EffectTarget.Self,
            hostFilter = GameObjectFilter.Creature.youControl(),
            becomesAuraOnAttach = true
        )
    }

    staticAbility {
        condition = SourceReturnedAsAura
        ability = CompositeStaticAbility(
            listOf(
                TransformPermanent(
                    setCardTypes = setOf("ENCHANTMENT"),
                    setSubtypes = setOf("Aura"),
                    filter = GroupFilter.source()
                ),
                LoseAllAbilities(filter = GroupFilter.source())
            )
        )
    }

    staticAbility {
        condition = SourceReturnedAsAura
        ability = GrantActivatedAbility(
            ability = ActivatedAbility(
                cost = Costs.Mana("{G}{W}"),
                effect = Effects.GrantKeyword(Keyword.INDESTRUCTIBLE, EffectTarget.EnchantedCreature),
                descriptionOverride = "{G}{W}: Enchanted creature gains indestructible until end of turn."
            ),
            filter = GroupFilter.source()
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "210"
        artist = "Alayna Danner"
        imageUri = "https://cards.scryfall.io/normal/front/8/f/8fdafadb-fa04-4282-b576-b85e79c242c9.jpg?1783931524"
        ruling(
            "2020-01-24",
            "If you control but don't own Bronzehide Lion, you'll return it to the battlefield " +
                "when it dies, not its owner. It'll enchant a creature you control."
        )
        ruling(
            "2020-01-24",
            "You choose a creature you control for Bronzehide Lion to enchant as it returns to " +
                "the battlefield. If there's nothing it can legally enchant, it remains in its " +
                "owner's graveyard."
        )
        ruling(
            "2020-01-24",
            "If a token is a copy of Bronzehide Lion, it won't return from its owner's graveyard."
        )
    }
}
