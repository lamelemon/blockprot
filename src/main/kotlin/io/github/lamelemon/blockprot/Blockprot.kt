package io.github.lamelemon.blockprot

import org.bukkit.plugin.java.JavaPlugin

class Blockprot : JavaPlugin() {
    companion object {
        lateinit var instance: Blockprot
    }

    override fun onEnable() {
        instance = this
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
