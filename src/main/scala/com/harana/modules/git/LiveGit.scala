package com.harana.modules.git

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import org.eclipse.jgit.api.{CreateBranchCommand, Git => JGit}
import org.eclipse.jgit.lib.{ObjectId, Ref}
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import zio.{Task, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable

object LiveGit {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveGit(config, logger, micrometer)
  }
}

case class LiveGit(config: Config, logger: Logger, micrometer: Micrometer) extends Git {

  def clone(uri: String,
            localDirectory: File,
            branch: Option[String] = None,
            username: Option[String] = None,
            password: Option[String] = None,
            oauthToken: Option[String] = None): Task[JGit] =
    for {
      git <- ZIO.succeed {
        val cloneCommand = JGit.cloneRepository().setDirectory(localDirectory).setURI(uri)
        if (branch.nonEmpty) cloneCommand.setBranch(branch.get)
        if (username.nonEmpty && password.nonEmpty) cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username.get, password.get))
        if (oauthToken.nonEmpty) cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(oauthToken.get, ""))
        cloneCommand.call()
      }
    } yield git


  def checkout(git: JGit, branchTagOrCommit: String): Task[Ref] =
    for {
      ref <- ZIO.attempt(git.checkout().setName(branchTagOrCommit).call())
    } yield ref


  def branch(git: JGit, branch: String, track: Boolean = true): Task[Ref] =
    ZIO.attempt {
      git
        .checkout
        .setCreateBranch(true)
        .setName(branch)
        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
        .setStartPoint("origin/" + branch)
        .call()
    }


  def refresh(git: JGit): Task[Unit] =
    for {
      _ <- ZIO.attempt(git.pull().call())
    } yield ()


  def hasChanged(git: JGit): Task[Boolean] =
    for {
      prevCommit  <- mostRecentCommitHash(git)
      _           <- refresh(git)
      newCommit   <- mostRecentCommitHash(git)
      changed     =  prevCommit.isEmpty && newCommit.nonEmpty || prevCommit.nonEmpty && newCommit.nonEmpty && prevCommit != newCommit
    } yield changed


  def mostRecentCommitHash(git: JGit): Task[Option[String]] =
    for {
      it    <- ZIO.attempt(git.log.setMaxCount(1).call.iterator()).option
      hash  = if (it.nonEmpty && it.get.hasNext) Some(it.get.next().getName) else None
    } yield hash


  def filesForCommit(git: JGit, hash: String): Task[List[File]] = {
    ZIO.attempt {
      val treeWalk = new TreeWalk(git.getRepository)
      treeWalk.reset(ObjectId.fromString(hash))

      val paths = mutable.ListBuffer[File]()

      while (treeWalk.next) paths += new File(treeWalk.getPathString)
      if (treeWalk != null) treeWalk.close()

      paths.toList
    }
  }


  def latestFiles(git: JGit): Task[List[File]] =
    for {
      hash    <- mostRecentCommitHash(git)
      files   <- ZIO.ifZIO(ZIO.succeed(hash.nonEmpty))(filesForCommit(git, hash.get), ZIO.attempt(List()))
      _       <- logger.debug(s"Latest files end")
    } yield files
}