package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.c19.cards.IdolOfOblivion
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Idol of Oblivion (C19 #55) — {2} artifact.
 *
 * "{T}: Draw a card. Activate only if you created a token this turn."
 * "{8}, {T}, Sacrifice this artifact: Create a 10/10 colorless Eldrazi creature token."
 *
 * The draw ability is the motivating card for the new `TurnTracker.TOKENS_CREATED` tracker
 * (`Conditions.YouCreatedATokenThisTurn`): these tests prove the card actually wires into it —
 * illegal with no token created this turn, legal once one has been, and — per the printed
 * 2019-08-23 ruling — still legal after that token has left the battlefield. The tracker's own
 * accrual/reset mechanics are covered exhaustively by `TokensCreatedTrackerTest` in `rules-engine`;
 * this file only proves Idol of Oblivion's own cost/effect/restriction wiring.
 */
class IdolOfOblivionScenarioTest : FunSpec({

    val drawAbilityId = IdolOfOblivion.activatedAbilities[0].id
    val sacAbilityId = IdolOfOblivion.activatedAbilities[1].id

    fun setup(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(IdolOfOblivion))
        driver.initMirrorMatch(deck = Deck.of("Island" to 40), skipMulligans = true)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return driver
    }

    fun GameTestDriver.resolveStackAndDecisions() {
        var guard = 0
        while (guard++ < 40 && (pendingDecision != null || state.stack.isNotEmpty())) {
            if (pendingDecision != null) autoResolveDecision() else bothPass()
        }
    }

    /** Activate Idol [idol]'s sacrifice ability, paying {8} — creates the Eldrazi token. */
    fun GameTestDriver.activateSacrificeAbility(player: EntityId, idol: EntityId) {
        giveMana(player, Color.BLUE, 8)
        submitSuccess(ActivateAbility(playerId = player, sourceId = idol, abilityId = sacAbilityId))
        resolveStackAndDecisions()
    }

    test("the draw ability is illegal before any token has been created this turn") {
        val driver = setup()
        val p1 = driver.activePlayer!!
        val idol = driver.putPermanentOnBattlefield(p1, "Idol of Oblivion")

        val result = driver.submit(ActivateAbility(playerId = p1, sourceId = idol, abilityId = drawAbilityId))

        result.isSuccess shouldBe false
        result.error shouldNotBe null
    }

    test("creating a token this turn makes the draw ability legal, and it draws a card") {
        val driver = setup()
        val p1 = driver.activePlayer!!
        val sacrificer = driver.putPermanentOnBattlefield(p1, "Idol of Oblivion")
        val drawer = driver.putPermanentOnBattlefield(p1, "Idol of Oblivion")

        driver.activateSacrificeAbility(p1, sacrificer)
        val handSizeBefore = driver.getHand(p1).size

        driver.submitSuccess(ActivateAbility(playerId = p1, sourceId = drawer, abilityId = drawAbilityId))
        driver.resolveStackAndDecisions()

        driver.getHand(p1).size shouldBe handSizeBefore + 1
    }

    test("the draw ability stays legal after the created token has left the battlefield") {
        val driver = setup()
        val p1 = driver.activePlayer!!
        val sacrificer = driver.putPermanentOnBattlefield(p1, "Idol of Oblivion")
        val drawer = driver.putPermanentOnBattlefield(p1, "Idol of Oblivion")

        driver.activateSacrificeAbility(p1, sacrificer)
        val eldrazi = driver.getPermanents(p1).single { driver.getCardName(it)?.contains("Eldrazi") == true }

        // The token leaves the battlefield entirely — the draw ability must not care.
        driver.replaceState(
            com.wingedsheep.engine.handlers.effects.ZoneTransitionService.moveToZone(
                state = driver.state,
                entityId = eldrazi,
                destinationZone = com.wingedsheep.sdk.core.Zone.EXILE
            ).state
        )

        val result = driver.submit(ActivateAbility(playerId = p1, sourceId = drawer, abilityId = drawAbilityId))

        result.isSuccess shouldBe true
    }

    test("the sacrifice ability creates a 10/10 colorless Eldrazi token and sacrifices Idol") {
        val driver = setup()
        val p1 = driver.activePlayer!!
        val idol = driver.putPermanentOnBattlefield(p1, "Idol of Oblivion")

        driver.activateSacrificeAbility(p1, idol)

        driver.findPermanent(p1, "Idol of Oblivion") shouldBe null // sacrificed
        val eldrazi = driver.getPermanents(p1).single { driver.getCardName(it)?.contains("Eldrazi") == true }
        driver.state.projectedState.getPower(eldrazi) shouldBe 10
        driver.state.projectedState.getToughness(eldrazi) shouldBe 10
        driver.state.projectedState.getColors(eldrazi) shouldBe emptySet()
    }
})
