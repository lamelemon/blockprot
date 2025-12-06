package io.github.lamelemon.blockprot

import io.github.lamelemon.blockprot.events.BlockInteract
import io.github.lamelemon.blockprot.events.BlockPlace
import io.github.lamelemon.blockprot.utils.utils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Blockprot : JavaPlugin() {
    companion object {
        lateinit var instance: Blockprot
    }

    override fun onEnable() {
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            saveResource("config.yml", true)
        }
        val config = YamlConfiguration.loadConfiguration(configFile)

        val pluginManager = Bukkit.getPluginManager()
        if (!config.getBoolean("enabled")) {
            pluginManager.disablePlugin(this)
        }

        instance = this
        utils.functionalMaterials = config.getStringList("functional-materials").mapNotNullTo(HashSet()) { Material.getMaterial(it) }

        pluginManager.registerEvents(BlockPlace(), this)
        pluginManager.registerEvents(BlockInteract(), this)
    }
}
