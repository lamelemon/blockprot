package io.github.lamelemon.blockprot.utils.dataTypes

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.UUID

object UUIDListDataType: PersistentDataType<ByteArray, List<UUID>> {
    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<List<UUID>> {
        @Suppress("UNCHECKED_CAST")
        return List::class.java as Class<List<UUID>>
    }

    override fun toPrimitive(
        complex: List<UUID>,
        context: PersistentDataAdapterContext
    ): ByteArray {
        val bb = ByteBuffer.allocate(complex.size * 16) // 16 bytes per UUID
        for (uuid in complex) {
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
        }
        return bb.array()
    }

    override fun fromPrimitive(
        primitive: ByteArray,
        context: PersistentDataAdapterContext
    ): List<UUID> {
        val bb = ByteBuffer.wrap(primitive)
        val list = mutableListOf<UUID>()

        while (bb.remaining() >= 16) {
            val msb = bb.long
            val lsb = bb.long
            list.add(UUID(msb, lsb))
        }

        return list
    }

    fun PersistentDataContainer.addUuid(key: NamespacedKey, uuid: UUID) {
        val existing = this.get(key, UUIDListDataType)?.toMutableList() ?: mutableListOf()
        existing.add(uuid)
        this.set(key, UUIDListDataType, existing)
    }

    fun PersistentDataContainer.removeUuid(key: NamespacedKey, uuid: UUID): Boolean {
        val existing = this.get(key, UUIDListDataType)?.toMutableList() ?: return false
        val removed = existing.remove(uuid)
        if (removed) this.set(key, UUIDListDataType, existing)
        return removed
    }
}