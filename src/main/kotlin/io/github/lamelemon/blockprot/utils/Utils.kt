package io.github.lamelemon.blockprot.utils

import io.github.lamelemon.blockprot.BlockProt.Companion.instance
import io.github.lamelemon.blockprot.utils.dataTypes.UUIDListDataType.addUuid
import io.github.lamelemon.blockprot.utils.dataTypes.UUIDListDataType.removeUuid
import io.github.lamelemon.blockprot.utils.dataTypes.UUIDDataType
import io.github.lamelemon.blockprot.utils.dataTypes.UUIDListDataType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

object Utils {

    private val friendKey: NamespacedKey = NamespacedKey(instance, "friends")
    val ownerKey: NamespacedKey = NamespacedKey(instance, "owner")

    val functionalTags: HashSet<Tag<Material>> = HashSet(setOf(
        Tag.DOORS,
        Tag.TRAPDOORS,
        Tag.SHULKER_BOXES,
        Tag.COPPER_CHESTS
    ))

    val functionalMaterials: HashSet<Material> = HashSet(setOf(
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.BARREL,
        Material.CRAFTER,
        Material.DROPPER,
        Material.DISPENSER,
        Material.FURNACE,
        Material.BLAST_FURNACE,
        Material.SMOKER
    ))

    fun messagePlayer(player: Player, message: String) {
        player.sendRichMessage("<white><gold>[</gold><blue>BlockProt</blue><gold>]</gold> $message</white>")
    }

    fun notifyPlayer(player: Player, message: String, sound: Sound) {
        messagePlayer(player, message)
        player.playSound(player, sound, 1f, 1f)
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

    fun getFriends(player: Player): List<UUID>? {
        return player.persistentDataContainer.get(friendKey, UUIDListDataType)
    }

    fun isFriend(dataContainer: PersistentDataContainer, friend: Player): Boolean {
        return dataContainer.get(friendKey, UUIDListDataType)?.contains(friend.uniqueId) ?: false
    }

    fun isFriend(player: Player, friend: Player): Boolean {
        return player.persistentDataContainer.get(friendKey, UUIDListDataType)?.contains(friend.uniqueId) ?: false
    }

    fun isOwner(dataContainer: PersistentDataContainer, player: Player): Boolean {
        return dataContainer.get(ownerKey, UUIDDataType) == player.uniqueId
    }

    fun hasOwner(dataContainer: PersistentDataContainer): Boolean {
        return dataContainer.get(ownerKey, UUIDDataType) is UUID
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
        if (isOwner(dataContainer, player)) return true

        val owner = getOwner(dataContainer)
        if (owner !is UUID) return true
        val owningPlayer: Player? = player.server.getPlayer(owner)

        return if (owningPlayer is Player) {
            isFriend(owningPlayer.persistentDataContainer, player)
        } else {
            player.server.getOfflinePlayer(owner).persistentDataContainer.get(friendKey, UUIDListDataType)?.contains(player.uniqueId) == true
        }
    }
}