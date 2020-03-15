package br.com.gamemods.mobai

import br.com.gamemods.mobai.ExtraAttributeIds.UNDERWATER_MOVEMENT
import br.com.gamemods.mobai.entity.EntityCategory
import br.com.gamemods.mobai.entity.attribute.AttributeRegistry.getIdOrRegister
import br.com.gamemods.mobai.entity.registerEntities
import br.com.gamemods.mobai.level.Dimension
import br.com.gamemods.mobai.level.dimensionType
import br.com.gamemods.mobai.level.feature.FeatureRegistry
import br.com.gamemods.mobai.level.feature.registerFeatures
import br.com.gamemods.mobai.level.spawning.LevelSettings
import br.com.gamemods.mobai.level.spawning.NaturalSpawnTask
import br.com.gamemods.mobai.level.spawning.registerVanillaBiomes
import br.com.gamemods.mobai.level.spawning.registerVanillaDimensions
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import cn.nukkit.event.level.LevelLoadEvent
import cn.nukkit.event.level.LevelSaveEvent
import cn.nukkit.event.level.LevelUnloadEvent
import cn.nukkit.event.server.RegistriesClosedEvent
import cn.nukkit.level.Level
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.ConfigSection

@Suppress("unused")
class MobAIPlugin: PluginBase() {
    override fun onEnable() {
        INSTANCE = this
        NaturalSpawnTask.logger = logger

        saveDefaultConfig()

        registerAttributes()
        registerVanillaDimensions()
        registerVanillaBiomes()
        registerFeatures()
        registerEntities()

        server.levels.forEach(this::onLevelLoad)

        server.scheduler.scheduleRepeatingTask(this, NaturalSpawnTask, 1)
        server.pluginManager.registerEvents(LevelPersistenceListener(), this)

        server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onRegistryClose(ev: RegistriesClosedEvent) {
                assert(!ev.isCancelled)
                FeatureRegistry.close()
                HandlerList.unregisterAll(this)
            }
        }, this)
    }

    override fun onDisable() {
        server.levels.forEach(this::onLevelUnload)
    }

    private fun registerAttributes() {
        logger.debug("Registering attributes")
        UNDERWATER_MOVEMENT = getIdOrRegister("minecraft:underwater_movement", 0F, 340282346638528859811704183484516925440.00f, 0.02F)
    }

    private inline fun <reified T: Enum<T>> nullableEnumValueOf(name: String): T? {
        return try {
            enumValueOf<T>(name)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun loadSettingsSection(section: ConfigSection): LevelSettings {
        return with(section) {
            LevelSettings(
                difficulty = getInt("difficulty", -1),
                spawnAnimals = getInheritableBoolean("spawn-animals"),
                spawnMonsters = getInheritableBoolean("spawn-monsters")
            ).apply {
                section.getSection("custom-category-caps").forEach { categoryName, customCap ->
                    if (customCap !is Int) return@forEach
                    val category = nullableEnumValueOf<EntityCategory>(categoryName.toUpperCase()) ?: return@forEach
                    customCategoryCaps[category] = customCap
                }
            }
        }
    }

    private fun saveLevelSettings(section: ConfigSection, settings: LevelSettings) {
        section["difficulty"] = settings.difficulty
        section["spawn-animals"] = settings.spawnAnimals?.toString() ?: "inherit"
        section["spawn-monsters"] = settings.spawnMonsters?.toString() ?: "inherit"
        val categoryCaps = ConfigSection()
        settings.customCategoryCaps.forEach { (category, cap) ->
            categoryCaps[category.name.toLowerCase()] = cap
        }
        section["custom-category-caps"] = categoryCaps
    }

    private fun loadDefaultLevelSettings() {
        val group = config.getSection("level-settings")
        LevelSettings.fallback = loadSettingsSection(group.getSection("fallback"))
        group.getSection("dimension-default")
            .mapNotNull { Dimension(it.key.toIntOrNull() ?: return@mapNotNull null) to (it.value as? ConfigSection ?: return@mapNotNull null) }
            .map { (dim, conf) -> dim to loadSettingsSection(conf) }
            .onEach { (dim, conf) -> LevelSettings.defaultByDimension[dim] = conf }
            .map { (dim) -> dim }
            .toSet()
            .also { LevelSettings.defaultByDimension.keys.retainAll(it) }
    }

    private fun saveDefaultLevelSettings() {
        val fallbackSection = config.rootSection.createPath("level-settings", "fallback")
        saveLevelSettings(fallbackSection, LevelSettings.fallback)
        val dimensionSections = config.rootSection.createPath("level-settings", "dimension-default")
        dimensionSections.clear()
        LevelSettings.defaultByDimension.forEach { (dim, settings) ->
            val section = ConfigSection()
            saveLevelSettings(section, settings)
            dimensionSections[dim.toString()] = section
        }
        saveConfig()
    }

    private fun loadLevelSettings(level: Level): LevelSettings {
        val section = config.getSection("level-settings.level").getSection(level.id)
        if (section.isEmpty()) {
            return LevelSettings.getEffective(level)
        }
        return loadSettingsSection(section)
    }

    private fun onLevelLoad(level: Level) {
        LevelSettings -= level
        LevelSettings[level] = loadLevelSettings(level)
    }

    private fun onLevelUnload(level: Level) {
        LevelSettings -= level
    }

    private fun onLevelSave(level: Level) {
        val currentConfig = LevelSettings[level] ?: return
        val parentConfig = LevelSettings.defaultByDimension[level.dimensionType] ?: LevelSettings.fallback
        if (currentConfig == parentConfig) {
            val configs = config.getSection("level-settings.level")
            if (level.id in configs) {
                configs.remove(level.id)
                saveConfig()
            }
            return
        }

        val section = config.rootSection.createPath("level-settings", "level", level.id)
        saveLevelSettings(section, currentConfig)
        saveConfig()
    }

    private tailrec fun ConfigSection.createPath(vararg path: String, pos: Int = 0): ConfigSection {
        if (pos >= path.size) {
            return this
        }
        val current = path[pos]
        if (current !in this || get(current) !is ConfigSection) {
            put(current, ConfigSection())
        }
        return getSection(current).createPath(*path, pos = pos + 1)
    }

    private fun ConfigSection.getInheritableBoolean(name: String): Boolean? {
        val value = getString(name).takeUnless {
            it.isBlank() || it == "-1" || it.equals("inherit", ignoreCase = true)
        }?.trim() ?: return null
        return value == "1" || value.toBoolean()
    }

    private inner class LevelPersistenceListener: Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        fun onLevelLoad(ev: LevelLoadEvent) {
            onLevelLoad(ev.level)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onLevelUnload(ev: LevelUnloadEvent) {
            onLevelUnload(ev.level)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onLevelSave(ev: LevelSaveEvent) {
            onLevelSave(ev.level)
        }
    }

    companion object {
        internal lateinit var INSTANCE: MobAIPlugin
    }
}
