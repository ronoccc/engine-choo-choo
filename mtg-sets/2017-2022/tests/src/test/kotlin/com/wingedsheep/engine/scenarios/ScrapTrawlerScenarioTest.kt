package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.aer.cards.ScrapTrawler
import com.wingedsheep.mtg.sets.definitions.atq.cards.Ornithopter
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Scrap Trawler — {3} Artifact Creature — Construct, 3/2
 *
 * Whenever this creature dies or another artifact you control is put into a graveyard from the
 * battlefield, return to your hand target artifact card in your graveyard with lesser mana value.
 */
class ScrapTrawlerScenarioTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(ScrapTrawler)
        driver.registerCard(Ornithopter)
        return driver
    }

    // Resolve the stack, auto-picking the only legal target whenever a ChooseTargetsDecision
    // arrives for the trigger.
    fun drainStack(driver: GameTestDriver) {
        var guard = 0
        while (guard++ < 30) {
            when (val decision = driver.pendingDecision) {
                is ChooseTargetsDecision -> {
                    val legal = decision.legalTargets.values.first()
                    driver.submitTargetSelection(decision.playerId, listOf(legal.first()))
                }
                null -> {
                    if (driver.getTopOfStack() == null) return
                    driver.bothPass()
                }
                else -> driver.bothPass()
            }
        }
    }

    test("Scrap Trawler dying returns a lesser-mana-value artifact card from the graveyard") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = driver.activePlayer!!

        // Ornithopter, mana value 0, sits in the graveyard as the only legal return target.
        driver.putCardInGraveyard(me, "Ornithopter")
        val trawler = driver.putCreatureOnBattlefield(me, "Scrap Trawler")
        driver.removeSummoningSickness(trawler)

        val handBefore = driver.getHandSize(me)

        // Destroy Scrap Trawler (mana value 3) with Doom Blade — Scrap Trawler is colorless, a
        // legal target. This fixture's `TestCards.DoomBlade` is modelled as a sorcery (see
        // TestCards.kt), so it's cast by `me` — the active player, who alone has priority during
        // their own precombat main with an empty stack — rather than by the opponent; who
        // controls the removal spell doesn't matter for what this test is checking (the trigger
        // on Scrap Trawler's own controller).
        val doomBlade = driver.putCardInHand(me, "Doom Blade")
        driver.giveMana(me, Color.BLACK, 1)
        driver.giveColorlessMana(me, 1)
        driver.submit(
            CastSpell(
                playerId = me,
                cardId = doomBlade,
                targets = listOf(com.wingedsheep.engine.state.components.stack.ChosenTarget.Permanent(trawler)),
                paymentStrategy = PaymentStrategy.FromPool
            )
        ).isSuccess shouldBe true

        drainStack(driver)

        driver.assertInGraveyard(me, "Scrap Trawler")
        driver.getGraveyardCardNames(me).contains("Ornithopter") shouldBe false
        driver.getHandSize(me) shouldBe handBefore + 1
        driver.getHand(me).mapNotNull { driver.getCardName(it) }.contains("Ornithopter") shouldBe true
    }

    test("another artifact dying triggers it too, and only strictly-lesser-mana-value cards are legal targets") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = driver.activePlayer!!

        // Ornithopter (mv 0) is a legal target; Palladium Myr (mv 3) is not, since it is not
        // strictly less than the dying Artifact Creature's mana value (2).
        driver.putCardInGraveyard(me, "Ornithopter")
        driver.putCardInGraveyard(me, "Palladium Myr")

        val trawler = driver.putCreatureOnBattlefield(me, "Scrap Trawler")
        driver.removeSummoningSickness(trawler)
        val otherArtifact = driver.putCreatureOnBattlefield(me, "Artifact Creature")

        val handBefore = driver.getHandSize(me)

        val doomBlade = driver.putCardInHand(me, "Doom Blade")
        driver.giveMana(me, Color.BLACK, 1)
        driver.giveColorlessMana(me, 1)
        driver.submit(
            CastSpell(
                playerId = me,
                cardId = doomBlade,
                targets = listOf(com.wingedsheep.engine.state.components.stack.ChosenTarget.Permanent(otherArtifact)),
                paymentStrategy = PaymentStrategy.FromPool
            )
        ).isSuccess shouldBe true

        drainStack(driver)

        // Scrap Trawler is still alive — it was "another artifact" that died.
        driver.findPermanent(me, "Scrap Trawler") shouldBe trawler
        driver.assertInGraveyard(me, "Artifact Creature")

        // Only the strictly-lesser-mana-value Ornithopter could be chosen; Palladium Myr (mv 3,
        // not less than the dying artifact's mv 2) stays behind.
        driver.getHandSize(me) shouldBe handBefore + 1
        driver.getHand(me).mapNotNull { driver.getCardName(it) }.contains("Ornithopter") shouldBe true
        driver.getGraveyardCardNames(me).contains("Palladium Myr") shouldBe true
    }
})
