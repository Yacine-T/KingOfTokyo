package com.example.kingoftokyo.Entity


enum class DiceFace {
    ONE, TWO, THREE, PAW, HEART, ENERGY
}

class Dice {
    fun roll(): DiceFace {
        return DiceFace.values().random()
    }
}

object DiceUtils {
    fun interpretDiceResults(results: List<DiceFace>): Map<DiceFace, Int> {
        return results.groupingBy { it }.eachCount()
    }

    fun calculateVictoryPointsFromDice(results: Map<DiceFace, Int>): Int {
        var points = 0
        for (entry in results) {
            if (entry.key == DiceFace.ONE && entry.value >= 3) {
                points += 1 + (entry.value - 3)
            }
            if (entry.key == DiceFace.TWO && entry.value >= 3) {
                points += 2 + 2 * (entry.value - 3)
            }
            if (entry.key == DiceFace.THREE && entry.value >= 3) {
                points += 3 + 3 * (entry.value - 3)
            }
        }
        return points
    }

    fun calculateEnergyFromDice(results: Map<DiceFace, Int>): Int {
        return results[DiceFace.ENERGY] ?: 0
    }

    fun calculateHeartsFromDice(results: Map<DiceFace, Int>, isInTokyo: Boolean): Int {
        return if (isInTokyo) 0 else (results[DiceFace.HEART] ?: 0)
    }

    fun calculatePawsFromDice(results: Map<DiceFace, Int>): Int {
        return results[DiceFace.PAW] ?: 0
    }

}
