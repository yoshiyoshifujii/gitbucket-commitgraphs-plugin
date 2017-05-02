package me.huzi.gitbucket.commitgraphs.controller

import scala.collection.JavaConverters._
import scala.sys.process._
import org.eclipse.jgit.api.Git
import gitbucket.core.controller._
import gitbucket.core.model._
import gitbucket.core.service._
import gitbucket.core.util._
import gitbucket.core.util.AdminAuthenticator
import gitbucket.core.util.SyntaxSugars._
import gitbucket.core.util.Directory._
import gitbucket.core.util.Implicits._
import gitbucket.core.util.JGitUtil._
import io.github.gitbucket.scalatra.forms._
import me.huzi.gitbucket.commitgraphs.service.CommitGraphsSettingsService
import me.huzi.gitbucket.commitgraphs.service.CommitGraphsSettingsService._
import me.huzi.gitbucket.commitgraphs.html

class CommitGraphsController extends CommitGraphsControllerBase
  with RepositoryService with AccountService
  with ReferrerAuthenticator with AdminAuthenticator
  with CommitGraphsSettingsService

trait CommitGraphsControllerBase extends ControllerBase {
  self: RepositoryService with AccountService with ReferrerAuthenticator with AdminAuthenticator with CommitGraphsSettingsService =>

  val settingsForm: MappingValueType[CommitGraphsSettings] = mapping(
    "commitGraphsGitCommand"   -> text(required, maxlength(200)),
    "action" -> text(required, maxlength(10))
  )(CommitGraphsSettings.apply)

  get("/admin/commitgraphs")(adminOnly {
      val settings = loadCommitGraphsSettings()
      html.settings(settings.CommitGraphsGitCommand, None)
    }
  )

  post("/admin/commitgraphs", settingsForm)(adminOnly { form =>
    assert(form.CommitGraphsGitCommand != null)
    assert(!form.CommitGraphsGitCommand.isEmpty)
    form.action match {
      case "apply" =>
        saveCommitGraphsSettings(form)
        html.settings(form.CommitGraphsGitCommand, Some("Settings Saved"))
      case _ =>
        if( checkGitCommand(form.CommitGraphsGitCommand) ) {
          html.settings(form.CommitGraphsGitCommand, Some("command check succeded"))
        } else {
          html.settings(form.CommitGraphsGitCommand, Some("no such command in path"))
        }
    }
  })

  get("/:owner/:repository/commitgraphs")(referrersOnly { repository =>
    using(Git.open(getRepositoryDir(repository.owner, repository.name))) { git =>
      if (JGitUtil.isEmpty(git)) {
        html.guide(repository)
      } else {
        try {
          val commitGraphs = git.log.all.call.iterator.asScala.map { rev =>
            val p = Process.apply(
              Seq(loadCommitGraphsSettings().CommitGraphsGitCommand, "log", "-n", "1",
                "--numstat", """--pretty="%H"""", "--source", rev.getId.name), 
              git.getRepository.getDirectory)
            val (additions, deletions) = 
              p.lineStream_!.filter(_.split("\t").length == 3).foldLeft((0, 0)) { (i, l) =>
                val Array(a, d, filename) = l.split("\t")
                a.forall(_.isDigit) && d.forall(_.isDigit) match {
                  case true => (i._1 + a.toInt, i._2 + d.toInt)
                  case _    => i
                }
              }
            CommitCount(
              commit = new CommitInfo(rev),
              additions = additions,
              deletions = deletions)
          }.toSeq.groupBy { x =>
            if (x.commit.isDifferentFromAuthor) (x.commit.committerName, x.commit.committerEmailAddress)
            else (x.commit.authorName, x.commit.authorEmailAddress)
          }.toSeq.sortWith((lt1, lt2) => lt1._2.lengthCompare(lt2._2.length) > 0).map {
            case ((userName, mailAddress), ds) =>
              val dailys = ds.groupBy(x => date2DateStr(x.commit.authorTime)).map {
                case (date, ds) =>
                  val (additions, deletions) = ds.foldLeft((0L, 0L)) { (i, d) =>
                    (i._1 + d.additions, i._2 + d.deletions)
                  }
                  val dh = ds.head
                  DailyCount(
                    userName = dh.commit.authorName,
                    mailAddress = dh.commit.authorEmailAddress,
                    date = dateStr2Date(date),
                    commits = ds.length,
                    additions = additions,
                    deletions = deletions)
              }.toSeq.sortWith((lt1, lt2) => lt1.date.compareTo(lt2.date) < 0)

              val (additions, deletions) = dailys.foldLeft((0L, 0L)) { (i, d) =>
                (i._1 + d.additions, i._2 + d.deletions)
              }
              CommitGraph(
                userName = userName,
                mailAddress = mailAddress,
                commits = ds.length,
                additions = additions,
                deletions = deletions,
                dailys = dailys)
          }
          html.list(repository, commitGraphs)
        } catch {
          case _ : Throwable =>
            html.error(repository)
        }
      }
    }
  })

  private def checkGitCommand(command: String): Boolean = {
    try{ 
      val processBuilder = Process.apply(command)
      if( processBuilder.!(ProcessLogger(line => ())) == 1 ) true else false
    } catch {
      case _ : Throwable => false
    }
  }

  private def date2DateStr(date: java.util.Date): String = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date)
  private def dateStr2Date(date: String): java.util.Date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(date)
}

case class CommitCount(
  commit: CommitInfo,
  additions: Long,
  deletions: Long)

case class DailyCount(
  userName: String,
  mailAddress: String,
  date: java.util.Date,
  commits: Long,
  additions: Long,
  deletions: Long)

case class CommitGraph(
  userName: String,
  mailAddress: String,
  commits: Long,
  additions: Long,
  deletions: Long,
  dailys: Seq[DailyCount])

