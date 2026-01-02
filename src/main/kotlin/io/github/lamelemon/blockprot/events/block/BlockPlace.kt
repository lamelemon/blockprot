package io.github.lamelemon.blockprot.events.block

import io.github.lamelemon.blockprot.utils.Utils
import org.bukkit.block.Chest
import org.bukkit.block.data.type.Chest as ChestData
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlace: Listener {

    @EventHandler
    fun blockPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        if (!event.canBuild()) return

        val blockState = event.blockPlaced.state
        if (blockState !is TileState) return
        if (Utils.isIgnored(blockState.type)) return

        if (blockState is Chest && (blockState.blockData as ChestData).type != ChestData.Type.RIGHT) {
            TODO("Add support for double chests")
        }

        Utils.setOwner(blockState.persistentDataContainer, event.player)
        blockState.update()

        if (blockState !is Chest) return
        if ((blockState.blockData as ChestData).type == ChestData.Type.SINGLE) return
    }
}