package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.AbilityCost
import com.wingedsheep.sdk.scripting.AdditionalCostPayment
import com.wingedsheep.sdk.scripting.costs.CostAtom
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * End-to-end rules coverage for Halo Fountain (SNC) and the [CostAtom.UntapPermanents] cost
 * primitive it introduces — the untap-cost twin of [CostAtom.TapPermanents]. Halo Fountain has
 * three abilities, each "{mana}, {T}, Untap N tapped creatures you control: <effect>", so it
 * exercises the primitive at N = 1 and N = 2 directly, plus the CR 122.1d stun-counter interaction
 * that [com.wingedsheep.engine.core.untapOrConsumeStun] is the shared chokepoint for.
 */
class HaloFountainScenarioTest : ScenarioTestBase() {

    private fun haloFountainAbilityId(untapCount: Int) =
        cardRegistry.getCard("Halo Fountain")!!.activatedAbilities.first { ability ->
            val composite = ability.cost as? AbilityCost.Composite ?: return@first false
            composite.costs.any {
                val atom = (it as? AbilityCost.Atom)?.atom
                atom is CostAtom.UntapPermanents && atom.count == untapCount
            }
        }.id

    private fun grantMana(game: ScenarioTestBase.TestGame, playerId: EntityId, white: Int) {
        game.state = game.state.updateEntity(playerId) { it.with(ManaPoolComponent(white = white)) }
    }

    init {
        context("Halo Fountain (SNC) — {W}, {T}, Untap a tapped creature you control: Create a Citizen") {

            test("untapping one tapped creature pays the cost and creates a 1/1 GW Citizen") {
                val game = scenario()
                    .withPlayers()
                    .withCardOnBattlefield(1, "Halo Fountain", summoningSickness = false)
                    .withCardOnBattlefield(1, "Grizzly Bears", tapped = true, summoningSickness = false)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val fountain = game.findPermanent("Halo Fountain")!!
                val bears = game.findPermanent("Grizzly Bears")!!
                grantMana(game, game.player1Id, white = 1)

                val result = game.execute(
                    ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = fountain,
                        abilityId = haloFountainAbilityId(untapCount = 1),
                        costPayment = AdditionalCostPayment(untappedPermanents = listOf(bears))
                    )
                )
                withClue("activation should succeed: ${result.error}") { result.error shouldBe null }
                game.resolveStack()

                withClue("Halo Fountain itself is tapped to pay {T}") {
                    game.state.getEntity(fountain)?.has<TappedComponent>() shouldBe true
                }
                withClue("the untapped creature is actually untapped") {
                    game.state.getEntity(bears)?.has<TappedComponent>() shouldBe false
                }

                val citizen = game.findPermanent("Citizen")
                withClue("a Citizen token was created") { citizen shouldNotBe null }
                val citizenColors = game.state.getEntity(citizen!!)?.get<CardComponent>()?.colors
                withClue("Citizen is green and white") {
                    citizenColors shouldBe setOf(Color.GREEN, Color.WHITE)
                }
            }

            test("with only 1 tapped creature, the level-2 (untap two) ability cannot be paid") {
                val game = scenario()
                    .withPlayers()
                    .withCardOnBattlefield(1, "Halo Fountain", summoningSickness = false)
                    .withCardOnBattlefield(1, "Grizzly Bears", tapped = true, summoningSickness = false)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val fountain = game.findPermanent("Halo Fountain")!!
                val bears = game.findPermanent("Grizzly Bears")!!
                grantMana(game, game.player1Id, white = 2)

                val result = game.execute(
                    ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = fountain,
                        abilityId = haloFountainAbilityId(untapCount = 2),
                        costPayment = AdditionalCostPayment(untappedPermanents = listOf(bears))
                    )
                )
                withClue("only one tapped creature exists — the untap-2 cost cannot be paid") {
                    result.error shouldNotBe null
                }
            }

