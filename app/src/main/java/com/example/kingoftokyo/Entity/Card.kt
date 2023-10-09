package com.example.kingoftokyo.Entity


enum class CardType {
    POWER, ACTION
}

abstract class Card(
    val name: String,
    val energyCost: Int,
    val cardType: CardType,
    val imageResId: Int
) {
    abstract fun performAction(player: Player): Unit
}

class PowerCard(
    name: String,
    energyCost: Int,
    val effectDescription: String,
    private val effect: (Player) -> Unit,
    imageResId: Int
) : Card(name, energyCost, CardType.POWER, imageResId) {
    override fun performAction(player: Player) {
        effect.invoke(player)
    }
}

class ActionCard(
    name: String,
    energyCost: Int,
    val actionDescription: String,
    private val action: (Player) -> Unit,
    imageResId: Int
) : Card(name, energyCost, CardType.ACTION, imageResId) {
    override fun performAction(player: Player) {
        action.invoke(player)
    }
}
