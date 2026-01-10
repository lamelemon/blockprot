package io.github.lamelemon.blockprot.utils

import io.github.lamelemon.blockprot.BlockProt.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.TileState
import org.bukkit.entity.Player
import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataContainer
import java.util.*

object Utils {

    private val friendKey: NamespacedKey = NamespacedKey(instance, "friends")
    private val ownerKey: NamespacedKey = NamespacedKey(instance, "owner")
    private val uuidListDataType = ListPersistentDataType.LIST.listTypeFrom(UUIDDataType)
    private val ignoredMaterials: HashSet<Material> = HashSet(setOf(
        Material.BEEHIVE,
        Material.BELL,
        Material.SUSPICIOUS_GRAVEL,
        Material.SUSPICIOUS_SAND,
        Material.CALIBRATED_SCULK_SENSOR,
        Material.SCULK_CATALYST,
        Material.SCULK_SENSOR,
        Material.CHISELED_BOOKSHELF,
        Material.COMPARATOR,
        Material.CREAKING_HEART,
        Material.SPAWNER,
        Material.DAYLIGHT_DETECTOR,
        Material.DECORATED_POT,
        Material.ENCHANTING_TABLE,
        Material.END_GATEWAY,
        Material.TRIAL_SPAWNER,
        Material.VAULT,
        Material.COMMAND_BLOCK,
        Material.CHAIN_COMMAND_BLOCK,
        Material.REPEATING_COMMAND_BLOCK
    ) + Tag.BANNERS.values
        + Tag.BEDS.values
        + Tag.COPPER_GOLEM_STATUES.values
        + Tag.SIGNS.values
        + Tag.WOODEN_SHELVES.values
        + Tag.ITEMS_SKULLS.values
        )

    fun messagePlayer(player: Player, message: String) {
        player.sendRichMessage("<white><gold>[</gold><blue>BlockProt</blue><gold>]</gold> $message</white>")
    }

    fun notifyPlayer(player: Player, message: String, sound: Sound) {
        messagePlayer(player, message)
        player.playSound(player, sound, 1f, 1f)
    }

    fun isIgnored(material: Material): Boolean { // Keep separate from tile entity check as we may want to store non-tile entities later in a different way
        return ignoredMaterials.contains(material)
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
        return player.persistentDataContainer.get(friendKey, uuidListDataType)
    }

    fun isFriend(dataContainer: PersistentDataContainer, friend: Player): Boolean {
        return dataContainer.get(friendKey, uuidListDataType)?.contains(friend.uniqueId) ?: false
    }

    fun isFriend(player: Player, friend: Player): Boolean {
        return player.persistentDataContainer.get(friendKey, uuidListDataType)?.contains(friend.uniqueId) ?: false
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

    fun setOwner(tileState: TileState, owner: Player) {
        tileState.persistentDataContainer.set(ownerKey, UUIDDataType, owner.uniqueId)
        tileState.update()
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
        val owningPlayer = player.server.getPlayer(owner)

        return if (owningPlayer is Player) {
            isFriend(owningPlayer.persistentDataContainer, player)
        } else {
            player.server.getOfflinePlayer(owner).persistentDataContainer.get(friendKey, uuidListDataType)?.contains(player.uniqueId) == true
        }
    }

    private fun PersistentDataContainer.addUuid(key: NamespacedKey, uuid: UUID) {
        val existing = this.get(key, uuidListDataType)?.toMutableList() ?: mutableListOf()
        existing.add(uuid)
        this.set(key, uuidListDataType, existing)
    }

    private fun PersistentDataContainer.removeUuid(key: NamespacedKey, uuid: UUID): Boolean {
        val existing = this.get(key, uuidListDataType)?.toMutableList() ?: return false
        val removed = existing.remove(uuid)
        if (removed) this.set(key, uuidListDataType, existing)
        return removed
    }
}