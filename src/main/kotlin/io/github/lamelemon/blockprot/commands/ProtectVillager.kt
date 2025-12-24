package io.github.lamelemon.blockprot.commands

import io.github.lamelemon.blockprot.events.entity.VillagerProtect
import io.github.lamelemon.blockprot.utils.Utils
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class ProtectVillager(val timeoutDuration: Long, val applyGlow: Boolean): BasicCommand {

    override fun execute(
        commandSourceStack: CommandSourceStack,
        args: Array<out String>
    ) {
        val sender = commandSourceStack.sender
        if (sender !is Player) return

        Utils.messagePlayer(sender, "Right click a villager to register ownership!")
        VillagerProtect(sender, timeoutDuration, applyGlow)
    }

    /*override fun permission(): String {
        return "blockprot.permission.villager"
    }*/
}