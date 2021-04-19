import io.ktor.utils.io.core.*
import kotlin.test.*
import kotlin.time.*

@ExperimentalTime
class FramesTest {

    @Test
    fun bytesTestArray() {
        val generator = generator(::array, ::packet)
//        assert(generator)
        benchmark(generator)
    }

    @Test
    fun bytesTestArrayRaw() {
        val generator = generator(::array, ::packetRaw)
//        assert(generator)
        benchmark(generator)
    }

    @Test
    fun bytesTestString() {
        val generator = generator(::string, ::packet)
//        assert(generator)
        benchmark(generator)
    }
}

fun assert(generator: () -> List<Frame>) {
    generator().forEach {
        val frame1 = Frame(it.streamId, it.flags, Payload(it.payload.data.copy(), it.payload.metadata.copy()))
        val frame2 = it.write().read()

        assertEquals(frame1.streamId, frame2.streamId)
        assertEquals(frame1.flags, frame2.flags)
        assertTrue(frame1.payload.data.isNotEmpty)
        assertTrue(frame2.payload.data.isNotEmpty)
        assertTrue(frame1.payload.metadata.isNotEmpty)
        assertTrue(frame2.payload.metadata.isNotEmpty)
        assertEquals(frame1.payload.data.readBytes().decodeToString(), frame2.payload.data.readBytes().decodeToString())
        assertEquals(frame1.payload.metadata.readBytes().decodeToString(), frame2.payload.metadata.readBytes().decodeToString())
    }
}
