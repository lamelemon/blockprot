package io.github.lamelemon.blockprot.events

import io.github.lamelemon.blockprot.BlockProt
import io.github.lamelemon.blockprot.utils.Utils
import jdk.jshell.execution.Util
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.scheduler.BukkitRunnable

// This class is meant to be constructed whenever a player runs the /protectVillager command
class VillagerInteract(val player: Player, timeout: Long, val applyGlow: Boolean): Listener, BukkitRunnable() {

    init {
        Bukkit.getPluginManager().registerEvents(this, BlockProt.instance)
        this.runTaskLater(BlockProt.instance, timeout)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun playerInteractVillagerEvent(event: PlayerInteractEntityEvent) {
        if (event.player != this.player) return
        if (event.isCancelled) return

        val entity: Entity = event.rightClicked
        if (entity.type != EntityType.VILLAGER) return

        val dataContainer = entity.persistentDataContainer
        if (Utils.getOwner(dataContainer) != null) { // Someone already owns the villager
            if (Utils.isOwner(dataContainer, this.player)) { // Owner is player
                Utils.messagePlayer(this.player, "<red>You already own this villager!</red>")
            } else {
                Utils.messagePlayer(this.player, "<red>Someone already owns this villager!</red>")
            }

            this.player.playSound(this.player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
            event.isCancelled = true
            return
        }

        Utils.setOwner(dataContainer, this.player)
        Utils.messagePlayer(this.player, "<green>Registered this villager to you!</green>")
        this.player.playSound(this.player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)

        if (applyGlow) {
            entity.isGlowing = true
            Bukkit.getScheduler().runTaskLater(BlockProt.instance, Runnable{
                entity.isGlowing = false
            }, 20L)
        }

        this.cancel()
    }

    override fun run() {
        Utils.messagePlayer(this.player, "<red>Timed out! You didn't interact with a villager!</red>")
        this.player.playSound(this.player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
        this.cancel()
    }

    override fun cancel() {
        HandlerList.unregisterAll(this)
        super.cancel()
    }
}