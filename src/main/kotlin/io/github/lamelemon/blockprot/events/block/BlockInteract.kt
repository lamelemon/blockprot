package io.github.lamelemon.blockprot.events

import io.github.lamelemon.blockprot.utils.Utils.functionalMaterials
import io.github.lamelemon.blockprot.utils.Utils.isAllowedToInteract
import io.github.lamelemon.blockprot.utils.Utils.messagePlayer
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataHolder

class BlockInteract: Listener {

    @EventHandler
    fun blockInteract(event: PlayerInteractEvent) {

        val block = event.clickedBlock
        if (block !is Block) return

        if (!functionalMaterials.contains(block.type)) return

        val blockState = block.state
        if (blockState !is PersistentDataHolder) return

        if (!isAllowedToInteract(blockState.persistentDataContainer, event.player)) {
            event.setUseInteractedBlock(Event.Result.DENY)
            messagePlayer(event.player, "Not allowed to interact with this block!")
        }
    }
}