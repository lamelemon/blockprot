package io.github.lamelemon.blockprot.events.block

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.github.lamelemon.blockprot.utils.BlockOwnerDialog
import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.TileState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.Locale.getDefault
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
            return
        }

        if (blockState is Chest && (blockState.blockData as ChestData).type != ChestData.Type.SINGLE) {
            // We can make all of these type casts because we know things beforehand that don't seem to be known
            val doubleChest = blockState.inventory.holder as DoubleChest
            val otherChest: Chest = (if ((blockState.blockData as ChestData).type == ChestData.Type.LEFT) doubleChest.leftSide
            else doubleChest.rightSide) as Chest

            val otherDataContainer = otherChest.persistentDataContainer
            val otherChestOwner = Utils.getOwner(otherDataContainer)
            if (!Utils.isAllowedToInteract(otherDataContainer, player)) {
                Utils.setOwner(dataContainer, otherChestOwner as UUID)
                event.setUseInteractedBlock(Event.Result.DENY)
                Utils.messagePlayer(player,"<red>You are not allowed to interact with this block!</red>")
                return
            }

            val interactedChestOwner = Utils.getOwner(dataContainer)
            val interactedChestUnOwned = interactedChestOwner !is UUID
            val otherChestUnOwned = otherChestOwner !is UUID
            val playerID = player.uniqueId
            when {
                interactedChestUnOwned && otherChestUnOwned -> {
                    Utils.messagePlayer(player, "<green>Took ownership of ${block.type.name.lowercase().replaceFirstChar { it.titlecase(getDefault()) }}</green>")
                    Utils.setOwner(blockState, player)
                    Utils.setOwner(otherChest, player)
                }
                interactedChestUnOwned && playerID == otherChestOwner -> Utils.setOwner(otherChest, player)
                otherChestUnOwned && playerID == interactedChestOwner -> Utils.setOwner(blockState, player)
            }
        }

        if (player.isSneaking && player.inventory.itemInMainHand.isEmpty) {
            if (Utils.isOwner(dataContainer, player) || (Utils.hasOwner(dataContainer) && player.hasPermission("blockprot.permission.admin.unlock"))) {
                player.closeInventory()
                BlockOwnerDialog(player, block, blockState)
                event.setUseInteractedBlock(Event.Result.DENY)
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
        if (Utils.isIgnored(blockState.type)) return

        if (Utils.hasOwner(blockState.persistentDataContainer)) event.isCancelled = true
    }

    @EventHandler
    fun blockBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val blockState = event.block.state
        if (blockState !is TileState) return
        if (Utils.isIgnored(blockState.type)) return

        if (Utils.hasOwner(blockState.persistentDataContainer) && !Utils.isOwner(blockState.persistentDataContainer, event.player)) {
            event.isCancelled = true
            Utils.notifyPlayer(event.player, "<red>You are not allowed to break this block!</red>", Sound.BLOCK_NOTE_BLOCK_PLING)
        }
    }

    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        if (event.isCancelled) return

        val blockState = event.block.state
        if (blockState !is TileState) return
        if (Utils.isIgnored(blockState.type)) return

        if (Utils.hasOwner(blockState.persistentDataContainer)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun blockExplode(event: BlockExplodeEvent) {
        if (event.isCancelled) return
        filterExplosion(event.blockList())
    }

    @EventHandler
    fun entityExplode(event: EntityExplodeEvent) {
        if (event.isCancelled) return
        filterExplosion(event.blockList())
    }

    private fun filterExplosion(blocks: MutableList<Block>) {
        blocks.removeIf { block ->
            block.state is TileState &&
                    !Utils.isIgnored(block.type) &&
                    Utils.hasOwner((block.state as TileState).persistentDataContainer)
        }
    }
}