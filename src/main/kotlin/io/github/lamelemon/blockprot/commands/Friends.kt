package io.github.lamelemon.blockprot.commands

import io.github.lamelemon.blockprot.utils.Utils
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.Locale.getDefault
import java.util.TreeSet
import java.util.UUID

class Friends: BasicCommand {

    val args: TreeSet<String> = TreeSet(setOf(
        "add",
        "list",
        "remove"
    ))

    override fun execute(
        commandSourceStack: CommandSourceStack,
        args: Array<out String>
    ) {
        val sender = commandSourceStack.sender
        if (sender !is Player) {
            return
        }

        if (args.isEmpty()) return

        when (args[0].lowercase(getDefault())) {
            "list" -> {
                val friends = Utils.getFriends(sender)
                if (friends !is List<UUID>) {
                    Utils.messagePlayer(sender, "<red>You currently have no friends!</red>")
                    sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
                    return
                }

                sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                Utils.messagePlayer(sender, "<dark_blue>Friends: </dark_blue>\n${parseFriendsList(sender, friends).joinToString("\n")}")
            }
        }
    }

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<out String>): Collection<String> {
        if (args.isEmpty()) return this.args

        if (args[0] == "add" || args[0] == "remove") {
            return getFriendsList(commandSourceStack.sender as Player)
        }

        return super.suggest(commandSourceStack, args)
    }

    override fun permission(): String {
        return "blockprot.permission.friends"
    }

    fun getFriendsList(player: Player): TreeSet<String> {
        val friends = Utils.getFriends(player)
        TODO("Not yet implemented")
    }

    fun parseFriendsList(player: Player, friends: List<UUID>): List<String> {
        val friendsList: TreeSet<String> = TreeSet()
        for (uuid: UUID in friends) {
            val friend = player.server.getPlayer(uuid)
            if (friend is Player) {
                friendsList.add("<green>${friend.name}</green>\n")
            } else {
                friendsList.add("<red>${player.server.getOfflinePlayer(uuid).name}</red>\n")
            }
        }
        return friendsList.sorted()
    }
}