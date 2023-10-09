package com.example.kingoftokyo.Entity

import com.example.kingoftokyo.R

class Market(private val deck: MutableList<Card>) {
    private val maxMarketSize = 3
    val availableCards: MutableList<Card> = mutableListOf()

    init {
        refreshMarket()
    }

    fun purchaseCard(card: Card): Boolean {
        if (card !in availableCards) return false

        availableCards.remove(card)
        refreshMarket()

        return true
    }

    private fun refreshMarket() {
        while (availableCards.size < maxMarketSize && deck.isNotEmpty()) {
            val card = deck.removeAt(0)
            availableCards.add(card)
        }
    }



    companion object {
        fun generateDefaultDeck(): MutableList<Card> {
            val defaultDeck = mutableListOf<Card>()

            // Exemple de PowerCard: Giant Leap
            defaultDeck.add(
                PowerCard(
                    name = "Giant Leap",
                    energyCost = 3,
                    effectDescription = "Move out of Tokyo if you are in.",
                    effect = { player ->
                        if (player.isInTokyo) {
                            player.leaveTokyo(Tokyo())
                            println("${player.name} used Giant Leap to leave Tokyo!")
                        }
                    },
                    imageResId = R.drawable.some_image
                )
            )

            // Exemple de ActionCard: Energy Boost
            defaultDeck.add(
                ActionCard(
                    name = "Energy Boost",
                    energyCost = 2,
                    actionDescription = "Gain 3 energy points.",
                    action = { player ->
                        player.energy += 3
                        println("${player.name} used Energy Boost to gain 3 energy points!")
                    },
                    imageResId = R.drawable.some_image
                )
            )

            // Exemple de PowerCard: Mega Punch
            defaultDeck.add(
                PowerCard(
                    name = "Mega Punch",
                    energyCost = 4,
                    effectDescription = "Deal 5 damage to all opponents.",
                    effect = { player ->
                        val opponents = listOf<Player>()
                        opponents.forEach { opponent ->
                            if (opponent != player) {
                                opponent.takeDamage(5)
                            }
                        }
                        println("${player.name} used Mega Punch to deal 5 damage to all opponents!")
                    },
                    imageResId = R.drawable.some_image
                )
            )

            // ... PLus de cartes

            defaultDeck.shuffle()

            return defaultDeck
        }
    }
}