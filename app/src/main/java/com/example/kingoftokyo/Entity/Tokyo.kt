package com.example.kingoftokyo.Entity

class Tokyo {
    var currentPlayerInTokyo: Player? = null
    val isEmpty: Boolean
        get() = currentPlayerInTokyo == null

    fun enterTokyo(player: Player) {
        if (currentPlayerInTokyo == null) {
            currentPlayerInTokyo = player
            player.addVictoryPoints(1) // 1 point pour entrer dans tokyo
            println("${player.name} has entered Tokyo!")
        }
    }

    fun leaveTokyo(player: Player) {
        if (currentPlayerInTokyo == player) {
            currentPlayerInTokyo = null
            println("${player.name} has left Tokyo!")
        }
    }

    fun awardVictoryPointsForStayingInTokyo() {
        currentPlayerInTokyo?.addVictoryPoints(2) //2 points pour commencer un tour à Tokyo.
    }
}

