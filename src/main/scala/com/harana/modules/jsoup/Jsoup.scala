package com.harana.modules.jsoup

import com.harana.modules.jsoup.models._
import org.jsoup.nodes.{Document, Element, Node}
import org.jsoup.select.Elements
import zio.macros.accessible
import zio.stream._
import zio.{IO, UIO}

import java.io.{BufferedInputStream, File}
import java.net.URL

@accessible
trait Jsoup {
  def parse(file: File): IO[JsoupError, Document]

  def parse(string: String, fragment: Boolean = false): IO[JsoupError, Document]

  def parse(string: String, baseUri: String): IO[JsoupError, Document]

  def parse(url: URL, connectionOptions: ConnectionOptions): IO[JsoupError, Document]

  def parse(urlStream: Stream[JsoupError, URL], connectionOptions: ConnectionOptions): UIO[Stream[JsoupError, Document]]

  def elementStream(doc: Document, selector: String): UIO[Stream[JsoupError, Element]]

  def linkStream(doc: Document): UIO[Stream[JsoupError, URL]]

  def mediaStream(doc: Document): UIO[Stream[JsoupError, URL]]

  def stream(url: URL, connectionOptions: ConnectionOptions): IO[JsoupError, BufferedInputStream]

  def download(url: URL, path: File, connectionOptions: ConnectionOptions): IO[JsoupError, Unit]

  def mirror(url: URL, downloadDir: File, connectionOptions: ConnectionOptions): IO[JsoupError, Unit]

  def recursiveDownload(startDoc: Document,
                        navigateSelector: String,
                        downloadSelector: String,
                        downloadDir: File,
                        shouldDownload: Document => Boolean,
                        connectionOptions: ConnectionOptions): IO[JsoupError, Unit]
}

object Jsoup {
  implicit def enrichElements(xs: Elements): RichElements = new RichElements(xs)
  implicit def enrichElement(el: Element): RichElement = new RichElement(el)
  implicit def enrichNodeList[N <: Node](l: java.util.List[N]): RichNodeList[N] = new RichNodeList(l)
}
