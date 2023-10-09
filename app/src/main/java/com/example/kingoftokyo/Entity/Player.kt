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
    val imageResId: Int = R.drawable.default_image
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
                    if (!opponent.isAlive) {
                        eliminatedPlayers.add(opponent)
                    }
                }
            }
        } else {
            tokyo.currentPlayerInTokyo?.takeDamage(damage)
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
        // Leave Tokyo if health is below 5 or if AI has more than 15 victory points to protect its lead
        return health < 5 || victoryPoints > 15
    }
}
