package br.com.gamemods.mobai.ai.pathing

enum class PathNodeType(val defaultPenalty: Float = 0F) {
    BLOCKED(-1F),
    OPEN,
    WALKABLE,
    TRAPDOOR,
    FENCE(-1F),
    LAVA(-1F),
    WATER(8F),
    WATER_BORDER(8F),
    RAIL,
    DANGER_FIRE(8F),
    DAMAGE_FIRE(16F),
    DANGER_CACTUS(8F),
    DAMAGE_CACTUS(-1F),
    DANGER_OTHER(8F),
    DAMAGE_OTHER(-1F),
    DOOR_OPEN,
    DOOR_WOOD_CLOSED(-1F),
    DOOR_IRON_CLOSED(-1F),
    BREACH(4F),
    LEAVES(-1F),
    STICKY_HONEY(8F),
    COCOA(0F)
}
