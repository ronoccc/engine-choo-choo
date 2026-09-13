package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CompanionRevealedEvent
import com.wingedsheep.engine.core.PayCompanionCost
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.iko.cards.KaheeraTheOrphanguard
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.CardEntry
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Kaheera, the Orphanguard (IKO #224) — {1}{G/W}{G/W} Legendary Creature — Cat Beast, 3/2.
 *
 *   "Companion — Each creature card in your starting deck is a Cat, Elemental, Nightmare,
 *   Dinosaur, or Beast card. ..."
 *   "Vigilance"
 *   "Each other creature you control that's a Cat, Elemental, Nightmare, Dinosaur, or Beast gets
 *   +1/+1 and has vigilance."
 *
 * Two independent halves, tested separately: the deckbuilding/pregame Companion mechanic (CR
 * 702.139, generically covered by CompanionMechanicTest — these tests pin it to Kaheera's actual
 * printed restriction and card data) and the ordinary in-game static abilities (Vigilance plus the
 * lord effect).
 *
 * "Savannah Lions" (Cat) is TestCards' on-type fixture; "Grizzly Bears" (Bear) is off-type.
 */
class KaheeraTheOrphanguardScenarioTest : FunSpec({

    fun driver(): GameTestDriver = GameTestDriver().apply {
        registerCards(TestCards.all + KaheeraTheOrphanguard)
    }

    // Every creature card is Savannah Lions — a Cat, one of Kaheera's five allowed types — so this
    // deck satisfies her restriction.
    val onTypeDeck = Deck(
        cards = List(30) { "Savannah Lions" } + List(30) { "Forest" },
        companion = CardEntry("Kaheera, the Orphanguard"),
    )

    // Grizzly Bears is a Bear, not one of the five allowed types, so this deck fails the
    // restriction — used both as a companion-designating deck that shouldn't reveal, and as a
    // plain opponent deck with no companion at all.
    val offTypeDeckWithCompanion = Deck(
        cards = List(30) { "Grizzly Bears" } + List(30) { "Forest" },
        companion = CardEntry("Kaheera, the Orphanguard"),
    )
    val plainDeck = Deck(cards = List(30) { "Grizzly Bears" } + List(30) { "Forest" })

    test("a deck of on-type (Cat) creatures reveals Kaheera as companion at game setup") {
        val d = driver()
        d.initGame(deck1 = onTypeDeck, deck2 = plainDeck)

        val companionZone = d.state.getZone(ZoneKey(d.player1, Zone.COMPANION))
        companionZone.size shouldBe 1
        d.events.filterIsInstance<CompanionRevealedEvent>().map { it.cardName } shouldBe
            listOf("Kaheera, the Orphanguard")
    }

    test("a deck with an off-type creature (Bear) never reveals Kaheera") {
        val d = driver()
        d.initGame(deck1 = offTypeDeckWithCompanion, deck2 = plainDeck)

        d.state.getZone(ZoneKey(d.player1, Zone.COMPANION)).shouldBeEmpty()
        d.events.filterIsInstance<CompanionRevealedEvent>().shouldBeEmpty()
    }

    test("paying {3} puts the revealed Kaheera into hand from outside the game") {
        val d = driver()
        d.initGame(deck1 = onTypeDeck, deck2 = plainDeck)
        val player = d.player1
        val kaheeraId = d.state.getZone(ZoneKey(player, Zone.COMPANION)).first()

        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        d.giveColorlessMana(player, 3)
        d.submit(PayCompanionCost(player)).isSuccess shouldBe true

        d.state.getZone(ZoneKey(player, Zone.COMPANION)).shouldBeEmpty()
        (kaheeraId in d.state.getZone(ZoneKey(player, Zone.HAND))) shouldBe true
    }

    test("Kaheera's lord effect pumps and grants vigilance to other on-type creatures, but not off-type ones or herself") {
        val d = driver()
        d.initMirrorMatch(deck = Deck.of("Island" to 40), startingLife = 20)
        val controller = d.player1

        val kaheera = d.putCreatureOnBattlefield(controller, "Kaheera, the Orphanguard")
        val onTypeAlly = d.putCreatureOnBattlefield(controller, "Savannah Lions")
        val offTypeAlly = d.putCreatureOnBattlefield(controller, "Grizzly Bears")

        val projected = d.state.projectedState

        // Kaheera herself: base 3/2 — no bonus from her own lord effect (excludeSelf).
        projected.getPower(kaheera) shouldBe 3
        projected.getToughness(kaheera) shouldBe 2
        projected.hasKeyword(kaheera, Keyword.VIGILANCE) shouldBe true

        // On-type ally (Cat): base 1/1 -> 2/2, gains vigilance.
        projected.getPower(onTypeAlly) shouldBe 2
        projected.getToughness(onTypeAlly) shouldBe 2
        projected.hasKeyword(onTypeAlly, Keyword.VIGILANCE) shouldBe true

        // Off-type ally (Bear) is untouched by the lord effect.
        projected.getPower(offTypeAlly) shouldBe 2
        projected.getToughness(offTypeAlly) shouldBe 2
        projected.hasKeyword(offTypeAlly, Keyword.VIGILANCE) shouldBe false
    }
})
