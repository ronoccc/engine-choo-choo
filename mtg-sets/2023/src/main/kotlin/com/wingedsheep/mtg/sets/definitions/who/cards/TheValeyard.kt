package com.wingedsheep.mtg.sets.definitions.who.cards

import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.VillainousChoiceExtraForOpponents

/**
 * The Valeyard {2}{U}{B}{R}
 * Legendary Creature — Time Lord Noble
 * 4/5
 *
 * "If an opponent would face a villainous choice, they face that choice an additional time.
 *  (They can make the same or different choices.)
 *  While voting, you may vote an additional time."
 *
 * Modeling notes:
 *  - Ability 1 is implemented as the new marker static ability
 *    [VillainousChoiceExtraForOpponents]. [com.wingedsheep.engine.handlers.effects.composite.ChooseActionEffectExecutor]
 *    checks the battlefield for it directly: any [com.wingedsheep.sdk.scripting.effects.ChooseActionEffect]
 *    flagged `isVillainousChoice = true` whose choosing player is an opponent of this permanent's
 *    controller is resolved one additional time per copy (independently — the chooser may pick the
 *    same or a different option each time). See Davros, Dalek Creator; Midnight Crusader Shuttle;
 *    Dr. Eggman for cards that flag their `ChooseActionEffect` this way.
 *  - TODO: Ability 2 ("While voting, you may vote an additional time") is **not implemented**.
 *    Voting (CR 701.31, the "Council's Dilemma" mechanic) has no engine representation anywhere in
 *    this corpus yet — no `Vote` effect, no vote tally/decision plumbing, no card in the corpus casts
 *    a vote. Building it is a whole new mechanic (an `add-feature`-scale addition: a vote effect,
 *    a per-player vote decision, tally resolution, and the "for each vote" payoff shape), not a
 *    one-line composition of existing primitives, and no other card in this batch exercises voting
 *    to anchor the work. The printed line is preserved verbatim in `oracleText` below; only the
 *    ability itself is left unimplemented pending that mechanic's addition.
 */
val TheValeyard = card("The Valeyard") {
    manaCost = "{2}{U}{B}{R}"
    colorIdentity = "UBR"
    typeLine = "Legendary Creature — Time Lord Noble"
    power = 4
    toughness = 5
    oracleText = "If an opponent would face a villainous choice, they face that choice an " +
        "additional time. (They can make the same or different choices.)\n" +
        "While voting, you may vote an additional time."

    staticAbility {
        ability = VillainousChoiceExtraForOpponents
    }

    // TODO: "While voting, you may vote an additional time." — no vote mechanic exists in the
    // engine yet (see class-doc note above). Left unimplemented; oracle text preserved above.

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "165"
        artist = "Tyukina Tatiana"
        flavorText = "\"The most damning is still to come. And when I have finished, this court " +
            "will demand your life!\""
        imageUri = "https://cards.scryfall.io/normal/front/d/6/d63218a4-afaf-4ad8-9ca4-4f9af87877b9.jpg?1783914619"
    }
}
