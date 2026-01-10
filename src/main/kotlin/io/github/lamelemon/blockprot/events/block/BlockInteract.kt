package io.github.lamelemon.blockprot.events.block

import io.github.lamelemon.blockprot.BlockProt.Companion.instance
import io.github.lamelemon.blockprot.utils.Dialogs
import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent


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
        else if (Utils.isOwner(dataContainer, player) && player.isSneaking && player.inventory.itemInMainHand.isEmpty) {
            Dialogs.createBlockDialog(player, block)
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
        val startTime = System.nanoTime()

        blocks.removeIf { block ->
            block.state is TileState &&
                    !Utils.isIgnored(block.type) &&
                    Utils.hasOwner((block.state as TileState).persistentDataContainer)
        }

        instance.logger.info("took ${(System.nanoTime() - startTime)}ns")
    }
}