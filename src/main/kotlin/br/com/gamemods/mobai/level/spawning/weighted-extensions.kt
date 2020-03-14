package br.com.gamemods.mobai.level.spawning

import java.util.*

fun Iterable<Weighted>.sum() = sumBy(Weighted::weight)

fun <W: Weighted> List<W>.getByWeight(weight: Int): W? {
    var currentWeight = weight
    for (index in 0 until size) {
        val entry = this[index]
        currentWeight -= entry.weight
        if (currentWeight < 0) {
            return entry
        }
    }
    return null
}

fun <W: Weighted> List<W>.weightedRandom(random: Random): W? {
    val totalWeight = sum()
    if (totalWeight <= 0) {
        return null
    }
    val weight = random.nextInt(totalWeight)
    return getByWeight(weight)
}
