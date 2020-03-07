package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.math.ZERO_3F
import br.com.gamemods.mobai.math.offset
import br.com.gamemods.mobai.math.times
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f

interface MoveLogic: Traveller, EntityProperties {
    private inline val base get() = this as BaseEntity
    private inline val smart get() = this as SmartEntity

    fun moveToBoundingBoxCenter() { base.apply {
        val box = boundingBox
        setPosition(Vector3f((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0))
    }}

    override fun move(cause: MoveCause, movement: Vector3f) { base.apply {  smart.apply {
        if (this.noClip) {
            boundingBox.offset(movement)
            this.moveToBoundingBoxCenter()
            return
        }

        var mov = movement
        if (movementMultiplier.lengthSquared() > 1.0E-7) {
            mov = movement * movementMultiplier
            movementMultiplier = ZERO_3F
            motion = ZERO_3F
        }

        println("MV: $mov")

        base.move(mov.x, mov.y, mov.z)

        //val velocityMultiplier = velocityMultiplier.toDouble()
        //motion = motion.multiply(velocityMultiplier, 1.0, velocityMultiplier)
    }}}

/*
    override fun move(type: MoveCause, movement: Vector3f) { base.apply {  smart.apply {
        movement2 = adjustMovementForSneaking(movement2, type)
        val vec3d: Vec3d = this.adjustMovementForCollisions(movement2)
        if (vec3d.lengthSquared() > 1.0E-7) {
            this.setBoundingBox(this.getBoundingBox().offset(vec3d))
            this.moveToBoundingBoxCenter()
        }
        this.world.getProfiler().pop()
        this.world.getProfiler().push("rest")
        this.horizontalCollision =
            !MathHelper.approximatelyEquals(movement2.x, vec3d.x) || !MathHelper.approximatelyEquals(
                movement2.z,
                vec3d.z
            )
        this.verticalCollision = movement2.y !== vec3d.y
        this.onGround = this.verticalCollision && movement2.y < 0.0
        this.collided = this.horizontalCollision || this.verticalCollision
        val blockPos: BlockPos = this.getLandingPos()
        val blockState: BlockState = this.world.getBlockState(blockPos)
        this.fall(vec3d.y, this.onGround, blockState, blockPos)
        val vec3d2: Vec3d = this.getVelocity()
        if (movement2.x !== vec3d.x) {
            this.setVelocity(0.0, vec3d2.y, vec3d2.z)
        }
        if (movement2.z !== vec3d.z) {
            this.setVelocity(vec3d2.x, vec3d2.y, 0.0)
        }
        val block: Block = blockState.getBlock()
        if (movement2.y !== vec3d.y) {
            block.onEntityLand(this.world, this)
        }
        if (this.onGround && !this.bypassesSteppingEffects()) {
            block.onSteppedOn(this.world, blockPos, this)
        }
        if (this.canClimb() && !this.hasVehicle()) {
            val d: Double = vec3d.x
            var e: Double = vec3d.y
            val f: Double = vec3d.z
            if (block !== Blocks.LADDER && block !== Blocks.SCAFFOLDING) {
                e = 0.0
            }
            this.horizontalSpeed = (this.horizontalSpeed as Double + MathHelper.sqrt(
                net.minecraft.entity.Entity.squaredHorizontalLength(vec3d)
            ) as Double * 0.6).toFloat()
            this.distanceTraveled =
                (this.distanceTraveled as Double + MathHelper.sqrt(d * d + e * e + f * f) as Double * 0.6).toFloat()
            if (this.distanceTraveled > this.nextStepSoundDistance && !blockState.isAir()) {
                this.nextStepSoundDistance = this.calculateNextStepSoundDistance()
                if (this.isTouchingWater()) {
                    val entity: net.minecraft.entity.Entity =
                        if (this.hasPassengers() && this.getPrimaryPassenger() != null) this.getPrimaryPassenger() else this
                    val g = if (entity === this) 0.35f else 0.4f
                    val vec3d3: Vec3d = entity.getVelocity()
                    var h: Float =
                        MathHelper.sqrt(vec3d3.x * vec3d3.x * 0.20000000298023224 + vec3d3.y * vec3d3.y + vec3d3.z * vec3d3.z * 0.20000000298023224) * g
                    if (h > 1.0f) {
                        h = 1.0f
                    }
                    this.playSwimSound(h)
                } else {
                    this.playStepSound(blockPos, blockState)
                }
            } else if (this.distanceTraveled > this.nextFlySoundDistance && this.hasWings() && blockState.isAir()) {
                this.nextFlySoundDistance = this.playFlySound(this.distanceTraveled)
            }
        }
        try {
            this.inLava = false
            this.checkBlockCollision()
        } catch (var18: Throwable) {
            val crashReport: CrashReport = CrashReport.create(var18, "Checking entity block collision")
            val crashReportSection: CrashReportSection =
                crashReport.addElement("Entity being checked for collision")
            this.populateCrashReport(crashReportSection)
            throw CrashException(crashReport)
        }
        this.setVelocity(
            this.getVelocity().multiply(
                this.getVelocityMultiplier() as Double,
                1.0,
                this.getVelocityMultiplier() as Double
            )
        )
        if (!this.world.doesAreaContainFireSource(this.getBoundingBox().contract(0.001)) && this.fireTicks <= 0) {
            this.fireTicks = -this.getBurningDuration()
        }
        if (this.isWet() && this.isOnFire()) {
            this.playSound(
                SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                0.7f,
                1.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f
            )
            this.fireTicks = -this.getBurningDuration()
        }
        this.world.getProfiler().pop()
    }}}

    fun adjustMovementForPiston(movement: Vector3f): Vector3f {
        //TODO
        return movement
    }

    fun adjustMovementForSneaking(movement: Vector3f, type: MoveCause): Vector3f {
        return movement
    }

    fun adjustMovementForCollisions(movement: Vector3f): Vector3f { base.apply {
        val box = boundingBox
        val entityContext: EntityContext = EntityContext.of(this)
        val voxelShape: VoxelShape = this.world.getWorldBorder().asVoxelShape()
        val stream: Stream<VoxelShape> = if (VoxelShapes.matchesAnywhere(
                voxelShape,
                VoxelShapes.cuboid(box.contract(1.0E-7)),
                BooleanBiFunction.AND
            )
        ) Stream.empty<VoxelShape>() else Stream.of<VoxelShape>(voxelShape)
        val stream2: Stream<VoxelShape> = this.world.getEntityCollisions(this, box.stretch(movement), ImmutableSet.of())
        val reusableStream: ReusableStream<VoxelShape> = ReusableStream(Stream.concat(stream2, stream))
        val vec3d: Vec3d =
            if (movement.lengthSquared() === 0.0) movement else net.minecraft.entity.Entity.adjustMovementForCollisions(
                this,
                movement,
                box,
                this.world,
                entityContext,
                reusableStream
            )
        val bl = movement.x !== vec3d.x
        val bl2 = movement.y !== vec3d.y
        val bl3 = movement.z !== vec3d.z
        val bl4 = this.onGround || bl2 && movement.y < 0.0
        if (stepHeight > 0.0f && bl4 && (bl || bl3)) {
            var vec3d2: Vec3d = net.minecraft.entity.Entity.adjustMovementForCollisions(
                this,
                Vec3d(movement.x, stepHeight.toDouble(), movement.z),
                box,
                this.world,
                entityContext,
                reusableStream
            )
            val vec3d3: Vec3d = net.minecraft.entity.Entity.adjustMovementForCollisions(
                this,
                Vec3d(0.0, stepHeight.toDouble(), 0.0),
                box.stretch(movement.x, 0.0, movement.z),
                this.world,
                entityContext,
                reusableStream
            )
            if (vec3d3.y < stepHeight.toDouble()) {
                val vec3d4: Vec3d = net.minecraft.entity.Entity.adjustMovementForCollisions(
                    this,
                    Vec3d(movement.x, 0.0, movement.z),
                    box.offset(vec3d3),
                    this.world,
                    entityContext,
                    reusableStream
                ).add(vec3d3)
                if (net.minecraft.entity.Entity.squaredHorizontalLength(vec3d4) > net.minecraft.entity.Entity.squaredHorizontalLength(
                        vec3d2
                    )
                ) {
                    vec3d2 = vec3d4
                }
            }
            if (net.minecraft.entity.Entity.squaredHorizontalLength(vec3d2) > net.minecraft.entity.Entity.squaredHorizontalLength(
                    vec3d
                )
            ) {
                return vec3d2.add(
                    net.minecraft.entity.Entity.adjustMovementForCollisions(
                        this,
                        Vec3d(0.0, -vec3d2.y + movement.y, 0.0),
                        box.offset(vec3d2),
                        this.world,
                        entityContext,
                        reusableStream
                    )
                )
            }
        }
        return vec3d
    }}*/
}
