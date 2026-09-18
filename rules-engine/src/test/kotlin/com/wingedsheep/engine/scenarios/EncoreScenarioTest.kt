package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.GameConfig
import com.wingedsheep.engine.core.GameInitializer
import com.wingedsheep.engine.core.PlayerConfig
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.components.combat.AttackingComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.engine.state.components.identity.LifeTotalComponent
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * End-to-end coverage for the Encore keyword (CR 702.141a) through the real
 * `encoreAbility`/`CreateTokenCopyOfTargetEffect` wiring, using Soul of Eternity (Commander
 * Legends) — encore with no other twist (no printed target, so no retargeting decision to drive
 * through) — as the vehicle. [distinctAttackDefenders] is the one genuinely new engine capability
 * this mechanic needed (`CreateTokenCopyOfTargetEffect.distinctAttackDefenders`), so it gets the
 * most scrutiny (a real 3-player pod, not just an assertion in isolation).
 */
class EncoreScenarioTest : FunSpec({

    fun encoreAbilityId(driver: GameTestDriver, cardName: String) =
        driver.cardRegistry.getCard(cardName)!!.activatedAbilities.first().id

    test("2-player: one token attacking the sole opponent, with haste, P/T from current life total") {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), skipMulligans = true)
        val active = driver.activePlayer!!
        val opponent = driver.getOpponent(active)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // Life totals differ from the printed 20/20 default and from each other, to prove the
        // token's P/T reads life *at encore time*, not any snapshot from when the card was alive.
        // The opponent gets a life cushion so a hasty 23-power unblocked attacker doesn't just win
        // the game outright before the test can reach the delayed-sacrifice assertion below.
        driver.replaceState(
            driver.state
                .updateEntity(active) { it.with(LifeTotalComponent(23)) }
                .updateEntity(opponent) { it.with(LifeTotalComponent(999)) }
        )

        val soul = driver.putCardInGraveyard(active, "Soul of Eternity")
        driver.giveMana(active, Color.WHITE, 9) // {7}{W}{W}

        driver.submitSuccess(
            ActivateAbility(
                playerId = active,
                sourceId = soul,
                abilityId = encoreAbilityId(driver, "Soul of Eternity")
            )
        )
        // Activating puts the ability on the stack (CR 602.2b) — it still needs priority passes to
        // actually resolve, same as any other stack object.
        repeat(4) { if (driver.pendingDecision != null) driver.autoResolveDecision() else driver.bothPass() }

        // Paying the cost exiled the card from the graveyard (CR 702.141a: exiling it *is* the cost).
        driver.state.getZone(active, com.wingedsheep.sdk.core.Zone.GRAVEYARD).contains(soul) shouldBe false
        driver.state.getZone(active, com.wingedsheep.sdk.core.Zone.EXILE).contains(soul) shouldBe true

        val tokens = driver.state.getBattlefield(active).filter {
            driver.state.getEntity(it)?.get<CardComponent>()?.name == "Soul of Eternity"
        }
        tokens shouldHaveSize 1
        val token = tokens.single()
        val tokenEntity = driver.state.getEntity(token)
        tokenEntity?.has<TokenComponent>() shouldBe true
        driver.state.projectedState.hasKeyword(token, Keyword.HASTE) shouldBe true
        tokenEntity?.get<AttackingComponent>()?.defenderId shouldBe opponent
        // The CDA reads *current* life (23), not the printed 0/0 base nor any stale snapshot.
        driver.state.projectedState.getPower(token) shouldBe 23
        driver.state.projectedState.getToughness(token) shouldBe 23

        // Sacrificed at the beginning of the next end step — still here mid-turn...
        driver.state.getEntity(token) shouldNotBeNull { }
        driver.passPriorityUntil(Step.END)
        // ...gone once that end step's delayed trigger resolves.
        repeat(4) { if (driver.pendingDecision != null) driver.autoResolveDecision() else driver.bothPass() }
        driver.state.getBattlefield(active).contains(token) shouldBe false
    }

    test("Encore is sorcery-speed only: not activatable during the opponent's turn") {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), skipMulligans = true)
        val active = driver.activePlayer!!
        val opponent = driver.getOpponent(active)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val soul = driver.putCardInGraveyard(active, "Soul of Eternity")
        driver.giveMana(active, Color.WHITE, 9)

        // Pass the turn to the opponent — no longer active player, no longer a sorcery-speed window.
        driver.passPriorityUntil(Step.END)
        driver.passPriority(active)
        driver.passPriority(opponent)

        val result = driver.submit(
            ActivateAbility(
                playerId = active,
                sourceId = soul,
                abilityId = encoreAbilityId(driver, "Soul of Eternity")
            )
        )
        result.isSuccess shouldBe false
    }

    test("3-player pod: each token attacks a different opponent (distinctAttackDefenders)") {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        val deck = Deck.of("Plains" to 40)
        val init = GameInitializer(driver.cardRegistry).initializeGame(
            GameConfig(
                players = (1..3).map { PlayerConfig("Player $it", deck, 20) },
                skipMulligans = true,
                startingPlayerIndex = 0
            )
        )
        driver.replaceState(init.state)
        val players = init.playerIds
        val me = players[0]
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val soul = driver.putCardInGraveyard(me, "Soul of Eternity")
        driver.giveMana(me, Color.WHITE, 9)

        driver.submitSuccess(
            ActivateAbility(
                playerId = me,
                sourceId = soul,
                abilityId = encoreAbilityId(driver, "Soul of Eternity")
            )
        )
        repeat(4) { if (driver.pendingDecision != null) driver.autoResolveDecision() else driver.bothPass() }

        val tokens = driver.state.getBattlefield(me).filter {
            driver.state.getEntity(it)?.get<CardComponent>()?.name == "Soul of Eternity"
        }
        tokens shouldHaveSize 2 // one per opponent (CR 702.141a) — a 3-player pod has 2

        val defenders = tokens.map { driver.state.getEntity(it)?.get<AttackingComponent>()?.defenderId }
        defenders.toSet() shouldBe setOf(players[1], players[2]) // every opponent, each exactly once
        defenders.size shouldBe defenders.toSet().size // no two tokens share a defender
    }
})
