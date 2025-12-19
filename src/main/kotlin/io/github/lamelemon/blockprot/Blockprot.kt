package io.github.lamelemon.blockprot

import io.github.lamelemon.blockprot.commands.Friends
import io.github.lamelemon.blockprot.events.BlockInteract
import io.github.lamelemon.blockprot.events.BlockPlace
import org.bukkit.Bukkit
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

        pluginManager.registerEvents(BlockPlace(), this)
        pluginManager.registerEvents(BlockInteract(), this)

        registerCommand("friends", HashSet<String>(setOf("friend")), Friends())
    }
}
