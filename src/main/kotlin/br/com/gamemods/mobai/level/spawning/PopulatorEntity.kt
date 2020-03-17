package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.entity.EntityCategory
import br.com.gamemods.mobai.entity.SpawnType
import br.com.gamemods.mobai.level.CachingChunk
import br.com.gamemods.mobai.level.OnDemandChunkManager
import cn.nukkit.event.entity.CreatureSpawnEvent
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.BedrockRandom
import java.util.concurrent.ThreadLocalRandom

//TODO Safe Delete?
class PopulatorEntity: Populator() {
    override fun populate(chunkManager: ChunkManager, chunkX: Int, chunkZ: Int, bukkitRandom: BedrockRandom?, chunk: IChunk) {
        val level = chunk.level
        val cachedChunk = CachingChunk(chunk)
        val chunkManagerCache = OnDemandChunkManager(level, cachedChunk)
        val spawnLocation = level.spawnLocation
        val random = ThreadLocalRandom.current()
        EntityCategory.values().forEach {
            NaturalSpawnTask.populate(
                chunkManagerCache,
                level,
                it,
                spawnLocation,
                random,
                cachedChunk,
                CreatureSpawnEvent.SpawnReason.NATURAL,
                SpawnType.CHUNK_GENERATION
            )
        }
    }
}
