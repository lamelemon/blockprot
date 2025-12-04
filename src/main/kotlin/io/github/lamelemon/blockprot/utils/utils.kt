package io.github.lamelemon.blockprot.utils

import io.github.lamelemon.blockprot.Blockprot.Companion.instance
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object utils {
    val friendKey: NamespacedKey = NamespacedKey(instance, "friends")

    fun messagePlayer(player: Player, message: String) {
        player.sendRichMessage("<gold>[</gold><blue>BlockProt</blue><gold>]</gold> $message")
    }

    fun addPlayerFriend(player: Player, friend: Player) {
        TODO("not added yet")
    }
}