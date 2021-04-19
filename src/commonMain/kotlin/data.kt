import io.ktor.utils.io.core.*
import kotlin.time.*

class Payload(
    val data: ByteReadPacket,
    val metadata: ByteReadPacket
)

class Frame(
    val streamId: Int,
    val flags: Short,
    val payload: Payload
)

fun Frame.write(): ByteReadPacket = buildPacket {
    writeInt(streamId)
    writeShort(flags)
    val length = payload.metadata.remaining.toInt()
    writeByte((length shr 16).toByte())
    writeByte((length shr 8).toByte())
    writeByte(length.toByte())
    writePacket(payload.metadata)
    writePacket(payload.data)
}

fun ByteReadPacket.read(): Frame = Frame(
    streamId = readInt(),
    flags = readShort(),
    payload = run {
        val b = readByte().toInt() and 0xFF shl 16
        val b1 = readByte().toInt() and 0xFF shl 8
        val b2 = readByte().toInt() and 0xFF
        val length = b or b1 or b2
        val metadata = buildPacket { writePacket(this@run, length) }
        val data = buildPacket { writePacket(this@run) }
        Payload(
            data = data,
            metadata = metadata
        )
    }
)

@ExperimentalTime
class MeasureResult(
    val write: Duration,
    val read: Duration
)

@ExperimentalTime
data class DurationStats(
    val min: Duration,
    val max: Duration,
    val avg: Duration
)

@ExperimentalTime
data class MeasureStats(val write: DurationStats, val read: DurationStats) {
    fun print(tag: String) {
        println("[$tag] write: $write | read: $read")
    }
}

@ExperimentalTime
val List<Duration>.stats
    get() = DurationStats(
        max = maxOrNull()!!,
        min = minOrNull()!!,
        avg = (sumOf { it.inMilliseconds } / size).milliseconds,
    )

@ExperimentalTime
val List<DurationStats>.aggregate
    get() = DurationStats(
        max = maxOf { it.avg },
        min = minOf { it.avg },
        avg = (sumOf { it.avg.inMilliseconds } / size).milliseconds,
    )
