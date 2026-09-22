package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Steel Overseer.
 *
 * Steel Overseer ({2}): Artifact Creature — Construct, 1/1
 * "{T}: Put a +1/+1 counter on each artifact creature you control."
 */
class SteelOverseerScenarioTest : ScenarioTestBase() {

    init {
        context("Steel Overseer's tap ability") {

            test("puts a +1/+1 counter on itself and every other artifact creature you control") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Steel Overseer")
                    .withCardOnBattlefield(1, "Ornithopter")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val overseer = game.findPermanent("Steel Overseer")!!
                val ornithopter = game.findPermanent("Ornithopter")!!

                val def = cardRegistry.getCard("Steel Overseer")!!
                val abilityId = def.activatedAbilities.first().id

                val result = game.execute(
                    ActivateAbility(playerId = game.player1Id, sourceId = overseer, abilityId = abilityId)
                )
                withClue("Activation should succeed: ${result.error}") { result.error shouldBe null }
                game.resolveStack()

                val overseerCounters = game.state.getEntity(overseer)
                    ?.get<CountersComponent>()
                    ?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0
                val ornithopterCounters = game.state.getEntity(ornithopter)
                    ?.get<CountersComponent>()
                    ?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0

                withClue("Steel Overseer counts itself as an artifact creature it controls") {
                    overseerCounters shouldBe 1
                }
                withClue("Ornithopter is another artifact creature Player1 controls") {
                    ornithopterCounters shouldBe 1
                }
            }

            test("does not put a counter on a non-artifact creature or an opponent's artifact creature") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Steel Overseer")
                    .withCardOnBattlefield(1, "Grizzly Bears")
                    .withCardOnBattlefield(2, "Ornithopter")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val overseer = game.findPermanent("Steel Overseer")!!
                val bears = game.findPermanent("Grizzly Bears")!!
                val opponentOrnithopter = game.findPermanents("Ornithopter").first { id ->
                    game.state.getEntity(id)?.get<ControllerComponent>()?.playerId == game.player2Id
                }

                val def = cardRegistry.getCard("Steel Overseer")!!
                val abilityId = def.activatedAbilities.first().id

                val result = game.execute(
                    ActivateAbility(playerId = game.player1Id, sourceId = overseer, abilityId = abilityId)
                )
                withClue("Activation should succeed: ${result.error}") { result.error shouldBe null }
                game.resolveStack()

                val bearsCounters = game.state.getEntity(bears)
                    ?.get<CountersComponent>()
                    ?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0
                val opponentCounters = game.state.getEntity(opponentOrnithopter)
                    ?.get<CountersComponent>()
                    ?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0

                withClue("Grizzly Bears is not an artifact creature") { bearsCounters shouldBe 0 }
                withClue("Opponent's artifact creature is not controlled by Player1") { opponentCounters shouldBe 0 }
            }

            test("tapping Steel Overseer to activate the ability taps it") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Steel Overseer")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val overseer = game.findPermanent("Steel Overseer")!!
                val def = cardRegistry.getCard("Steel Overseer")!!
                val abilityId = def.activatedAbilities.first().id

                game.execute(
                    ActivateAbility(playerId = game.player1Id, sourceId = overseer, abilityId = abilityId)
                )
                game.resolveStack()

                withClue("Ability requires tapping Steel Overseer, so it should now be tapped") {
                    (game.state.getEntity(overseer)?.has<TappedComponent>() == true) shouldBe true
                }
            }
        }
    }
}
