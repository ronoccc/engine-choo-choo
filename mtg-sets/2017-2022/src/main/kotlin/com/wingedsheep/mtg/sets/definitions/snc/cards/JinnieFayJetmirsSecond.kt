package com.wingedsheep.mtg.sets.definitions.snc.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.AlternateTokenTemplate
import com.wingedsheep.sdk.scripting.ReplaceTokenCreationWithChoiceOfTokens

/**
 * Jinnie Fay, Jetmir's Second
 * {R/G}{G}{G/W}
 * Legendary Creature — Elf Druid
 * 3/3
 *
 * If you would create one or more tokens, you may instead create that many 2/2 green Cat
 * creature tokens with haste or that many 3/1 green Dog creature tokens with vigilance.
 *
 * Modeled with [ReplaceTokenCreationWithChoiceOfTokens] — a general "choose one of several
 * named alternate token templates" replacement effect built for this card (no existing
 * primitive covered a player choice between named token templates; see
 * `docs/card-sdk-language-reference.md` for the general vocabulary). Per the printed ruling,
 * the chosen template's characteristics wholly replace the original tokens' — no keyword or
 * ability the original creation would have granted survives — while riders describing *how*
 * the tokens enter (tapped, attacking, exile-/sacrifice-at-step) still apply.
 *
 * The choice (including "don't replace") belongs to Jinnie Fay's controller — CR 616.1's
 * "if you would create" is self-referential to whichever player controls this ability, not to
 * whoever controls the effect that's creating the tokens.
 */
val JinnieFayJetmirsSecond = card("Jinnie Fay, Jetmir's Second") {
    manaCost = "{R/G}{G}{G/W}"
    colorIdentity = "GRW"
    typeLine = "Legendary Creature — Elf Druid"
    oracleText = "If you would create one or more tokens, you may instead create that many 2/2 " +
        "green Cat creature tokens with haste or that many 3/1 green Dog creature tokens with " +
        "vigilance."
    power = 3
    toughness = 3

    replacementEffect(
        ReplaceTokenCreationWithChoiceOfTokens(
            templates = listOf(
                AlternateTokenTemplate(
                    power = 2,
                    toughness = 2,
                    colors = setOf(Color.GREEN),
                    creatureTypes = setOf("Cat"),
                    keywords = setOf(Keyword.HASTE),
                    imageUri = "https://cards.scryfall.io/normal/front/5/7/57bd0b29-0860-4ee3-832a-f62ba7bf4aac.jpg?1783922959"
                ),
                AlternateTokenTemplate(
                    power = 3,
                    toughness = 1,
                    colors = setOf(Color.GREEN),
                    creatureTypes = setOf("Dog"),
                    keywords = setOf(Keyword.VIGILANCE),
                    imageUri = "https://cards.scryfall.io/normal/front/6/7/6736fe71-bd55-4602-b0aa-ece6193048a9.jpg?1783922959"
                ),
            )
        )
    )

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "195"
        artist = "David Gaillet"
        flavorText = "She watches Jetmir's back while her companions watch hers."
        imageUri = "https://cards.scryfall.io/normal/front/c/5/c5f9326a-2a41-45b3-97c3-548f0bdc0882.jpg?1783923082"
        ruling(
            "2022-04-29",
            "The tokens' characteristics are entirely replaced by either 2/2 green Cat creature " +
                "token with haste or 3/1 green Dog creature token with vigilance. They don't have " +
                "any other abilities the tokens would have been created with. Anything else " +
                "specified in the effect creating the tokens (such as tapped, attacking, \"That " +
                "token gains haste,\" or \"Exile that token at end of combat\") still applies."
        )
    }
}
