package io.github.lamelemon.blockprot.events.entity

import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

class VillagerInteract: Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun playerInteractVillagerEvent(event: PlayerInteractEntityEvent) {
        if (event.isCancelled) return

        val villager = event.rightClicked
        if (villager !is Villager) return

        val player = event.player
        if (!Utils.isAllowedToInteract(villager.persistentDataContainer, player)) {
            Utils.messagePlayer(player, "<red>You are not allowed to interact with this villager!</red>")
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
            event.isCancelled = true
            return
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun villagerTakeDamageEvent(event: EntityDamageEvent) {
        if (event.isCancelled) return

        val villager = event.entity
        if (villager !is Villager) return

        val dataContainer = villager.persistentDataContainer
        if (!Utils.hasOwner(dataContainer)) return

        val player = event.damageSource.causingEntity
        if (player !is Player) {
            event.isCancelled = true
            return
        }

        if (!Utils.isAllowedToInteract(dataContainer, player)) {
            Utils.messagePlayer(player, "<red>You are not allowed to damage this villager!</red>")
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
            event.isCancelled = true
        }
    }
}