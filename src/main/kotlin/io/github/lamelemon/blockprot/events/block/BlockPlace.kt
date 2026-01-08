package io.github.lamelemon.blockprot.events.block

import io.github.lamelemon.blockprot.BlockProt.Companion.instance
import io.github.lamelemon.blockprot.utils.Utils
import jdk.jshell.execution.Util
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.type.Chest as ChestData
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.InventoryHolder

class BlockPlace: Listener {

    @EventHandler
    fun blockPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        if (!event.canBuild()) return

        val block = event.blockPlaced
        val blockState = block.state
        if (blockState !is TileState) return
        if (Utils.isIgnored(blockState.type)) return

        Utils.setOwner(blockState.persistentDataContainer, event.player)
        blockState.update()

        // Check for double chests
        Bukkit.getScheduler().runTaskLater(instance, Runnable{
            if (event.isCancelled) return@Runnable
            if (blockState !is Chest) return@Runnable
            if ((blockState.blockData as ChestData).type == ChestData.Type.SINGLE) return@Runnable

            val doubleChest = blockState.inventory.holder
            if (doubleChest !is DoubleChest) return@Runnable

            // Slightly confusing, this is how it works though.
            val otherChest = if ((blockState.blockData as ChestData).type == ChestData.Type.LEFT) doubleChest.leftSide
            else doubleChest.rightSide

            if (otherChest !is Chest) return@Runnable

            val player = event.player
            val dataContainer = otherChest.persistentDataContainer
            if (!Utils.hasOwner(dataContainer)) Utils.setOwner(dataContainer, player)
            else if (!Utils.isOwner(dataContainer, player)) {
                block.breakNaturally()
                Utils.notifyPlayer(player, "<red>You can't place a chest next to one you don't own!</red>", Sound.BLOCK_NOTE_BLOCK_BASS)
            }
        }, 1L)
    }
}