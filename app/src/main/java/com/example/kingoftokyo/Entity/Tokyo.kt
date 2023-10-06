package com.example.kingoftokyo.Entity

class Tokyo {
    var city: Player? = null
    val isEmpty: Boolean
        get() = city == null

    fun enterTokyo(player: Player) {
        if (city == null) {
            city = player
            player.addVictoryPoints(1)
            println("${player.name} has entered Tokyo!")
        }
    }

    fun leaveTokyo(player: Player) {
        if (city == player) {
            city = null
            println("${player.name} has left Tokyo!")
        }
    }

    fun awardVictoryPointsForStayingInTokyo() {
        city?.addVictoryPoints(2) //2 points pour commencer un tour à Tokyo.
    }
}

