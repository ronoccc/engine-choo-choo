package com.wingedsheep.mtg.sets.definitions.iko.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.CompanionAbility
import com.wingedsheep.sdk.model.EveryCreatureCardHasSubtype
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Kaheera, the Orphanguard
 * {1}{G/W}{G/W}
 * Legendary Creature — Cat Beast
 * 3/2
 *
 * Companion — Each creature card in your starting deck is a Cat, Elemental, Nightmare, Dinosaur,
 * or Beast card. (If this card is your chosen companion, you may put it into your hand from
 * outside the game for {3} as a sorcery.)
 * Vigilance
 * Each other creature you control that's a Cat, Elemental, Nightmare, Dinosaur, or Beast gets
 * +1/+1 and has vigilance.
 *
 * Two independent halves, per CR 702.139a: the printed keyword is a deckbuilding-time
 * restriction that functions *outside the game* — modeled as [CompanionAbility] /
 * [EveryCreatureCardHasSubtype] on [com.wingedsheep.sdk.model.CardDefinition.companion], carrying
 * no in-game behavior of its own (see `add-feature`'s Companion mechanic commit for the
 * deckbuilding-validation and pregame-zone machinery this plugs into). The creature itself has an
 * ordinary in-game static ability line: Vigilance, plus a lord effect pumping and granting
 * vigilance to every other on-type creature Kaheera's controller controls. The lord's own
 * creature-type list is intentionally the *same* five types as the companion restriction, but
 * they're independent pieces of data on the card — the restriction never gates the lord, and vice
 * versa (a Kaheera cheated onto the battlefield in an off-type deck still buffs on-type
 * creatures).
 */
val KaheeraTheOrphanguard = card("Kaheera, the Orphanguard") {
    manaCost = "{1}{G/W}{G/W}"
    colorIdentity = "GW"
    typeLine = "Legendary Creature — Cat Beast"
    oracleText = "Companion — Each creature card in your starting deck is a Cat, Elemental, " +
        "Nightmare, Dinosaur, or Beast card. (If this card is your chosen companion, you may " +
        "put it into your hand from outside the game for {3} as a sorcery.)\n" +
        "Vigilance\n" +
        "Each other creature you control that's a Cat, Elemental, Nightmare, Dinosaur, or Beast " +
        "gets +1/+1 and has vigilance."
    power = 3
    toughness = 2

    keywords(Keyword.VIGILANCE)

    staticAbility {
        ability = ModifyStats(
            powerBonus = 1,
            toughnessBonus = 1,
            filter = GroupFilter(
                GameObjectFilter.Creature.youControl()
                    .withAnyOfSubtypes(
                        listOf(Subtype.CAT, Subtype.ELEMENTAL, Subtype.NIGHTMARE, Subtype.DINOSAUR, Subtype.BEAST)
                    ),
                excludeSelf = true
            )
        )
    }

    staticAbility {
        ability = GrantKeyword(
            keyword = Keyword.VIGILANCE,
            filter = GroupFilter(
                GameObjectFilter.Creature.youControl()
                    .withAnyOfSubtypes(
                        listOf(Subtype.CAT, Subtype.ELEMENTAL, Subtype.NIGHTMARE, Subtype.DINOSAUR, Subtype.BEAST)
                    ),
                excludeSelf = true
            )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "224"
        artist = "Ryan Pancoast"
        imageUri = "https://cards.scryfall.io/normal/front/d/4/d4ebed0b-8060-4a7b-a060-5cfcd2172b16.jpg?1783931013"
        ruling(
            "2020-04-17",
            "Creature cards in your deck may be a mix of the five creature types that Kaheera " +
                "cares about. They may also have additional types, as long as each creature card " +
                "is at least one of those five. Noncreature cards may have any subtypes."
        )
        ruling(
            "2020-04-17",
            "A creature that's more than one of the five types gets +1/+1 only once from " +
                "Kaheera's last ability."
        )
        ruling(
            "2020-04-17",
            "Your companion begins the game outside the game. In tournament play, this means " +
                "your sideboard. In casual play, it's simply a card you own that's not in your " +
                "starting deck."
        )
        ruling(
            "2020-06-01",
            "Wizards of the Coast has issued functional errata for the Companion mechanic. " +
                "Instead of casting companions from outside the game: Once per game, any time " +
                "you could cast a sorcery (during your main phase when the stack is empty), you " +
                "can pay {3} to put your companion from your sideboard into your hand. This is a " +
                "special action, not an activated ability. It happens immediately and can't be " +
                "responded to."
        )
    }
}.copy(
    companion = CompanionAbility(
        EveryCreatureCardHasSubtype(
            setOf(Subtype.CAT, Subtype.ELEMENTAL, Subtype.NIGHTMARE, Subtype.DINOSAUR, Subtype.BEAST)
        )
    )
)
