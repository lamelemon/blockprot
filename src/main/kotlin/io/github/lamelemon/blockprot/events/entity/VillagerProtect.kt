package io.github.lamelemon.blockprot.events.entity

import io.github.lamelemon.blockprot.BlockProt
import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

// This class is meant to be constructed whenever a player runs the /protectVillager command
class VillagerProtect(val player: Player, timeout: Long, val applyGlow: Boolean): Listener, BukkitRunnable() {
    lateinit var clickedVillager: Villager

    init {
        Bukkit.getPluginManager().registerEvents(this, BlockProt.instance)
        this.runTaskLater(BlockProt.instance, timeout)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun playerInteractVillagerEvent(event: PlayerInteractEntityEvent) {
        if (event.player != this.player) return
        if (event.isCancelled) return

        val entity: Entity = event.rightClicked
        if (entity !is Villager) return

        val dataContainer = entity.persistentDataContainer
        if (entity == clickedVillager) {
            Utils.removeOwner(dataContainer)
            Utils.notifyPlayer(this.player, "<green>Successfully removed your ownership of this villager!", Sound.BLOCK_NOTE_BLOCK_PLING)
            applyGlow(entity)
            this.cancel()
            return
        }

        val owner = Utils.getOwner(dataContainer)
        if (owner is UUID) { // Someone already owns the villager
            if (owner == this.player.uniqueId) { // Owner is player
                clickedVillager = entity
                Utils.messagePlayer(this.player, "<red>You already own this villager! Click it again to remove your ownership from it!</red>")
            } else {
                Utils.messagePlayer(this.player, "<red>Someone already owns this villager!</red>")
                event.isCancelled = true
            }

            this.player.playSound(this.player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
            return
        }

        Utils.setOwner(dataContainer, this.player)
        Utils.messagePlayer(this.player, "<green>Registered this villager to you!</green>")
        this.player.playSound(this.player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)

        applyGlow(entity)
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

    fun applyGlow(entity: Entity) {
        if (applyGlow) {
            entity.isGlowing = true
            Bukkit.getScheduler().runTaskLater(BlockProt.instance, Runnable{
                entity.isGlowing = false
            }, 20L)
        }
    }
}