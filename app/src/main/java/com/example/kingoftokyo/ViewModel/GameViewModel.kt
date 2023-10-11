package com.example.kingoftokyo.ViewModel

import android.os.Handler
import android.os.Looper
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
    val uiEvent = MutableLiveData<UIEvent>()

    val currentDiceResults = MutableLiveData<List<DiceFace>>(emptyList())

    private var currentRollCount = 0
    private val maxRolls = 3

    init {
        gameState.value = GameState(players, market.availableCards)
    }

    fun startGame() {
        players.shuffle()
        val firstPlayer = players[0]
        gameState.value = gameState.value?.copy(currentTurnPlayer = firstPlayer)

        // Reset roll count for the first player's turn.
        currentRollCount = 0

        if (firstPlayer is IAPlayer) {
            handleIATurn(firstPlayer)
        }
    }

    fun playerLeavesTokyo(player: Player) {
        tokyo.leaveTokyo(player)
        refreshGameState()
    }

    private fun List<DiceFace>.toDiceFaceCount(): Map<DiceFace, Int> {
        return this.groupingBy { it }.eachCount()
    }

    fun rollDiceForPlayer(player: Player, diceToKeep: List<DiceFace>? = null) {
        if (currentRollCount >= maxRolls) {
            // maximum rolls reached.
            nextTurn()
            return
        }

        val dice = Dice()
        val newDiceCount = 6 - (diceToKeep?.size ?: 0)
        val newRolls = List(newDiceCount) { dice.roll() }
        val finalRolls = diceToKeep?.plus(newRolls) ?: newRolls
        val diceFaceCounts = finalRolls.toDiceFaceCount()

        player.energy += DiceUtils.calculateEnergyFromDice(diceFaceCounts)
        player.heal(DiceUtils.calculateHeartsFromDice(diceFaceCounts, player.isInTokyo))
        player.addVictoryPoints(DiceUtils.calculateVictoryPointsFromDice(diceFaceCounts))

        val damage = DiceUtils.calculatePawsFromDice(diceFaceCounts)
        if (damage > 0) {
            player.attack(players - player, damage, tokyo)
        }

        currentRollCount++
        currentDiceResults.value = finalRolls
        refreshGameState()

        // Si c'est un IAPlayer, prenons des décisions pour lui
        if (player is IAPlayer && currentRollCount < maxRolls) {
            val diceDecision = player.decideDiceToKeep(finalRolls)
            rollDiceForPlayer(player, diceDecision)
        } else if (currentRollCount >= maxRolls || (player is IAPlayer && !player.shouldRollAgain(currentDiceResults.value ?: emptyList()))) {
            nextTurn()
        }
    }


    fun rollDiceForCurrentPlayer() {
        val currentPlayer = gameState.value?.currentTurnPlayer ?: return
        rollDiceForPlayer(currentPlayer)
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

    fun handleIATurn(iaPlayer: IAPlayer) {
        uiEvent.postValue(UIEvent.ShowDialog("Tour de ${iaPlayer.name}", "C'est le tour de ${iaPlayer.name}."))

        Handler(Looper.getMainLooper()).postDelayed({
            // Lancer les dés pour l'IA
            rollDiceForPlayer(iaPlayer)
            uiEvent.postValue(UIEvent.ShowDialog("Lancer de dés", "${iaPlayer.name} a lancé les dés et a obtenu ${currentDiceResults.value?.joinToString(", ")}."))

            Handler(Looper.getMainLooper()).postDelayed({
                // Décider si l'IA veut relancer les dés
                if (iaPlayer.shouldRollAgain(currentDiceResults.value ?: emptyList()) && currentRollCount < maxRolls) {
                    val diceDecision = iaPlayer.decideDiceToKeep(currentDiceResults.value ?: emptyList())
                    uiEvent.postValue(UIEvent.ShowDialog("Décision de l'IA", "${iaPlayer.name} a décidé de garder les dés: ${diceDecision.joinToString(", ")}"))

                    Handler(Looper.getMainLooper()).postDelayed({
                        rollDiceForPlayer(iaPlayer, diceDecision)

                        Handler(Looper.getMainLooper()).postDelayed({
                            // 3. Conclure le tour de l'IA après les lancers
                            concludeIATurn(iaPlayer)
                        }, 1000)
                    }, 1000)
                } else {
                    // Si l'IA ne veut pas relancer, conclure son tour
                    concludeIATurn(iaPlayer)
                }
            }, 1000)
        }, 1000)
    }

    private fun concludeIATurn(iaPlayer: IAPlayer) {
        // Décider si l'IA veut quitter Tokyo après avoir été attaquée
        if (iaPlayer.hasBeenAttacked && iaPlayer.decisionToLeaveTokyo()) {
            playerLeavesTokyo(iaPlayer)
            uiEvent.postValue(UIEvent.ShowDialog("Tokyo", "${iaPlayer.name} a décidé de quitter Tokyo."))

            Handler(Looper.getMainLooper()).postDelayed({
                // Décider si l'IA veut acheter une carte
                decideCardPurchaseForIA(iaPlayer)
            }, 1000)
        } else if (iaPlayer.isInTokyo) {
            uiEvent.postValue(UIEvent.ShowDialog("Tokyo", "${iaPlayer.name} est actuellement à Tokyo."))
            Handler(Looper.getMainLooper()).postDelayed({
                nextTurn()
            }, 1000)
        } else {
            if (tokyo.isEmpty) {
                iaPlayer.enterTokyo(tokyo)
                uiEvent.postValue(UIEvent.ShowDialog("Tokyo", "${iaPlayer.name} est entré à Tokyo."))
            }
            decideCardPurchaseForIA(iaPlayer)
        }
    }

    private fun decideCardPurchaseForIA(iaPlayer: IAPlayer) {
        val cardToBuy = iaPlayer.decideCardToBuy(market)
        if (cardToBuy != null) {
            purchaseCardForPlayer(iaPlayer, cardToBuy)
            uiEvent.postValue(UIEvent.ShowDialog("Achat de carte", "${iaPlayer.name} a décidé d'acheter la carte ${cardToBuy.name}."))

            Handler(Looper.getMainLooper()).postDelayed({
                nextTurn()
            }, 1000)
        } else {
            nextTurn()
        }
    }
    fun nextTurn() {
        tokyo.awardVictoryPointsForStayingInTokyo()

        val winner = checkForWinner()
        if (winner != null) {
            endGame(winner)
        } else {
            val nextPlayer = nextPlayer(gameState.value?.currentTurnPlayer)
            gameState.value = gameState.value?.copy(currentTurnPlayer = nextPlayer)

            uiEvent.postValue(UIEvent.ShowDialog("Changement de tour", "C'est le tour de ${nextPlayer.name}.", onDismiss = {

                if (nextPlayer is IAPlayer) {
                    handleIATurn(nextPlayer)
                }
            }))
        }

        // Reset roll count for the next player's turn.
        currentRollCount = 0
    }



    fun endGame(winner: Player) {
        // Update the game state to reflect the winner.
        gameState.value = gameState.value?.copy(winner = winner)
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
