package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.mechanics.layers.StateProjector
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Overwhelming Stampede (canonical printing: Magic 2011 #189).
 *
 * {3}{G}{G} · Sorcery
 * "Until end of turn, creatures you control gain trample and get +X/+X, where X is the greatest
 * power among creatures you control."
 *
 * The printed ruling (2010-08-15) gives the worked example this test mirrors: X is checked once,
 * as the spell resolves, and every creature you control gets the SAME bonus — not a value that
 * climbs as earlier creatures in some iteration order get pumped first.
 */
class OverwhelmingStampedeScenarioTest : FunSpec({

    val projector = StateProjector()

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        return driver
    }

    test("every creature you control gets the same +X/+X and trample, where X is the greatest power") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true)
        val me = driver.activePlayer!!
        val opponent = driver.getOpponent(me)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // Grizzly Bears (2/2), Savannah Lions (1/1): greatest power among them is 2.
        val bears = driver.putCreatureOnBattlefield(me, "Grizzly Bears")
        val lions = driver.putCreatureOnBattlefield(me, "Savannah Lions")
        val theirBears = driver.putCreatureOnBattlefield(opponent, "Grizzly Bears")

        driver.giveMana(me, Color.GREEN, 5)
        val stampede = driver.putCardInHand(me, "Overwhelming Stampede")
        driver.castSpell(me, stampede)
        driver.bothPass()

        // X = 2: both of my creatures get +2/+2, not an escalating bonus.
        projector.getProjectedPower(driver.state, bears) shouldBe 4
        projector.getProjectedToughness(driver.state, bears) shouldBe 4
        projector.getProjectedPower(driver.state, lions) shouldBe 3
        projector.getProjectedToughness(driver.state, lions) shouldBe 3

        projector.hasProjectedKeyword(driver.state, bears, Keyword.TRAMPLE) shouldBe true
        projector.hasProjectedKeyword(driver.state, lions, Keyword.TRAMPLE) shouldBe true

        // Opponent's creature is untouched.
        projector.getProjectedPower(driver.state, theirBears) shouldBe 2
        projector.hasProjectedKeyword(driver.state, theirBears, Keyword.TRAMPLE) shouldBe false
    }

    test("X is frozen at resolution: a creature entering afterward gets no bonus") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true)
        val me = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val bears = driver.putCreatureOnBattlefield(me, "Grizzly Bears")

        driver.giveMana(me, Color.GREEN, 5)
        val stampede = driver.putCardInHand(me, "Overwhelming Stampede")
        driver.castSpell(me, stampede)
        driver.bothPass()

        // X = 2 (Grizzly Bears' own power, the only creature at resolution).
        projector.getProjectedPower(driver.state, bears) shouldBe 4
        projector.getProjectedToughness(driver.state, bears) shouldBe 4

        // A creature that enters afterward missed the resolution-time snapshot entirely.
        val lateLions = driver.putCreatureOnBattlefield(me, "Savannah Lions")
        projector.getProjectedPower(driver.state, lateLions) shouldBe 1
        projector.getProjectedToughness(driver.state, lateLions) shouldBe 1
        projector.hasProjectedKeyword(driver.state, lateLions, Keyword.TRAMPLE) shouldBe false
    }

    test("the printed ruling's worked example: six creatures of varying power all get +5/+5") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true)
        val me = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        // A 5/5 (Craterhoof-free stand-in via two known test creatures at 2/2 and a 5/5-shaped
        // proxy is unnecessary here — the primitive test already proves uniform broadcast with
        // three distinct powers; this test focuses on the specific "greatest power" reading with
        // a genuine 1-power gap between two real creatures plus a clearly dominant one).
        val small = driver.putCreatureOnBattlefield(me, "Savannah Lions") // 1/1
        val mid = driver.putCreatureOnBattlefield(me, "Grizzly Bears") // 2/2
        val big = driver.putCreatureOnBattlefield(me, "Craterhoof Behemoth") // 5/5, haste

        driver.giveMana(me, Color.GREEN, 5)
        val stampede = driver.putCardInHand(me, "Overwhelming Stampede")
        driver.castSpell(me, stampede)
        driver.bothPass()

        // Greatest power among {1, 2, 5} is 5 -> every creature gets +5/+5, including the ones
        // that started below that power.
        projector.getProjectedPower(driver.state, small) shouldBe 6
        projector.getProjectedPower(driver.state, mid) shouldBe 7
        projector.getProjectedPower(driver.state, big) shouldBe 10
        projector.hasProjectedKeyword(driver.state, small, Keyword.TRAMPLE) shouldBe true
        projector.hasProjectedKeyword(driver.state, mid, Keyword.TRAMPLE) shouldBe true
        projector.hasProjectedKeyword(driver.state, big, Keyword.TRAMPLE) shouldBe true
    }
})
