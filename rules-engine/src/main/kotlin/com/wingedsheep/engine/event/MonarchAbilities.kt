package com.wingedsheep.engine.event

import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.scripting.AbilityId
import com.wingedsheep.sdk.scripting.EventPattern
import com.wingedsheep.sdk.scripting.TriggerBinding
import com.wingedsheep.sdk.scripting.TriggeredAbility
import com.wingedsheep.sdk.scripting.events.DamageType
import com.wingedsheep.sdk.scripting.events.RecipientFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * The two rules-level triggered abilities every game with a monarch has (CR 716.4, 716.6) — neither
 * is printed on, or granted by, any card, so both are synthesized the same way the inherent speed
 * trigger is (see [SpeedAbilities]): a plain [TriggeredAbility] whose `sourceId`/`controllerId` are
 * supplied per-occurrence rather than by any battlefield scan, and stable [AbilityId]s so the
 * once-per-turn/stack machinery can key on them consistently.
 *
 * The two triggers are detected through genuinely different paths, because
 * [com.wingedsheep.engine.event.TriggerMatcher.matchesTrigger] always returns `false` for
 * [EventPattern.StepEvent] — step triggers are matched by the dedicated
 * [com.wingedsheep.engine.event.TriggerMatcher.matchesStepTrigger] instead, called from
 * [com.wingedsheep.engine.event.TriggerDetector.detectPhaseStepTriggers] (a step-transition sweep,
 * not a per-`GameEvent` dispatch). So [endStepDraw] is synthesized inline in
 * `detectPhaseStepTriggers` itself, while [combatDamageTransfer] — a genuine `DealsDamageEvent`
 * reaction — is synthesized in `TriggerDetector.detectMonarchCombatDamageTrigger`, called from the
 * ordinary per-event dispatch (`detectTriggersForEvent`), the same place
 * `detectInherentSpeedTriggers` hooks in.
 */
object MonarchAbilities {

    /** Stable identity for the monarch's end-step draw. */
    val END_STEP_DRAW_ABILITY_ID: AbilityId = AbilityId("monarch_end_step_draw")

    /** Stable identity for the combat-damage-to-the-monarch transfer. */
    val COMBAT_DAMAGE_TRANSFER_ABILITY_ID: AbilityId = AbilityId("monarch_combat_damage_transfer")

    /** Stack/log label for both sourceless abilities — CR 716 gives neither an object source. */
    const val SOURCE_NAME: String = "the monarch"

    /**
     * "At the beginning of the monarch's end step, that player draws a card." (CR 716.4)
     *
     * `StepEvent(Step.END, Player.You)` reads as "the monarch's end step" because the synthesis
     * site in `TriggerDetector.detectPhaseStepTriggers` always supplies the current monarch as
     * both source and controller when calling `matchesStepTrigger` for this ability, exactly as
     * [SpeedAbilities.inherentSpeedIncrease] does for `Player.You` meaning "your speed". The draw's
     * default target ([EffectTarget.Controller]) resolves to that same player.
     */
    val endStepDraw: TriggeredAbility = TriggeredAbility(
        id = END_STEP_DRAW_ABILITY_ID,
        trigger = EventPattern.StepEvent(Step.END, Player.You),
        binding = TriggerBinding.ANY,
        effect = Effects.DrawCards(1),
        descriptionOverride = "At the beginning of the monarch's end step, that player draws a card."
    )

    /**
     * "Whenever a creature deals combat damage to the monarch, that creature's controller becomes
     * the monarch instead." (CR 716.6)
     *
     * `RecipientFilter.Any` plus [DamageType.Combat] matches any creature dealing combat damage to
     * any player; [com.wingedsheep.engine.event.TriggerDetector.detectMonarchCombatDamageTrigger]
     * only ever synthesizes this trigger for the one event whose recipient actually is the current
     * monarch, sourced from the damaging creature with the creature's controller as the ability's
     * controller — so [Effects.BecomeMonarch]'s default target
     * ([EffectTarget.Controller]) reads as "that creature's controller" for free. Multiple attackers
     * dealing combat damage to the monarch in the same step each get their own instance of this
     * ability (CR 716.6 has no "one or more" batching), which is why the detector emits one
     * `PendingTrigger` per matching [com.wingedsheep.engine.core.DamageDealtEvent] rather than per
     * combat-damage step.
     */
    val combatDamageTransfer: TriggeredAbility = TriggeredAbility(
        id = COMBAT_DAMAGE_TRANSFER_ABILITY_ID,
        trigger = EventPattern.DealsDamageEvent(
            damageType = DamageType.Combat,
            recipient = RecipientFilter.Any
        ),
        binding = TriggerBinding.ANY,
        effect = Effects.BecomeMonarch(target = EffectTarget.Controller),
        descriptionOverride = "Whenever a creature deals combat damage to the monarch, that " +
            "creature's controller becomes the monarch instead."
    )
}
