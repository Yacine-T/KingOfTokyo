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

    val isAlive: Boolean
        get() = health > 0

    fun useCard(card: Card) {
        if (card !in cards) {
            println("You don't have this card.")
            return
        }

        if (energy >= card.energyCost) {
            card.performAction(this)
            energy -= card.energyCost
        } else {
            println("Not enough energy to use this card.")
        }
    }

    fun addCard(card: Card) {
        if (card in cards) {
            println("You already have this card.")
            return
        }

        cards.add(card)
    }

    fun removeCard(card: Card) {
        if (card !in cards) {
            println("You don't have this card.")
            return
        }

        cards.remove(card)
    }

    fun takeDamage(damage: Int) {
        if (damage < 0) {
            println("Invalid damage amount.")
            return
        }

        health -= damage
        if (health < 0) health = 0
    }

    fun heal(amount: Int) {
        if (amount < 0) {
            println("Invalid healing amount.")
            return
        }

        if (isInTokyo) {
            println("Cannot heal while in Tokyo!")
            return
        }

        health += amount
        if (health > 10) health = 10
    }

    fun addVictoryPoints(points: Int) {
        if (points < 0) {
            println("Invalid points.")
            return
        }
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

    fun attack(opponents: List<Player>, damage: Int, tokyo: Tokyo) {
        if (damage < 0) {
            println("Invalid damage amount.")
            return
        }

        if (isInTokyo) {
            for (opponent in opponents) {
                if (!opponent.isInTokyo) {
                    opponent.takeDamage(damage)
                }
            }
        } else {
            tokyo.currentPlayerInTokyo?.takeDamage(damage)

            if (tokyo.currentPlayerInTokyo?.isAlive == false || tokyo.currentPlayerInTokyo?.decisionToLeaveTokyo() == true) {
                tokyo.leaveTokyo(tokyo.currentPlayerInTokyo!!)
                enterTokyo(tokyo)
            }
        }
    }

    open fun decisionToLeaveTokyo(): Boolean {
        println("$name, you've been attacked in Tokyo. Do you want to leave Tokyo? (yes/no)")
        val answer = readLine()
        return answer?.toLowerCase() == "yes"
    }

}

class IAPlayer(name: String) : Player(name, imageResId = R.drawable.ia_image, isHuman = false) {
    override fun decisionToLeaveTokyo(): Boolean {
        //
        if (health < 5) {
            println("$name has decided to leave Tokyo.")
            return true
        }
        return false
    }
}
