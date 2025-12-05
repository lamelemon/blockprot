package io.github.lamelemon.blockprot.events

import io.github.lamelemon.blockprot.utils.utils.functionalMaterials
import io.github.lamelemon.blockprot.utils.utils.setOwner
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataHolder

class BlockPlace: Listener {

    @EventHandler
    fun blockPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        if (!event.canBuild()) return

        val block = event.blockPlaced
        if (!functionalMaterials.contains(block.type)) return

        val blockState = block.state
        if (blockState is PersistentDataHolder) {
            setOwner(blockState.persistentDataContainer, event.player)
        }
    }
}