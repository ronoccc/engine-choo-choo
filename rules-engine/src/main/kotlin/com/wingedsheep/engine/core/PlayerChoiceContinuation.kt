package com.wingedsheep.engine.core

import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.effects.Mode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resume after the resolved chooser of a
 * [com.wingedsheep.sdk.scripting.effects.PlayerChoiceEffect] ("villainous choice") picked one of
 * [options]. Runs that option's effect in the original [effectContext] — captured unmodified, the
 * same way [com.wingedsheep.engine.core.ChooseGuessKindContinuation] carries its context — so an
 * option performed by the chooser (`EffectTarget.TargetController`) and a sibling option performed
 * by the original caster (`EffectTarget.Controller`) both resolve correctly without this
 * continuation rebinding anything.
 */
@Serializable
@SerialName("PlayerChoiceContinuation")
data class PlayerChoiceContinuation(
    val chooserId: EntityId,
    val options: List<Mode>,
    val effectContext: EffectContext,
) : AnswerContinuation
