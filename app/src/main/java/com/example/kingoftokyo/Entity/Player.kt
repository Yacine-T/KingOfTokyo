package com.example.kingoftokyo.Entity

import com.example.kingoftokyo.R

open class Player(
    val name: String,
    var energy: Int = 0,
    var health: Int = 10,
    var victoryPoints: Int = 0,
    var isHuman: Boolean = true,
    var isInTokyo: Boolean = false,
    private val cards: MutableList<Card> = mutableListOf(),
    val imageResId: Int = R.drawable.default_image,
    var hasBeenAttacked: Boolean = false

) {
    val isAlive: Boolean get() = health > 0

    fun useCard(card: Card): Boolean {
        if (card !in cards || energy < card.energyCost) return false
        card.performAction(this)
        energy -= card.energyCost
        return true
    }

    fun addCard(card: Card): Boolean {
        if (card in cards) return false
        cards.add(card)
        return true
    }

    fun removeCard(card: Card): Boolean {
        if (card !in cards) return false
        cards.remove(card)
        return true
    }

    fun takeDamage(damage: Int) {
        if (damage < 0) return
        health = (health - damage).coerceAtLeast(0)
    }

    fun heal(amount: Int) {
        if (amount < 0 || isInTokyo) return
        health = (health + amount).coerceAtMost(10)
    }

    fun addVictoryPoints(points: Int) {
        if (points < 0) return
        victoryPoints += points
    }

    fun enterTokyo(tokyo: Tokyo) {
        if (!isInTokyo && tokyo.isEmpty) {
            tokyo.enterTokyo(this)
            isInTokyo = true
        }
    }

    fun leaveTokyo(tokyo: Tokyo) {
        if (isInTokyo) {
            tokyo.leaveTokyo(this)
            isInTokyo = false
        }
    }

    open fun decisionToLeaveTokyo(): Boolean = false

    fun attack(opponents: List<Player>, damage: Int, tokyo: Tokyo): List<Player> {
        if (damage < 0) return emptyList()

        val eliminatedPlayers = mutableListOf<Player>()

        if (isInTokyo) {
            for (opponent in opponents) {
                if (!opponent.isInTokyo) {
                    opponent.takeDamage(damage)
                    opponent.hasBeenAttacked = true
                    if (!opponent.isAlive) {
                        eliminatedPlayers.add(opponent)
                    }
                }
            }
        } else {
            tokyo.currentPlayerInTokyo?.takeDamage(damage)
            tokyo.currentPlayerInTokyo?.hasBeenAttacked = true
            if (tokyo.currentPlayerInTokyo?.isAlive == false || tokyo.currentPlayerInTokyo?.decisionToLeaveTokyo() == true) {
                tokyo.leaveTokyo(tokyo.currentPlayerInTokyo!!)
                enterTokyo(tokyo)
            }
        }


        return eliminatedPlayers
    }

}

class IAPlayer(name: String) : Player(name, imageResId = R.drawable.ia_image, isHuman = false) {
    override fun decisionToLeaveTokyo(): Boolean {
        if (health < 3) return true
        if (victoryPoints > 15 && health < 6) return true
        // Adding a small randomness 10%
        return (0..9).random() == 0
    }

    fun decideCardToBuy(market: Market): Card? {
        val affordableCards = market.availableCards.filter { it.energyCost <= this.energy }
        if (affordableCards.isEmpty()) return null

        if (this.health < 5) {
            val healthBoostingCard = affordableCards.find { card -> card.name.toLowerCase().contains("heal") }
            if (healthBoostingCard != null) return healthBoostingCard
        }

        if (this.victoryPoints > 15) {
            val victoryBoostingCard = affordableCards.find { card -> card.name.toLowerCase().contains("victory") }
            if (victoryBoostingCard != null) return victoryBoostingCard
        }

        return affordableCards.random()
    }


    fun decideDiceToKeep(diceResults: List<DiceFace>): List<DiceFace> {
        //  l'IA a moins de 5 points de vie, garder les cœurs
        if (this.health < 5) {
            return diceResults.filter { it == DiceFace.HEART }
        }

        // si IA beaucoup d'énergie,  garder les griffes pour attaquer
        if (this.energy > 5) {
            return diceResults.filter { it == DiceFace.PAW }
        }

        // Sinon,  garder les dés qui ont le plus haut nombre
        val counts = diceResults.groupingBy { it }.eachCount()
        val maxCount = counts.maxByOrNull { it.value }?.key
        return diceResults.filter { it == maxCount }
    }


}
