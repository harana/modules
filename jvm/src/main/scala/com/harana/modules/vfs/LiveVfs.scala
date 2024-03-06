package com.harana.modules.vfs

import com.github.vfss3.S3FileProvider
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.sdk.shared.models.HaranaFile
import net.lingala.zip4j.ZipFile
import org.apache.commons.io.IOUtils
import org.apache.commons.vfs2.impl.DefaultFileSystemManager
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileProvider
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider
import org.apache.commons.vfs2.provider.ftps.FtpsFileProvider
import org.apache.commons.vfs2.provider.gzip.GzipFileProvider
import org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider
import org.apache.commons.vfs2.provider.http.HttpFileProvider
import org.apache.commons.vfs2.provider.https.HttpsFileProvider
import org.apache.commons.vfs2.provider.jar.JarFileProvider
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider
import org.apache.commons.vfs2.provider.ram.RamFileProvider
import org.apache.commons.vfs2.provider.res.ResourceFileProvider
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider
import org.apache.commons.vfs2.provider.tar.TarFileProvider
import org.apache.commons.vfs2.provider.temp.TemporaryFileProvider
import org.apache.commons.vfs2.provider.url.UrlFileProvider
import org.apache.commons.vfs2.{AllFileSelector, FileUtil, Selectors}

import java.io.{File, InputStream, OutputStream}
//import org.apache.commons.vfs2.provider.webdav.WebdavFileProvider
import org.apache.commons.vfs2.provider.zip.ZipFileProvider
import zio.{Task, ZLayer, ZIO}

object LiveVfs {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveVfs(config, logger, micrometer)
  }
}

case class LiveVfs(config: Config, logger: Logger, micrometer: Micrometer) extends Vfs {

  private val sourceManager = {
    val fsm = new DefaultFileSystemManager()
    fsm.addProvider("ftp", new FtpFileProvider)
    fsm.addProvider("ftps", new FtpsFileProvider)
    fsm.addProvider("hdfs", new HdfsFileProvider)
    fsm.addProvider("http", new HttpFileProvider)
    fsm.addProvider("https", new HttpsFileProvider)
    fsm.addProvider("local", new DefaultLocalFileProvider)
    fsm.addProvider("sftp", new SftpFileProvider)
    fsm.addProvider("s3", new S3FileProvider)
//      fsm.addProvider("webdav", new WebdavFileProvider)
    fsm.addProvider("bzip2", new Bzip2FileProvider)
    fsm.addProvider("gzip", new GzipFileProvider)
    fsm.addProvider("jar", new JarFileProvider)
    fsm.addProvider("ram", new RamFileProvider)
    fsm.addProvider("res", new ResourceFileProvider)
    fsm.addProvider("tar", new TarFileProvider)
    fsm.addProvider("tmp", new TemporaryFileProvider)
    fsm.addProvider("url", new UrlFileProvider)
    fsm.addProvider("zip", new ZipFileProvider)
    fsm.init()
    fsm
  }

  private val all = new AllFileSelector()


  def mkdir(uri: String): Task[Unit] =
    ZIO.attempt(file(uri).createFolder())


  def read(uri: String): Task[InputStream] =
    ZIO.attempt(file(uri).getContent.getInputStream)


  def read(uri: String, outputStream: OutputStream): Task[Unit] =
    ZIO.attempt {
      val content = file(uri).getContent
      IOUtils.copy(content.getInputStream, outputStream)
      content.close()
    }


  def readAsBytes(uri: String): Task[Array[Byte]] =
    ZIO.attempt(FileUtil.getContent(file(uri)))


  def write(uri: String, inputStream: InputStream): Task[Unit] =
    ZIO.attempt {
      val content = file(uri).getContent
      IOUtils.copy(inputStream, content.getOutputStream)
      content.close()
    }


  def copy(fromUri: String, toUri: String): Task[Unit] =
    ZIO.attempt(file(toUri).copyFrom(file(fromUri), Selectors.SELECT_ALL))


  def move(fromUri: String, toUri: String): Task[Unit] =
    ZIO.attempt(file(fromUri).moveTo(file(toUri)))


  def duplicate(uri: String): Task[Unit] =
    ZIO.attempt(file(duplicateName(file(uri))).copyFrom(file(uri), Selectors.SELECT_ALL))


  def delete(uri: String): Task[Unit] =
    ZIO.attempt(if (file(uri).exists()) file(uri).delete())


  def exists(uri: String): Task[Boolean] =
    ZIO.attempt(file(uri).exists())


  def info(uri: String): Task[HaranaFile] =
    ZIO.attempt(toDataFile(file(uri)))


  def list(uri: String): Task[List[HaranaFile]] =
    ZIO.attempt(file(uri).getChildren.toList.map(toDataFile))


  def search(uri: String, query: String): Task[List[HaranaFile]] = {
    val lowercaseQuery = query.toLowerCase
    ZIO.attempt(file(uri).findFiles(all).toList.filter(_.getName.getBaseName.toLowerCase.contains(lowercaseQuery)).map(toDataFile))
  }

  def underlyingFile(uri: String): Task[File] =
    ZIO.attempt(new File(file(uri).getName.getPath))


  def size(uri: String): Task[Long] =
    ZIO.attempt(calculateSize(file(uri)))


  def decompress(uri: String): Task[Unit] =
    ZIO.attempt {
      val outputDir = file(decompressName(file(uri)))
      outputDir.createFolder()
      new ZipFile(file(uri).getName.getPath).extractAll(outputDir.getName.getPath)
    }


  def compress(uri: String): Task[Unit] =
    ZIO.attempt {
      val inputFile = new File(file(uri).getName.getPath)
      val outputFile = new File(file(compressName(file(uri), "zip")).getName.getPath)
      if (file(uri).isFile) new ZipFile(outputFile).addFile(inputFile) else new ZipFile(outputFile).addFolder(inputFile)
    }


  def rename(uri: String, newName: String): Task[Option[Unit]] = {
    val path = s"${file(uri).getParent}/$newName"
    move(uri, s"${file(uri).getParent}/$newName").unless(file(path).exists())
  }


  private def file(uri: String) = sourceManager.resolveFile(uri)

}