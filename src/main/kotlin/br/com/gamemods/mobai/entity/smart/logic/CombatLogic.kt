package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.level.doMobLoot
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityOwnable
import cn.nukkit.entity.Projectile
import cn.nukkit.entity.impl.EntityLiving
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.player.Player

interface CombatLogic: SplitLogic {
    fun attack(source: EntityDamageEvent): Boolean {
        despawnCounter = 0
        var entity = (source as? EntityDamageByEntityEvent)?.damager ?: return true
        if (entity is Projectile) {
            entity = entity.shooter ?: return true
        }
        attacker = entity
        lastAttackedTime = base.ticksLived
        return true
    }

    fun canTarget(entity: Entity) = entity is EntityLiving

    fun isTeammate(entity: Entity) = false

    fun onAttacking(target: Entity) {
        attacking = target
        lastAttackTime = base.ticksLived
    }

    fun tryAttack(target: Entity): Boolean { base {
        return true
        /*var bl: Boolean
        var i: Int
        var f = attribute(ATTACK_DAMAGE).value as Float
        var g = 5F //attribute(ATTACK_KNOCKBACK).value
        if (target is EntityLiving) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), (target as LivingEntity).getGroup())
            g += EnchantmentHelper.getKnockback(this) as Float
        }
        if (EnchantmentHelper.getFireAspect(this).also({ i = it }) > 0) {
            target.setOnFireFor(i * 4)
        }
        if (target.damage(DamageSource.mob(this), f).also({ bl = it })) {
            if (g > 0.0f && target is LivingEntity) {
                (target as LivingEntity).takeKnockback(
                    g * 0.5f,
                    MathHelper.sin(this.yaw * 0.017453292f),
                    -MathHelper.cos(this.yaw * 0.017453292f)
                )
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6))
            }
            if (target is PlayerEntity) {
                var playerEntity: PlayerEntity
                this.method_24521(
                    playerEntity,
                    this.getMainHandStack(),
                    if ((target as PlayerEntity?. also {
                            playerEntity = it
                        }).isUsingItem()) playerEntity.getActiveItem() else ItemStack.EMPTY
                )
            }
            this.dealDamage(this, target)
            onAttacking(target)
        }
        return bl*/
    }}

    fun kill() {
        if (!level.doMobLoot) {
            return
        }
        val attacker = attacker
        if (attacker !is Player && (attacker !is EntityOwnable || attacker.owner !is Player)) {
            return
        }

        val range = expDrop.takeUnless { it.isEmpty() || it.last <= 0 } ?: return
        val drops = range.first.coerceAtLeast(0) + random.nextInt(range.last + 1)
        if (drops > 0) {
            entity {
                level.dropExpOrb(position, drops)
            }
        }
    }
}
