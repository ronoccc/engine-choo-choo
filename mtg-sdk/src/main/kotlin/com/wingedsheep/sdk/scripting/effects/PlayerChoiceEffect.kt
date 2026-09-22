package com.wingedsheep.sdk.scripting.effects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * "[chooser] faces a villainous choice — [options]." The resolved [chooser] (not necessarily the
 * spell's controller) picks exactly one of [options] and that option's effect resolves — the
 * Doctor Who "villainous choice" mechanic (e.g. This Is How It Ends, Great Intelligence's Plan).
 *
 * Distinct from [ModalEffect]: a modal spell's mode is always picked by the spell's controller
 * (CR 700.2, `700.2a`), while here the deciding player is whoever [chooser] resolves to —
 * routed through the same [Chooser] enum and `ChooserResolution` used elsewhere, so
 * `Chooser.TargetPlayer` ("target opponent faces a villainous choice") and
 * `Chooser.ControllerOfTarget` ("target creature's owner ... faces a villainous choice", with its
 * owner-fallback for a target that already left the battlefield earlier in the same resolution)
 * both work out of the box.
 *
 * [options] resolve in the **original** effect context — the controller/source/targets of the
 * enclosing spell or ability, unchanged. This is what lets one option be performed by the chooser
 * (reference `EffectTarget.TargetController`, which already carries its own controller-with-
 * owner-fallback resolution) while a sibling option is performed by the original caster
 * (`EffectTarget.Controller`) — Great Intelligence's Plan's "They discard three cards, or **you**
 * may cast a spell..." needs exactly this asymmetry, so this effect never rebinds
 * `EffectContext.controllerId` to the chooser the way `ForEachPlayer` rebinds it per iteration.
 *
 * Only [Mode.effect] is used — per-option [Mode.targetRequirements] / [Mode.additionalCosts] are
 * not currently wired (no printed "villainous choice" needs cast-time per-option targeting; a
 * resolution-time selection like "shuffle another creature they own" is instead composed as a
 * Gather → Select → Move pipeline inside the option's own effect, scoped with
 * `ControllerPredicate.ControlledByReferencedPlayer(EffectTarget.TargetController)`).
 */
@Serializable
@SerialName("PlayerChoiceEffect")
data class PlayerChoiceEffect(
    val chooser: Chooser,
    val options: List<Mode>,
    val promptOverride: String? = null,
    /**
     * Internal — set by [com.wingedsheep.engine.handlers.effects.composite.PlayerChoiceEffectExecutor]
     * on the repeated copies it builds for The Valeyard's "faces that choice an additional time"
     * (`VillainousChoiceExtraForOpponents`), so those copies don't re-check for doublers and
     * re-multiply themselves — the same one-shot guard [com.wingedsheep.sdk.scripting.effects.ChooseActionEffect.isVillainousChoice]
     * gets by being flipped to `false` on its own repeated copies. Card authors never set this;
     * it always starts `false`.
     */
    val alreadyDoubled: Boolean = false,
) : Effect {
    override val description: String = buildString {
        append(promptOverride ?: "faces a villainous choice —")
        options.forEach { append("\n• "); append(it.description) }
    }
}
