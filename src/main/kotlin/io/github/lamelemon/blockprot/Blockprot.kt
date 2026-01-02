package io.github.lamelemon.blockprot

import io.github.lamelemon.blockprot.commands.Friends
import io.github.lamelemon.blockprot.commands.ProtectVillager
import io.github.lamelemon.blockprot.events.block.BlockInteract
import io.github.lamelemon.blockprot.events.block.BlockPlace
import io.github.lamelemon.blockprot.events.entity.VillagerInteract
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class BlockProt : JavaPlugin() {

    companion object {
        lateinit var instance: BlockProt
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

        registerCommand("friends", config.getStringList("friends.command-aliases"), Friends())

        if (config.getBoolean("villagers.enabled", true)) {
            pluginManager.registerEvents(VillagerInteract(), this)

            registerCommand("protectVillager",
                config.getStringList("villagers.command-aliases"),
                ProtectVillager(
                    config.getLong("villagers.timeout", 10L) * 20,
                    config.getBoolean("villagers.apply-glow", true)
                )
            )
        }
    }
}
