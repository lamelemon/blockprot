package io.github.lamelemon.blockprot.utils

import io.github.lamelemon.blockprot.Blockprot.Companion.instance
import io.github.lamelemon.blockprot.utils.UUIDListDataType.addUuid
import io.github.lamelemon.blockprot.utils.UUIDListDataType.removeUuid
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

object utils {
    private val friendKey: NamespacedKey = NamespacedKey(instance, "friends")
    val ownerKey: NamespacedKey = NamespacedKey(instance, "owner")
    lateinit var functionalMaterials: HashSet<Material>

    fun messagePlayer(player: Player, message: String) {
        player.sendRichMessage("<gold>[</gold><blue>BlockProt</blue><gold>]</gold> $message")
    }

    fun addFriend(player: Player, friend: Player) {
        player.persistentDataContainer.addUuid(friendKey, friend.uniqueId)
    }

    fun addFriend(dataContainer: PersistentDataContainer, friend: Player) {
        dataContainer.addUuid(friendKey, friend.uniqueId)
    }

    fun removeFriend(player: Player, friend: Player) {
        player.persistentDataContainer.removeUuid(friendKey, friend.uniqueId)
    }

    fun removeFriend(dataContainer: PersistentDataContainer, friend: Player) {
        dataContainer.removeUuid(friendKey, friend.uniqueId)
    }

    fun isFriend(dataContainer: PersistentDataContainer, friend: Player): Boolean {
        return dataContainer.get(friendKey, UUIDListDataType)?.contains(friend.uniqueId) ?: false
    }

    fun setOwner(dataContainer: PersistentDataContainer, owner: Player) {
        dataContainer.set(ownerKey, UUIDDataType, owner.uniqueId)
    }

    fun getOwner(dataContainer: PersistentDataContainer): UUID? {
        return dataContainer.get(ownerKey, UUIDDataType)
    }

    fun removeOwner(dataContainer: PersistentDataContainer) {
        if (dataContainer.has(ownerKey)) {
            dataContainer.remove(ownerKey)
        }
    }

    fun isAllowedToInteract(dataContainer: PersistentDataContainer, player: Player): Boolean {
        if (isFriend(dataContainer, player)) return true

        val owner = getOwner(dataContainer)?.let { Bukkit.getPlayer(it) }
        return if (owner is Player) {
            isFriend(owner.persistentDataContainer, player)
        } else {
            getOwner(dataContainer)?.let { Bukkit.getOfflinePlayer(it) }?.persistentDataContainer?.get(friendKey, UUIDListDataType)?.contains(player.uniqueId) == true
        }
    }
}