package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.entity.handItems
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.findClosestPlayer
import br.com.gamemods.mobai.math.square
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.item.Item
import cn.nukkit.level.Location
import cn.nukkit.player.Player
import cn.nukkit.utils.Identifier
import kotlin.math.abs

open class TemptGoal<E>(
    val ai: EntityAI<E>,
    val speed: Double,
    val checker: (Item) -> Boolean,
    val canBeScared: Boolean = false
): Goal() where E: BaseEntity, E: SmartEntity {
    private var lastPlayerLocation: Location? = null
    protected var closestPlayer: Player? = null
    private var cooldown = 0
    private var active = false
    constructor(ai: EntityAI<E>, speed: Double, food: Identifier, canBeScared: Boolean = false)
            : this(ai, speed, { it.id == food }, canBeScared)

    init {
        addControls(Control.MOVE, Control.LOOK)
    }

    override fun canStart(): Boolean {
        if (cooldown > 0) {
            cooldown--
            return false
        }
        val entity = ai.entity
        closestPlayer = entity.level.findClosestPlayer(FILTER, entity)
        val closestPlayer = closestPlayer ?: return false
        return closestPlayer.handItems.any(this::isTempedBy)
    }

    protected open fun isTempedBy(item: Item) = checker(item)

    override fun shouldContinue(): Boolean {
        val closestPlayer = closestPlayer ?: return false
        val lastPlayerLocation = lastPlayerLocation ?: return false
        if (!canBeScared) {
            return canStart()
        }

        val entity = ai.entity
        if(entity.distanceSquared(closestPlayer) < 6.square()) {
            if (closestPlayer.distanceSquared(lastPlayerLocation) > 0.1.square()) {
                return false
            }
            if (abs(closestPlayer.pitch - lastPlayerLocation.pitch) > 5.0
                || abs(closestPlayer.yaw - lastPlayerLocation.yaw) > 5.0) {
                return false
            }
            lastPlayerLocation.pitch = closestPlayer.pitch
            lastPlayerLocation.yaw = closestPlayer.yaw
        } else {
            this.lastPlayerLocation = closestPlayer.location
        }

        return canStart()
    }

    override fun start() {
        lastPlayerLocation = checkNotNull(closestPlayer).location.apply {
            yaw = 0.0
            pitch = 0.0
        }
        active = true
    }

    override fun stop() {
        closestPlayer = null
        ai.navigation.stop()
    }

    override fun tick() {
        val closestPlayer = checkNotNull(closestPlayer)
        val entity = ai.entity
        ai.lookControl.lookAt(closestPlayer as Entity, entity.lookYawSpeed + 20, entity.lookPitchSpeed)
        if (entity.distanceSquared(closestPlayer) < 2.5.square()) {
            ai.navigation.stop()
        } else {
            ai.navigation.startMovingTo(closestPlayer as Entity, speed)
        }
    }

    companion object {
        val FILTER = TargetFilter(
            baseMaxDistance = 10.0,
            includeInvulnerable = true,
            includeTeammates = true,
            ignoreEntityTargetRules = true,
            includeHidden = true
        )
    }
}
