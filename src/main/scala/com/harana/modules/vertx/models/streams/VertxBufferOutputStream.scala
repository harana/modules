package com.harana.modules.vertx.models.streams

import io.vertx.core.buffer.Buffer

import java.io.OutputStream

class VertxBufferOutputStream extends OutputStream {

	val buffer = Buffer.buffer()

	override def write(b: Int) =
		buffer.appendByte((b & 0xFF).toByte)
	
	override def write(b: Array[Byte]) =
		buffer.appendBytes(b)
	
	override def write(b: Array[Byte], off: Int, len: Int) =
		buffer.appendBytes(b, off, len)

}