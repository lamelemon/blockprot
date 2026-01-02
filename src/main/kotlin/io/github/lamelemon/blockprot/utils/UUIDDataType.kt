package io.github.lamelemon.blockprot.utils

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.UUID

object UUIDDataType: PersistentDataType<ByteArray, UUID> {

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<UUID> = UUID::class.java

    override fun toPrimitive(
        complex: UUID,
        context: PersistentDataAdapterContext
    ): ByteArray {
        val bb = ByteBuffer.allocate(Long.SIZE_BYTES * 2)
        bb.putLong(complex.mostSignificantBits)
        bb.putLong(complex.leastSignificantBits)
        return bb.array()
    }

    override fun fromPrimitive(
        primitive: ByteArray,
        context: PersistentDataAdapterContext
    ): UUID {
        val bb = ByteBuffer.wrap(primitive)
        val firstLong = bb.getLong()
        val secondLong = bb.getLong()
        return UUID(firstLong, secondLong)
    }
}