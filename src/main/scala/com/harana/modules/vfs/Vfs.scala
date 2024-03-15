package com.harana.modules.vfs

import com.harana.sdk.shared.models.HaranaFile
import zio.Task
import zio.macros.accessible

import java.io.{File, InputStream, OutputStream}

@accessible
trait Vfs {
    def read(uri: String): Task[InputStream]
    def read(uri: String, outputStream: OutputStream): Task[Unit]
    def readAsBytes(uri: String): Task[Array[Byte]]
    def write(uri: String, inputStream: InputStream): Task[Unit]
    def copy(fromUri: String, toUri: String): Task[Unit]
    def move(fromUri: String, toUri: String): Task[Unit]
    def info(uri: String): Task[HaranaFile]
    def mkdir(uri: String): Task[Unit]
    def delete(uri: String): Task[Unit]
    def exists(uri: String): Task[Boolean]
    def duplicate(uri: String): Task[Unit]
    def underlyingFile(uri: String): Task[File]
    def list(uri: String): Task[List[HaranaFile]]
    def search(uri: String, query: String): Task[List[HaranaFile]]
    def size(uri: String): Task[Long]
    def decompress(uri: String): Task[Unit]
    def compress(uri: String): Task[Unit]
    def rename(uri: String, newName: String): Task[Option[Unit]]
}