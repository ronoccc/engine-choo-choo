package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.aer.cards.SpireOfIndustry
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Spire of Industry (Aether Revolt #184)
 * Land
 *
 * {T}: Add {C}.
 * {T}, Pay 1 life: Add one mana of any color. Activate only if you control an artifact.
 */
class SpireOfIndustryScenarioTest : FunSpec({

    val colorlessAbilityId = SpireOfIndustry.activatedAbilities[0].id
    val anyColorAbilityId = SpireOfIndustry.activatedAbilities[1].id

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        driver.registerCard(SpireOfIndustry)
        return driver
    }

    test("tapping for the plain ability adds {C} with no life cost") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val you = driver.activePlayer!!
        val spire = driver.putPermanentOnBattlefield(you, "Spire of Industry")
        val beforeLife = driver.getLifeTotal(you)

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = spire, abilityId = colorlessAbilityId)
        )
        result.isSuccess shouldBe true

        driver.isTapped(spire) shouldBe true
        val pool = driver.state.getEntity(you)?.get<ManaPoolComponent>()
        pool?.colorless shouldBe 1
        driver.getLifeTotal(you) shouldBe beforeLife
    }

    test("the any-color ability is illegal without an artifact in play") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val you = driver.activePlayer!!
        val spire = driver.putPermanentOnBattlefield(you, "Spire of Industry")

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = spire, abilityId = anyColorAbilityId, manaColorChoice = Color.BLUE)
        )
        result.isSuccess shouldBe false
        driver.isTapped(spire) shouldBe false
    }

    test("tapping and paying 1 life adds one mana of any color while you control an artifact") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val you = driver.activePlayer!!
        driver.putPermanentOnBattlefield(you, "Artifact Creature")
        val spire = driver.putPermanentOnBattlefield(you, "Spire of Industry")
        val beforeLife = driver.getLifeTotal(you)

        val result = driver.submit(
            ActivateAbility(playerId = you, sourceId = spire, abilityId = anyColorAbilityId, manaColorChoice = Color.BLUE)
        )
        result.isSuccess shouldBe true

        driver.isTapped(spire) shouldBe true
        val pool = driver.state.getEntity(you)?.get<ManaPoolComponent>()
        pool?.blue shouldBe 1
        driver.getLifeTotal(you) shouldBe (beforeLife - 1)
    }
})
