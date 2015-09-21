package me.huzi.gitbucket.commitgraphs.controller

import scala.collection.JavaConverters._
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
    using(Git.open(getRepositoryDir(repository.owner, repository.name))) { git =>
      val personIter = git.log.all.call.iterator.asScala.map { rev =>
        println(rev.getAuthorIdent.getWhen)
        rev.getAuthorIdent
      }.toSeq.groupBy(x => (x.getEmailAddress, x.getName)).toSeq
      val persons = personIter.sortWith {
        case (t1, t2) =>
          t1._2.length > t2._2.length
      }
      html.list(repository, persons)
    }
  })

}