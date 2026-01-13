package io.github.lamelemon.blockprot.events.block

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.github.lamelemon.blockprot.utils.BlockOwnerDialog
import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.Locale.getDefault


class BlockInteract: Listener {

    @EventHandler
    fun blockInteract(event: PlayerInteractEvent) {
        if (event.useInteractedBlock() == Event.Result.DENY) return

        val block = event.clickedBlock
        if (block !is Block) return

        val blockState = block.state
        if (blockState !is TileState) return
        if (Utils.isIgnored(blockState.type)) return

        val player = event.player
        val dataContainer = blockState.persistentDataContainer
        if (!Utils.isAllowedToInteract(dataContainer, player)) {
            event.setUseInteractedBlock(Event.Result.DENY)
            Utils.messagePlayer(player,"<red>You are not allowed to interact with this block!</red>")
        }
        else if (player.isSneaking && player.inventory.itemInMainHand.isEmpty) {
            if (Utils.isOwner(dataContainer, player)) {
                event.setUseInteractedBlock(Event.Result.DENY)
                BlockOwnerDialog(player, block, blockState)
            }
            else if (!Utils.hasOwner(dataContainer)) {
                Utils.setOwner(blockState, player)
                Utils.messagePlayer(player, "<green>Took ownership of ${block.type.name.lowercase().replaceFirstChar { it.titlecase(getDefault()) }}</green>")
            }
        }
    }

    @EventHandler
    fun blockDestroy(event: BlockDestroyEvent) {
        if (event.isCancelled) return

        val blockState = event.block.state
        if (blockState !is TileState) return

        if (Utils.hasOwner(blockState.persistentDataContainer)) {
            event.isCancelled = true
        }
    }
}