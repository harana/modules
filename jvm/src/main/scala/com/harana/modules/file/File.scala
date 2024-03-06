package com.harana.modules.file

import io.circe.{Decoder, Encoder}
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.ext.reactivestreams.ReactiveWriteStream
import one.jasyncfio.AsyncFile
import zio.Task
import zio.macros.accessible

import java.nio.ByteBuffer
import java.nio.file.Path

@accessible
trait File {

    def readStream(path: Path, range: Option[(Long, Long)] = None): Task[ReadStream[Buffer]]
    def read(file: Either[Path, AsyncFile], buffer: ByteBuffer, position: Option[Int] = None): Task[Int]
    def readJson[A](file: Path)(implicit decoder: Decoder[A]): Task[A]
    def readString(file: Path): Task[String]

    def writeAwsStream(path: Path,
                       stream: ReactiveWriteStream[Buffer],
                       length: Long,
                       onStart: Option[() => Any] = None,
                       onStop: Option[() => Any] = None): Task[Unit]
    def writeStream(path: Path,
                    stream: ReactiveWriteStream[Buffer],
                    length: Long,
                    onStart: Option[() => Any] = None,
                    onStop: Option[() => Any] = None,
                    onData: Option[Buffer => (Buffer, Boolean)] = None): Task[Unit]
    def write(file: Either[Path, AsyncFile], buffer: ByteBuffer, position: Option[Int] = None): Task[Int]
    def writeJson[A](file: Path, obj: A)(implicit encoder: Encoder[A]): Task[Unit]
    def writeString(file: Path, string: String): Task[Unit]

    def merge(sourcePaths: List[Path], targetPath: Path): Task[Unit]

    def close(file: Either[Path, AsyncFile]): Task[Unit]

}