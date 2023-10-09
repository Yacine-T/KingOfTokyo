package com.example.kingoftokyo.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kingoftokyo.Entity.*

class GameViewModel : ViewModel() {
    private val tokyo = Tokyo()
    private val deck = Market.generateDefaultDeck()
    private val market = Market(deck)

    private val human = Player("Human")
    private val ai1 = IAPlayer("AI1")
    private val ai2 = IAPlayer("AI2")
    private val ai3 = IAPlayer("AI3")

    val players: MutableList<Player> = mutableListOf(human, ai1, ai2, ai3)

    val gameState = MutableLiveData<GameState>()

    private var currentRollCount = 0
    private val maxRolls = 3

    init {
        gameState.value = GameState(players, market.availableCards)
    }

    fun rollDiceForPlayer(player: Player) {
        if (currentRollCount >= maxRolls) {
            // maximum rolls reached.
            return
        }

        val dice = Dice()
        val results = List(6) { dice.roll() }
        val interpretedResults = DiceUtils.interpretDiceResults(results)

        player.energy += DiceUtils.calculateEnergyFromDice(interpretedResults)
        player.heal(DiceUtils.calculateHeartsFromDice(interpretedResults, player.isInTokyo))
        player.addVictoryPoints(DiceUtils.calculateVictoryPointsFromDice(interpretedResults))

        val damage = DiceUtils.calculatePawsFromDice(interpretedResults)
        if (damage > 0) {
            player.attack(players - player, damage, tokyo)
        }

        refreshGameState()
        currentRollCount++
    }


    fun purchaseCardForPlayer(player: Player, card: Card) {
        if (player.energy < card.energyCost) {
            //  Error- insufficient energy.
            return
        }

        if (player.energy >= card.energyCost && market.purchaseCard(card)) {
            player.energy -= card.energyCost
            player.addCard(card)
            refreshGameState()
        }
    }

    private fun refreshGameState() {
        gameState.value = GameState(players, market.availableCards)
    }

    fun checkForWinner(): Player? {
        val alivePlayers = players.filter { it.isAlive }

        if (alivePlayers.size == 1) {
            return alivePlayers[0]
        }

        val playersWith20Points = players.filter { it.victoryPoints >= 20 }
        return playersWith20Points.maxByOrNull { it.victoryPoints }
    }

    fun nextTurn() {
        tokyo.awardVictoryPointsForStayingInTokyo()

        val winner = checkForWinner()
        if (winner != null) {
            gameState.value = gameState.value?.copy(winner = winner)
        } else {
            gameState.value = gameState.value?.copy(currentTurnPlayer = nextPlayer(gameState.value?.currentTurnPlayer))
        }

        // Reset roll count for the next player's turn.
        currentRollCount = 0
    }

    private fun nextPlayer(currentPlayer: Player?): Player {
        val currentIndex = players.indexOf(currentPlayer)
        tokyo.awardVictoryPointsForStayingInTokyo()
        // Reset roll count for the next player's turn.
        currentRollCount = 0

        return if (currentIndex == -1 || currentIndex == players.size - 1) {
            players[0]
        } else {
            players[currentIndex + 1]
        }
    }


    fun playerAttacks(attacker: Player, opponents: List<Player>, diceResults: List<DiceFace>, tokyo: Tokyo) {
        val damage = DiceUtils.calculatePawsFromDice(DiceUtils.interpretDiceResults(diceResults))
        val eliminatedPlayers = attacker.attack(opponents, damage, tokyo)

        // Award for eliminating players
        if (eliminatedPlayers.isNotEmpty()) {
            attacker.addVictoryPoints(2 * eliminatedPlayers.size)
            println("${attacker.name} earned ${2 * eliminatedPlayers.size} victory points for eliminating players!")
        }

        // Notify players about the attack
        println("${attacker.name} dealt $damage damage!")
        for (player in opponents) {
            if (!player.isAlive) {
                println("${player.name} has been eliminated!")
            } else {
                println("${player.name} has ${player.health} health remaining.")
            }
        }

    }

}

data class GameState(
    val players: List<Player>,
    val availableCards: List<Card>,
    val currentTurnPlayer: Player? = null,
    val winner: Player? = null
)
