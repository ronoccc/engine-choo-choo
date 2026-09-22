package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.c14.cards.CommandersSphere
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Format
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Commander's Sphere (Commander 2014 #54)
 * {3} · Artifact
 *
 * {T}: Add one mana of any color in your commander's color identity.
 * Sacrifice this artifact: Draw a card.
 */
class CommandersSphereScenarioTest : FunSpec({

    val manaAbilityId = CommandersSphere.activatedAbilities[0].id
    val sacrificeAbilityId = CommandersSphere.activatedAbilities[1].id

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(CommandersSphere)
        return driver
    }

    test("tapping for mana adds one mana of the controller's commander's color identity") {
        val driver = createDriver()
        // "Centaur Courser" ({2}{G}) has a green color identity.
        val players = driver.initMultiplayer(
            decks = listOf(Deck.of("Plains" to 40), Deck.of("Plains" to 40)),
            format = Format.Commander(),
            commanders = listOf("Centaur Courser", "Centaur Courser"),
        )
        val you = players[0]
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val sphere = driver.putPermanentOnBattlefield(you, "Commander's Sphere")

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = sphere, abilityId = manaAbilityId, manaColorChoice = Color.GREEN)
        )
        result.isSuccess shouldBe true

        driver.isTapped(sphere) shouldBe true
        val pool = driver.state.getEntity(you)?.get<ManaPoolComponent>()
        pool?.green shouldBe 1
    }

    test("mana ability produces no mana outside your commander's color identity") {
        val driver = createDriver()
        val players = driver.initMultiplayer(
            decks = listOf(Deck.of("Plains" to 40), Deck.of("Plains" to 40)),
            format = Format.Commander(),
            commanders = listOf("Centaur Courser", "Centaur Courser"),
        )
        val you = players[0]
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val sphere = driver.putPermanentOnBattlefield(you, "Commander's Sphere")

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = sphere, abilityId = manaAbilityId, manaColorChoice = Color.RED)
        )
        // AddManaOfChoiceExecutor: a mana ability can't pause to ask again once activated, so an
        // illegal color pick for a constrained set (commander identity, here just green) falls
        // back to the sole available color instead of failing the activation outright — see its
        // "an illegal pick falls back to the first available color" doc comment. The activation
        // still succeeds; it just produces green, not red.
        result.isSuccess shouldBe true
        val pool = driver.state.getEntity(you)?.get<ManaPoolComponent>()
        pool?.green shouldBe 1
        pool?.red shouldBe 0
    }

    test("sacrificing Commander's Sphere draws a card") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val you = driver.activePlayer!!
        val sphere = driver.putPermanentOnBattlefield(you, "Commander's Sphere")
        val beforeHand = driver.getHandSize(you)

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = sphere, abilityId = sacrificeAbilityId)
        )
        result.isSuccess shouldBe true
        // Sacrifice is paid at activation, but the draw effect is the ability itself, and a
        // non-mana activated ability goes on the stack rather than resolving immediately (CR
        // 602.2a) — it needs priority passed to resolve.
        driver.bothPass()

        driver.getHandSize(you) shouldBe beforeHand + 1
        driver.findPermanent(you, "Commander's Sphere") shouldBe null
    }

    test("sacrificing Commander's Sphere works even while it's tapped") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val you = driver.activePlayer!!
        val sphere = driver.putPermanentOnBattlefield(you, "Commander's Sphere")
        driver.tapPermanent(sphere)

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = sphere, abilityId = sacrificeAbilityId)
        )
        result.isSuccess shouldBe true
        driver.findPermanent(you, "Commander's Sphere") shouldBe null
    }
})