            test("untapping two tapped creatures pays the cost and draws a card") {
                val game = scenario()
                    .withPlayers()
                    .withCardOnBattlefield(1, "Halo Fountain", summoningSickness = false)
                    .withCardOnBattlefield(1, "Grizzly Bears", tapped = true, summoningSickness = false)
                    .withCardOnBattlefield(1, "Spined Wurm", tapped = true, summoningSickness = false)
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val fountain = game.findPermanent("Halo Fountain")!!
                val bears = game.findPermanent("Grizzly Bears")!!
                val wurm = game.findPermanent("Spined Wurm")!!
                grantMana(game, game.player1Id, white = 2)

                val handSizeBefore = game.state.getZone(
                    com.wingedsheep.engine.state.ZoneKey(game.player1Id, com.wingedsheep.sdk.core.Zone.HAND)
                ).size

                val result = game.execute(
                    ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = fountain,
                        abilityId = haloFountainAbilityId(untapCount = 2),
                        costPayment = AdditionalCostPayment(untappedPermanents = listOf(bears, wurm))
                    )
                )
                withClue("activation should succeed: ${result.error}") { result.error shouldBe null }
                game.resolveStack()

                withClue("both creatures were untapped to pay the cost") {
                    game.state.getEntity(bears)?.has<TappedComponent>() shouldBe false
                    game.state.getEntity(wurm)?.has<TappedComponent>() shouldBe false
                }
                val handSizeAfter = game.state.getZone(
                    com.wingedsheep.engine.state.ZoneKey(game.player1Id, com.wingedsheep.sdk.core.Zone.HAND)
                ).size
                withClue("a card was drawn") { handSizeAfter shouldBe handSizeBefore + 1 }
            }

            test("untapping a creature with a stun counter removes the counter instead (CR 122.1d), and still pays the cost") {
                val game = scenario()
                    .withPlayers()
                    .withCardOnBattlefield(1, "Halo Fountain", summoningSickness = false)
                    .withCardOnBattlefield(1, "Grizzly Bears", tapped = true, summoningSickness = false)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val fountain = game.findPermanent("Halo Fountain")!!
                val bears = game.findPermanent("Grizzly Bears")!!
                grantMana(game, game.player1Id, white = 1)
                game.state = game.state.updateEntity(bears) { container ->
                    val existing = container.get<CountersComponent>() ?: CountersComponent()
                    container.with(existing.withAdded(CounterType.STUN, 1))
                }

                val result = game.execute(
                    ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = fountain,
                        abilityId = haloFountainAbilityId(untapCount = 1),
                        costPayment = AdditionalCostPayment(untappedPermanents = listOf(bears))
                    )
                )
                withClue("the cost is paid even though the stun counter replaces the untap: ${result.error}") {
                    result.error shouldBe null
                }
                game.resolveStack()

                withClue("the stun counter was removed instead of the creature untapping") {
                    game.state.getEntity(bears)?.get<CountersComponent>()?.getCount(CounterType.STUN) shouldBe 0
                }
                withClue("the creature never actually untapped") {
                    game.state.getEntity(bears)?.has<TappedComponent>() shouldBe true
                }
                withClue("the ability still resolved — a Citizen token was created") {
                    game.findPermanent("Citizen") shouldNotBe null
                }
            }
        }

        context("Halo Fountain (SNC) — {W}{W}{W}{W}{W}, {T}, Untap fifteen tapped creatures you control: You win the game") {

            test("untapping fifteen tapped creatures wins the game") {
                val builder = scenario().withPlayers()
                    .withCardOnBattlefield(1, "Halo Fountain", summoningSickness = false)
                repeat(15) {
                    builder.withCardOnBattlefield(1, "Grizzly Bears", tapped = true, summoningSickness = false)
                }
                val game = builder
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val fountain = game.findPermanent("Halo Fountain")!!
                val bearsIds = game.state.getBattlefield(game.player1Id).filter { id ->
                    game.state.getEntity(id)?.get<CardComponent>()?.name == "Grizzly Bears"
                }
                bearsIds shouldHaveSize 15
                grantMana(game, game.player1Id, white = 5)

                val result = game.execute(
                    ActivateAbility(
                        playerId = game.player1Id,
                        sourceId = fountain,
                        abilityId = haloFountainAbilityId(untapCount = 15),
                        costPayment = AdditionalCostPayment(untappedPermanents = bearsIds)
                    )
                )
                withClue("activation should succeed: ${result.error}") { result.error shouldBe null }
                game.resolveStack()

                withClue("the controller wins the game") {
                    game.state.gameOver shouldBe true
                    game.state.winnerId shouldBe game.player1Id
                }
            }
        }
    }
}
