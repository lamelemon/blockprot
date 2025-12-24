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

        val argument = args[0].lowercase()
        when (args[0].lowercase(getDefault())) {
            "add" -> {
                if (args.size < 2) {
                    Utils.messagePlayer(sender, "<red>Please enter a username!</red>")
                }
                editFriends(sender, args[1], true)
            }

            "remove" -> {
                if (args.size < 2) {
                    Utils.messagePlayer(sender, "<red>Please enter a username!</red>")
                }
                editFriends(sender, args[1], false)
            }

            "list" -> {
                Utils.notifyPlayer(sender, "<dark_blue>Friends: </dark_blue>\n${fancyFriendsList(sender).joinToString("\n")}", Sound.BLOCK_NOTE_BLOCK_PLING)
            }

            else -> {
                Utils.notifyPlayer(sender, "<red>\"$argument\" Is not a valid argument!</red>", Sound.BLOCK_NOTE_BLOCK_BASS)
            }
        }
    }

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<out String>): Collection<String> {
        if (args.isEmpty()) return this.args
        if (args.size > 2) return super.suggest(commandSourceStack, args)

        return when (args[0]) {
            "add" -> {
                commandSourceStack.sender.server.onlinePlayers.map { it.name }
            }

            "remove" -> {
                getFriendsList(commandSourceStack.sender as Player)
            }

            else -> {
                super.suggest(commandSourceStack, args)
            }
        }

    }

    override fun permission(): String {
        return "blockprot.permission.friends"
    }

    fun getFriendsList(player: Player): TreeSet<String> {
        val friends = Utils.getFriends(player)
        if (friends !is List<UUID>) return TreeSet()

        val results: TreeSet<String> = TreeSet()
        for (uuid in friends) {
            val friend = player.server.getPlayer(uuid)
            if (friend is Player) {
                results.add(friend.name)
            } else {
                player.server.getOfflinePlayer(uuid).name?.let { results.add(it) }
            }
        }
        return results
    }

    // I know this can be merged with the other one get off my back
    fun fancyFriendsList(player: Player): TreeSet<String> {
        val friends = Utils.getFriends(player)
        if (friends !is List<UUID>) return TreeSet(setOf("<red>None!</red>"))

        val results: TreeSet<String> = TreeSet()
        for (uuid in friends) {
            val friend = player.server.getPlayer(uuid)
            if (friend is Player) {
                results.add("<green>${friend.name}</green>")
            } else {
                player.server.getOfflinePlayer(uuid).name?.let { results.add("<red>$it</red>") }
            }
        }
        return results
    }

    fun editFriends(player: Player, friend: String, add: Boolean) {
        val playerFriend = player.server.getOfflinePlayer(friend).player
        if (playerFriend !is Player) {
            Utils.notifyPlayer(player, "<red>Could not find player!</red>", Sound.BLOCK_NOTE_BLOCK_BASS)
            return
        }

        if (add) {
            if (Utils.isFriend(player, playerFriend)) {
                Utils.notifyPlayer(player, "<red>${playerFriend.name} is already in your friends list!", Sound.BLOCK_NOTE_BLOCK_BASS)
                return
            }
            Utils.addFriend(player, playerFriend)
            Utils.notifyPlayer(player, "<green>Successfully added ${playerFriend.name} as a friend!", Sound.BLOCK_NOTE_BLOCK_PLING)
            return
        }

        if (Utils.isFriend(player, playerFriend)) {
            Utils.removeFriend(player, playerFriend)
            Utils.notifyPlayer(player, "<green>Successfully removed ${playerFriend.name} from your friends list!", Sound.BLOCK_NOTE_BLOCK_PLING)
            return
        }

        Utils.notifyPlayer(player, "<red>${playerFriend.name} is not in your friends list!</red>", Sound.BLOCK_NOTE_BLOCK_BASS)
    }
}