package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.rna.cards.Bedevil
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Bedevil {B}{B}{R} Instant (Ravnica Allegiance canonical).
 *
 * Destroy target artifact, creature, or planeswalker.
 */
class BedevilScenarioTest : FunSpec({

    val TestCreature = CardDefinition.creature(
        name = "Test Grizzly",
        manaCost = ManaCost.parse("{1}{G}"),
        subtypes = emptySet(),
        power = 2,
        toughness = 2
    )

    val TestArtifact = CardDefinition.artifact(
        name = "Test Rock",
        manaCost = ManaCost.parse("{2}")
    )

    val TestPlaneswalker = CardDefinition.planeswalker(
        name = "Test Walker",
        manaCost = ManaCost.parse("{2}{U}"),
        subtypes = emptySet(),
        startingLoyalty = 4
    )

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(Bedevil, TestCreature, TestArtifact, TestPlaneswalker))
        return driver
    }

    fun payBedevil(driver: GameTestDriver, you: com.wingedsheep.sdk.model.EntityId) {
        driver.giveMana(you, Color.BLACK, 2)
        driver.giveMana(you, Color.RED, 1)
    }

    test("destroys a creature") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        val you = driver.activePlayer!!
        val opp = driver.getOpponent(you)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val creature = driver.putCreatureOnBattlefield(opp, "Test Grizzly")
        payBedevil(driver, you)
        val bedevil = driver.putCardInHand(you, "Bedevil")

        driver.castSpellWithTargets(you, bedevil, listOf(ChosenTarget.Permanent(creature))).isSuccess shouldBe true
        driver.bothPass()

        driver.findPermanent(opp, "Test Grizzly") shouldBe null
    }

    test("destroys an artifact") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        val you = driver.activePlayer!!
        val opp = driver.getOpponent(you)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val rock = driver.putPermanentOnBattlefield(opp, "Test Rock")
        payBedevil(driver, you)
        val bedevil = driver.putCardInHand(you, "Bedevil")

        driver.castSpellWithTargets(you, bedevil, listOf(ChosenTarget.Permanent(rock))).isSuccess shouldBe true
        driver.bothPass()

        driver.findPermanent(opp, "Test Rock") shouldBe null
    }

    test("destroys a planeswalker") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        val you = driver.activePlayer!!
        val opp = driver.getOpponent(you)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val walker = driver.putPermanentOnBattlefield(opp, "Test Walker")
        payBedevil(driver, you)
        val bedevil = driver.putCardInHand(you, "Bedevil")

        driver.castSpellWithTargets(you, bedevil, listOf(ChosenTarget.Permanent(walker))).isSuccess shouldBe true
        driver.bothPass()

        driver.findPermanent(opp, "Test Walker") shouldBe null
    }

    test("cannot target an illegal permanent, e.g. a land") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Swamp" to 40), startingLife = 20)
        val you = driver.activePlayer!!
        val opp = driver.getOpponent(you)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val land = driver.putPermanentOnBattlefield(opp, "Swamp")
        payBedevil(driver, you)
        val bedevil = driver.putCardInHand(you, "Bedevil")

        val result = driver.submit(
            CastSpell(
                playerId = you,
                cardId = bedevil,
                targets = listOf(ChosenTarget.Permanent(land)),
                paymentStrategy = PaymentStrategy.FromPool
            )
        )
        result.isSuccess shouldBe false
        driver.findPermanent(opp, "Swamp") shouldBe land
    }
})
