package io.github.lamelemon.blockprot.utils

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.TileState
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.Locale.getDefault
import org.bukkit.block.data.type.Chest as ChestData

class BlockOwnerDialog(val player: Player, val block: Block, val tileState: TileState): Listener {

    init {
        player.showDialog(Dialog.create { builder ->
            val currentState = Utils.getLockState(tileState.persistentDataContainer)
            builder.empty()
                .base(DialogBase.builder(Component.text("${player.name}'s ${
                    tileState.block.type.name.lowercase(getDefault()).replaceFirstChar { it.titlecase(getDefault()) }}"
                ))
                    .inputs(listOf(
                        DialogInput.singleOption("lockState", 300, listOf(
                            SingleOptionDialogInput.OptionEntry.create("1",
                                Component.text("Owner", NamedTextColor.GOLD),
                                currentState == Utils.LockState.OWNER.index
                            ),
                            SingleOptionDialogInput.OptionEntry.create("2",
                                Component.text("Friends & Owner", NamedTextColor.GOLD),
                                currentState == Utils.LockState.FRIENDS.index
                            ),
                            SingleOptionDialogInput.OptionEntry.create("3",
                                Component.text("Anyone", NamedTextColor.GOLD),
                                currentState == Utils.LockState.ANYONE.index
                            )),
                            Component.text("State", NamedTextColor.DARK_BLUE),
                            true
                        )
                    ))
                    .build()
                )
                .type(DialogType.multiAction(
                    mutableListOf(
                        ActionButton.builder(Component.text("Discard", NamedTextColor.RED))
                            .tooltip(Component.text("Click to discard changes."))
                            .action(DialogAction.customClick(Key.key("blockprot:blockowner/cancel"), null))
                            .build(),
                        ActionButton.builder(Component.text("Save", NamedTextColor.GREEN))
                            .tooltip(Component.text("Click to save changes."))
                            .action(
                                DialogAction.customClick(
                                    { response, audience ->
                                        val newState = response.getText("lockState")?.toByte()
                                        if (newState is Byte && newState != currentState) {
                                            Utils.setLockState(tileState, newState)
                                            Utils.notifyPlayer(player, "Changed block lock state!", Sound.BLOCK_NOTE_BLOCK_PLING)
                                        }
                                    },
                                    ClickCallback.Options.builder()
                                        .uses(1)
                                        .build()
                                )
                            ).build(),
                        ActionButton.builder(Component.text("Remove ownership", NamedTextColor.YELLOW))
                            .tooltip(Component.text("Click to remove ownership of this block."))
                            .action(
                                DialogAction.customClick(
                                    { response, audience ->
                                        Utils.removeOwner(tileState)
                                        Utils.setLockState(tileState, Utils.LockState.FRIENDS)
                                        Utils.notifyPlayer(player, "Removed ownership of block! Sneak + Use to reclaim ownership!", Sound.BLOCK_NOTE_BLOCK_PLING)
                                        if (tileState is Chest && (tileState.blockData as ChestData).type != ChestData.Type.SINGLE) {
                                            val doubleChest = tileState.inventory.holder as DoubleChest

                                            // Slightly confusing, this is how it works though.
                                            val otherChest = if ((tileState.blockData as ChestData).type == ChestData.Type.LEFT) doubleChest.leftSide
                                            else doubleChest.rightSide

                                            if (Utils.isOwner((otherChest as TileState).persistentDataContainer, player) || player.hasPermission("blockprot.permission.admin.unlock")) {
                                                Utils.removeOwner(otherChest)
                                                Utils.setLockState(otherChest, Utils.LockState.FRIENDS)
                                            }
                                        }
                                    },
                                    ClickCallback.Options.builder()
                                        .uses(1)
                                        .build()
                                )
                            ).build()
                    )
                ).build()
                )
        })
    }
}