package me.huzi.gitbucket.commitgraphs.controller

import scala.collection.JavaConverters._
import scala.sys.process._
import org.eclipse.jgit.api.Git
import gitbucket.core.controller._
import gitbucket.core.model._
import gitbucket.core.service._
import gitbucket.core.util._
import gitbucket.core.util.ControlUtil._
import gitbucket.core.util.Directory._
import gitbucket.core.util.Implicits._
import me.huzi.gitbucket.commitgraphs.html

class CommitGraphsController extends CommitGraphsControllerBase
  with RepositoryService with AccountService
  with ReferrerAuthenticator

trait CommitGraphsControllerBase extends ControllerBase {
  self: RepositoryService with AccountService with ReferrerAuthenticator =>

  get("/:owner/:repository/graphs")(referrersOnly { repository =>
    if (repository.commitCount == 0) {
      html.guide(repository)
    } else {
      using(Git.open(getRepositoryDir(repository.owner, repository.name))) { git =>
        val commitGraphs = git.log.all.call.iterator.asScala.map { rev =>
          val p = Process.apply(Seq("git", "log", "-n", "1", "--numstat", """--pretty="%H"""", "--source", rev.getId.name), git.getRepository.getDirectory)
          val (additions, deletions) = p.lineStream_!.filter(_.split("\t").length == 3).foldLeft((0, 0)) { (i, l) =>
            val Array(a, d, filename) = l.split("\t")
            (a.forall(_.isDigit) && d.forall(_.isDigit)) match {
              case true => (i._1 + a.toInt, i._2 + d.toInt)
              case _    => i
            }
          }
          DailyCount(
            userName = rev.getAuthorIdent.getName,
            mailAddress = rev.getAuthorIdent.getEmailAddress,
            date = rev.getAuthorIdent.getWhen,
            commits = 1,
            additions = additions,
            deletions = deletions)
        }.toSeq.groupBy(x => x.mailAddress).toSeq.sortWith((lt1, lt2) => lt1._2.lengthCompare(lt2._2.length) > 0).map {
          case (m, ds) =>
            val dailys = ds.groupBy(x => date2DateStr(x.date)).map {
              case (date, ds) =>
                val (additions, deletions) = ds.foldLeft((0L, 0L)) { (i, d) =>
                  (i._1 + d.additions, i._2 + d.deletions)
                }
                val dh = ds.head
                DailyCount(
                  userName = dh.userName,
                  mailAddress = dh.mailAddress,
                  date = dateStr2Date(date),
                  commits = ds.length,
                  additions = additions,
                  deletions = deletions)
            }.toSeq.sortWith((lt1, lt2) => lt1.date.compareTo(lt2.date) < 0)

            val (additions, deletions) = dailys.foldLeft((0L, 0L)) { (i, d) =>
              (i._1 + d.additions, i._2 + d.deletions)
            }
            CommitGraph(
              userName = dailys.head.userName,
              mailAddress = m,
              commits = ds.length,
              additions = additions,
              deletions = deletions,
              dailys = dailys)
        }.toSeq

        html.list(repository, commitGraphs)
      }
    }
  })

  private def date2DateStr(date: java.util.Date): String = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date)
  private def dateStr2Date(date: String): java.util.Date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(date)
}

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

