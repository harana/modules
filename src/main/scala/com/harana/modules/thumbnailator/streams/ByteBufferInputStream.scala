package com.harana.modules.thumbnailator.streams

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Wraps a ByteBuffer so it can be used with interfaces that require an InputStream. The
 * buffer should not be modified outside of the reader until reading is complete.
 */
class ByteBufferInputStream(buffer: ByteBuffer) extends InputStream {

    override def read(): Int = {
        if (buffer.hasRemaining) buffer.get() else -1
    }

    override def read(buf: Array[Byte], offset: Int, length: Int): Int = {
        if (buffer.hasRemaining) {
            val readLength = math.min(buffer.remaining(), length)
            buffer.get(buf, offset, readLength)
            readLength
        } else {
            -1
        }
    }

    override def available(): Int = {
        buffer.remaining()
    }

    override def skip(n: Long): Long = {
        val skipAmount = math.min(buffer.remaining(), n).toInt
        buffer.position(buffer.position() + skipAmount)
        skipAmount
    }

    override def markSupported(): Boolean = true

    override def mark(readlimit: Int): Unit = {
        buffer.mark()
    }

    override def reset(): Unit = {
        buffer.reset()
    }

    override def close(): Unit = {
        buffer.flip()
    }
}