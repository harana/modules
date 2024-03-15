package com.harana.modules

import com.harana.sdk.shared.models.HaranaFile
import org.apache.commons.lang3.StringUtils
import org.apache.commons.vfs2.{AllFileSelector, FileObject}

import java.time.Instant

package object vfs {

  def calculateSize(file: FileObject): Long = {
    if (file.isFile) file.getContent.getSize
    else file.findFiles(new AllFileSelector()).map(f => if (f.isFolder) 0 else f.getContent.getSize).sum
  }


  def toDataFile(file: FileObject) = {
    HaranaFile(
      name = file.getName.getBaseName,
      path = file.getName.getPath,
      extension = if (StringUtils.isEmpty(file.getName.getExtension)) None else Some(file.getName.getExtension),
      isFolder = file.isFolder,
      created = Instant.ofEpochMilli(file.getContent.getLastModifiedTime),
      updated = Instant.ofEpochMilli(file.getContent.getLastModifiedTime),
      size = calculateSize(file),
      tags = List()
    )
  }


  def nameWithoutExtension(file: FileObject) =
    file.getName.getBaseName.replace(s".${file.getName.getExtension}", "")

  
  def decompressName(file: FileObject) = {
    var newName = nameWithoutExtension(file)

    while (newName.equals(file.getName.getBaseName) || file.getParent.getChild(newName) != null) {
      val index = newName.substring(newName.lastIndexOf(" ") + 1, newName.length())
      if (index.forall(Character.isDigit)) {
        val prefix = newName.substring(0, newName.lastIndexOf(" "))
        newName = s"$prefix ${Integer.valueOf(index)+1}"
      }else
        newName = s"$newName 2"
    }
    s"${file.getParent.getName.getURI}/$newName"
  }


  def compressName(file: FileObject, format: String) = {
    var newName = nameWithoutExtension(file)
    while (file.getParent.getChild(s"$newName.$format") != null) {
      val index = newName.substring(newName.lastIndexOf(" ") + 1, newName.length())
      if (index.forall(Character.isDigit)) {
        val prefix = newName.substring(0, newName.lastIndexOf(" "))
        newName = s"$prefix ${Integer.valueOf(index)+1}"
      }else
        newName = s"$newName 2"
    }
    s"${file.getParent.getName.getURI}/$newName.$format"
  }


  def duplicateName(file: FileObject) = {
    var newName = nameWithoutExtension(file)
    val suffix = if (StringUtils.isBlank(file.getName.getExtension)) "" else s".${file.getName.getExtension}"

    while (newName.equals(file.getName.getBaseName) || file.getParent.getChild(s"$newName$suffix") != null) {
      val index = newName.substring(newName.lastIndexOf(" ") + 1, newName.length())
      if (index.forall(Character.isDigit)) {
        val prefix = newName.substring(0, newName.lastIndexOf(" "))
        newName = s"$prefix ${Integer.valueOf(index)+1}"
      }else{
        newName = if (!newName.endsWith(" copy")) s"$newName copy" else s"$newName 2"
      }
    }
    s"${file.getParent.getName.getURI}/$newName$suffix"
  }
}