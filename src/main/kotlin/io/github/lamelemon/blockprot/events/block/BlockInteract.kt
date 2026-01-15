package io.github.lamelemon.blockprot.events.block

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.github.lamelemon.blockprot.utils.BlockOwnerDialog
import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.TileState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.Locale.getDefault
import java.util.UUID
import org.bukkit.block.data.type.Chest as ChestData


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
        else if (blockState is Chest && (blockState.blockData as ChestData).type != ChestData.Type.SINGLE) {
            // We can make all of these type casts because we know things beforehand that don't seem to be known
            val doubleChest = blockState.inventory.holder as DoubleChest
            val otherChest = if ((blockState.blockData as ChestData).type == ChestData.Type.LEFT) doubleChest.leftSide
            else doubleChest.rightSide

            val otherDataContainer = (otherChest as Chest).persistentDataContainer
            if (!Utils.isAllowedToInteract(otherDataContainer, player)) {
                Utils.setOwner(dataContainer, Utils.getOwner(otherDataContainer) as UUID)
                event.setUseInteractedBlock(Event.Result.DENY)
                Utils.messagePlayer(player,"<red>You are not allowed to interact with this block!</red>")
                return
            } else if (Utils.isOwner(otherDataContainer, player) && !Utils.hasOwner(dataContainer)) {
                Utils.setOwner(dataContainer, player)
                Utils.messagePlayer(player, "<green>Took ownership of ${block.type.name.lowercase().replaceFirstChar { it.titlecase(getDefault()) }}</green>")
            }
        }
        else if (player.isSneaking && player.inventory.itemInMainHand.isEmpty) {
            if (Utils.isOwner(dataContainer, player) || (Utils.hasOwner(dataContainer) && player.hasPermission("blockprot.permission.admin.unlock"))) {
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