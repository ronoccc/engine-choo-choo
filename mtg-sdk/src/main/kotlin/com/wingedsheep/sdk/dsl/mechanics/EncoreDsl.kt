package com.wingedsheep.sdk.dsl

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.ActivatedAbility
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.costs.CostAtom
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.values.DynamicAmount

/**
 * The Encore ability (CR 702.141a):
 *
 *   "[cost], Exile this card from your graveyard: For each opponent, create a token that's a copy
 *    of this card that attacks that opponent this turn if able. The tokens gain haste. Sacrifice
 *    them at the beginning of the next end step. Activate only as a sorcery."
 *
 * Composed exactly like [embalmAbility] / Eternalize - an ordinary graveyard-activated ability
 * needing no engine subsystem of its own:
 *  - the mana [cost] plus [AbilityCost.ExileSelf] (the card exiles itself from the graveyard as
 *    part of the cost - the same "no responding by exiling the card first" shape Embalm has),
 *  - `activateFromZone = Zone.GRAVEYARD`, so `ZoneActivatedAbilityEnumerator` surfaces it while the
 *    card is in the graveyard,
 *  - `timing = TimingRule.SorcerySpeed` for "Activate only as a sorcery", and
 *  - a [CreateTokenCopyOfTargetEffect] of the card itself, but unlike Embalm/Eternalize this makes
 *    one token per opponent, each attacking a different one: `count =
 *    DynamicAmount.PlayerCount(Player.EachOpponent)` paired with `distinctAttackDefenders = true`
 *    so token i attacks opponent i rather than every token sharing one resolved defender (see
 *    that field's doc - a single shared defender is only ever correct in a 1-opponent game).
 *    `attacking = true` and `addedKeywords = setOf(Keyword.HASTE)` spell "attacks ... if able" and
 *    "the tokens gain haste"; `sacrificeAtStep = Step.END` with
 *    `sacrificeOnlyOnControllersTurn = false` spells "sacrifice them at the beginning of the next
 *    end step" - the very next end step of any player's turn (the delayed-trigger machinery
 *    always fires at the next occurrence of the step, never retroactively), unlike Mardu
 *    Siegebreaker's "your next end step" (`sacrificeOnlyOnControllersTurn = true`).
 *
 * The source resolves through [EffectTarget.Self] - by the time the effect runs the card is in
 * exile, and the copy is made from what was printed on it.
 */
fun encoreAbility(cost: ManaCost): ActivatedAbility = ActivatedAbility(
    cost = AbilityCost.Composite(
        listOf(AbilityCost.Atom(CostAtom.Mana(cost)), AbilityCost.ExileSelf)
    ),
    effect = CreateTokenCopyOfTargetEffect(
        target = EffectTarget.Self,
        count = DynamicAmount.PlayerCount(Player.EachOpponent),
        attacking = true,
        distinctAttackDefenders = true,
        addedKeywords = setOf(Keyword.HASTE),
        sacrificeAtStep = Step.END,
        sacrificeOnlyOnControllersTurn = false,
    ),
    timing = TimingRule.SorcerySpeed,
    activateFromZone = Zone.GRAVEYARD,
    descriptionOverride = "Encore $cost ($cost, Exile this card from your graveyard: For each " +
        "opponent, create a token copy that attacks that opponent this turn if able. They gain " +
        "haste. Sacrifice them at the beginning of the next end step. Activate only as a sorcery.)",
)

/**
 * Add Encore [cost] (CR 702.141, Streets of New Capenna) to a creature card.
 *
 * ```kotlin
 * encore("{6}{W}{W}")
 * ```
 *
 * See [encoreAbility] for how the ability is composed.
 */
fun CardBuilder.encore(cost: String) {
    activatedAbilities.add(encoreAbility(ManaCost.parse(cost)))
    keywordSet.add(Keyword.ENCORE)
}
