package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CompanionRevealedEvent
import com.wingedsheep.engine.core.PayCompanionCost
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.CompanionAbility
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EveryCreatureCardHasSubtype
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Tests for the Companion mechanic (CR 702.139, Ikoria: Lair of Behemoths).
 *
 * CR text pinned by these tests:
 *  - **702.139a**: "Before the game begins, you may reveal one card you own from outside the
 *    game with a companion ability whose condition is fulfilled by your starting deck. ...
 *    Once during the game, any time you have priority and the stack is empty, but only during a
 *    main phase of your turn, you may pay {3} and put that card into your hand. This is a
 *    special action that doesn't use the stack (see rule 116.2g)."
 *  - **103.2b**: revealing a companion is optional, capped at one, and gated on the condition
 *    being fulfilled; the card "remains outside the game" either way.
 *  - **116.2g**: the fetch is a special action, available "any time [the player] has priority and
 *    the stack is empty during a main phase of their turn, but only if they haven't done so yet
 *    this game."
 *
 * A Kaheera-shaped restriction (Scryfall oracle text) is used as the test fixture:
 * "Each creature card in your starting deck is a [Cat/Elemental/Nightmare/Dinosaur/Beast] card."
 */
class CompanionMechanicTest : FunSpec({

    val allowedTypes = setOf(Subtype.CAT, Subtype.ELEMENTAL, Subtype.NIGHTMARE, Subtype.DINOSAUR, Subtype("Beast"))

    val testCompanion = card("Test Orphanguard") {
        manaCost = "{1}{G/W}{G/W}"
        typeLine = "Legendary Creature — Cat Beast"
        power = 3
        toughness = 2
    }.copy(companion = CompanionAbility(EveryCreatureCardHasSubtype(allowedTypes)))

    val onTypeCreature = card("Test On-Type Cat") {
        manaCost = "{1}{G}"
        typeLine = "Creature — Cat"
        power = 2
        toughness = 2
    }

    val offTypeCreature = card("Test Off-Type Human") {
        manaCost = "{1}{W}"
        typeLine = "Creature — Human Soldier"
        power = 2
        toughness = 2
    }

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(testCompanion)
        driver.registerCard(onTypeCreature)
        driver.registerCard(offTypeCreature)
        return driver
    }

    val legalDeck = Deck(
        cards = List(20) { "Test On-Type Cat" } + List(20) { "Forest" },
        companion = com.wingedsheep.sdk.model.CardEntry("Test Orphanguard"),
    )
    val illegalDeck = Deck(
        cards = List(19) { "Test On-Type Cat" } + "Test Off-Type Human" + List(20) { "Forest" },
        companion = com.wingedsheep.sdk.model.CardEntry("Test Orphanguard"),
    )
    val noCompanionDeck = Deck(cards = List(20) { "Test On-Type Cat" } + List(20) { "Forest" })

    test("a companion whose restriction the starting deck satisfies is revealed into the companion zone at setup") {
        val driver = createDriver()
        driver.initGame(deck1 = legalDeck, deck2 = noCompanionDeck)

        val companionZone = driver.state.getZone(ZoneKey(driver.player1, Zone.COMPANION))
        companionZone.size shouldBe 1

        val revealed = driver.events.filterIsInstance<CompanionRevealedEvent>()
        revealed.size shouldBe 1
        revealed.first().playerId shouldBe driver.player1
        revealed.first().cardName shouldBe "Test Orphanguard"

        // It is not in the library or hand — CR 702.139a puts it in exactly one place pregame.
        val companionId = companionZone.first()
        (companionId in driver.state.getZone(ZoneKey(driver.player1, Zone.HAND))) shouldBe false
        (companionId in driver.state.getZone(ZoneKey(driver.player1, Zone.LIBRARY))) shouldBe false
    }

    test("a companion whose restriction the starting deck violates is never revealed") {
        val driver = createDriver()
        driver.initGame(deck1 = illegalDeck, deck2 = noCompanionDeck)

        driver.state.getZone(ZoneKey(driver.player1, Zone.COMPANION)).shouldBeEmpty()
        driver.events.filterIsInstance<CompanionRevealedEvent>().shouldBeEmpty()
    }

    test("a player with no companion designated reveals nothing") {
        val driver = createDriver()
        driver.initGame(deck1 = noCompanionDeck, deck2 = noCompanionDeck)

        driver.state.getZone(ZoneKey(driver.player1, Zone.COMPANION)).shouldBeEmpty()
        driver.state.getZone(ZoneKey(driver.player2, Zone.COMPANION)).shouldBeEmpty()
    }

    test("paying {3} during a main phase with priority and an empty stack puts the companion into hand") {
        val driver = createDriver()
        driver.initGame(deck1 = legalDeck, deck2 = noCompanionDeck)
        val player = driver.player1
        val companionId = driver.state.getZone(ZoneKey(player, Zone.COMPANION)).first()

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.giveColorlessMana(player, 3)

        val result = driver.submit(PayCompanionCost(player))
        result.isSuccess shouldBe true

        driver.state.getZone(ZoneKey(player, Zone.COMPANION)).shouldBeEmpty()
        (companionId in driver.state.getZone(ZoneKey(player, Zone.HAND))) shouldBe true
    }

    test("the companion can't be paid for without enough mana") {
        val driver = createDriver()
        driver.initGame(deck1 = legalDeck, deck2 = noCompanionDeck)
        val player = driver.player1

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        // No mana given — paying {3} must fail.
        val result = driver.submit(PayCompanionCost(player))
        result.isSuccess shouldBe false

        driver.state.getZone(ZoneKey(player, Zone.COMPANION)).size shouldBe 1
    }

    test("the companion can't be fetched a second time in the same game") {
        val driver = createDriver()
        driver.initGame(deck1 = legalDeck, deck2 = noCompanionDeck)
        val player = driver.player1

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.giveColorlessMana(player, 3)
        driver.submit(PayCompanionCost(player)).isSuccess shouldBe true

        // Zone is empty now, so a second attempt has nothing to fetch (CR 702.139a: "once during
        // the game").
        driver.giveColorlessMana(player, 3)
        val second = driver.submit(PayCompanionCost(player))
        second.isSuccess shouldBe false
    }

    test("the companion can't be paid for outside a main phase") {
        val driver = createDriver()
        driver.initGame(deck1 = legalDeck, deck2 = noCompanionDeck)
        val player = driver.player1

        // Still in the UNTAP/UPKEEP/DRAW steps of the beginning phase — not a main phase.
        driver.giveColorlessMana(player, 3)
        val result = driver.submit(PayCompanionCost(player))
        result.isSuccess shouldBe false
        driver.state.getZone(ZoneKey(player, Zone.COMPANION)).size shouldBe 1
    }
})
