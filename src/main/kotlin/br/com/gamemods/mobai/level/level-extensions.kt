package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.ai.filter.TargetFilter
import cn.nukkit.entity.Entity
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.Vector3f
import cn.nukkit.player.Player
import kotlin.reflect.KClass

fun Level.findClosestPlayer(filter: TargetFilter, cause: Entity?, position: Vector3f): Player? {
    return findClosestEntity(players.values, filter, cause, position)
}

fun Level.findClosestEntity(
    typeFilter: KClass<out Entity>?,
    targetFilter: TargetFilter,
    cause: Entity?,
    position: Vector3f,
    bb: AxisAlignedBB
): Entity? {
    return findClosestEntity(getCollidingEntities(bb).run {
        if (typeFilter != null) {
            filter { typeFilter.java.isInstance(it) }
        } else {
            this
        }
    }, targetFilter, cause, position)
}

private fun <T : Entity> findClosestEntity(
    entityList: Collection<T>,
    targetPredicate: TargetFilter,
    entity: Entity?,
    position: Vector3f
): T? {
    var d = -1.0
    var livingEntity: T? = null
    val var13: Iterator<T> = entityList.iterator()
    while (true) {
        var livingEntity2: T
        var e: Double
        do {
            do {
                if (!var13.hasNext()) {
                    return livingEntity
                }
                livingEntity2 = var13.next()
            } while (!targetPredicate.test(entity, livingEntity2))
            e = livingEntity2.position.distanceSquared(position)
        } while (d != -1.0 && e >= d)
        d = e
        livingEntity = livingEntity2
    }
}
