import io.ktor.utils.io.core.*
import kotlin.random.*
import kotlin.time.*

fun packet(text: String): ByteReadPacket = buildPacket { writeText(text) }
fun packet(array: ByteArray): ByteReadPacket = buildPacket { writeFully(array) }
fun packetRaw(array: ByteArray): ByteReadPacket = ByteReadPacket(array)

fun array() = Random.nextBytes(10000)
fun string() = run {
    "0123456789qwertyuiopasdfghjklzxcvbnm,./'".repeat(250)
}

@ExperimentalTime
fun benchmark(generator: () -> List<Frame>) {
    repeat(5) { generator().warm() }
    (1..10).map { generator().measure().also { it.print("warmup") } }
    val iteration = (1..10).map { generator().measure().also { it.print("iteration") } }

    val iw = iteration.map { it.write }.aggregate
    val ir = iteration.map { it.read }.aggregate

    println()
    println()
    println("[!!!result!!! ${platform()}] write: ${iw.avg} | read: ${ir.avg}")
    println()
    println()
}

@ExperimentalTime
fun List<Frame>.measure(): MeasureStats {
    val results = map { frame ->
        val (packet, writeTime) = measureTimedValue { frame.write() }
        val (_, readTime) = measureTimedValue { packet.read() }
        MeasureResult(writeTime, readTime)
    }
    val write = results.map { it.write }.stats
    val read = results.map { it.read }.stats

    return MeasureStats(write, read)
}

fun List<Frame>.warm() {
    forEach { it.write().read() }
}

fun <T> generator(dataBlock: () -> T, packetBlock: (T) -> ByteReadPacket): () -> List<Frame> = {
    List(10000) {
        Frame(
            Random.nextInt(),
            Random.nextInt().toShort(),
            Payload(
                packetBlock(dataBlock()),
                packetBlock(dataBlock())
            )
        )
    }
}
