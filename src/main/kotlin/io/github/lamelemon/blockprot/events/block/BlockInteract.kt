package io.github.lamelemon.blockprot.events.block

import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.TileState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class BlockInteract: Listener {

    @EventHandler
    fun blockInteract(event: PlayerInteractEvent) {

        val block = event.clickedBlock
        if (block !is Block) return

        val blockState = block.state
        if (blockState !is TileState) return
        if (Utils.isIgnored(blockState.type)) return

        val player = event.player
        if (!Utils.isAllowedToInteract(blockState.persistentDataContainer, player)) {
            event.setUseInteractedBlock(Event.Result.DENY)
            Utils.messagePlayer(player,"<red>You are not allowed to interact with this block!</red>")
        }
    }
}