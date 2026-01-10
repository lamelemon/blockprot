package io.github.lamelemon.blockprot.utils

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.entity.Player

object Dialogs {
    fun createBlockDialog(player: Player, block: Block) {
        return // TODO finish dialog
        player.showDialog(Dialog.create { builder ->
            builder.empty()
                .base(DialogBase.builder(Component.text("Configure your new experience value"))
                    .inputs(listOf(
                        DialogInput.numberRange("level", Component.text("Level", NamedTextColor.GREEN), 0f, 100f)
                            .step(1f)
                            .initial(0f)
                            .width(300)
                            .build(),
                        DialogInput.numberRange("experience", Component.text("Experience", NamedTextColor.GREEN), 0f, 100f)
                            .step(1f)
                            .initial(0f)
                            .labelFormat("%s: %s percent to the next level")
                            .width(300)
                            .build()
                    ))
                    .build()
                )
                .type(DialogType.notice())
        })
    }
}