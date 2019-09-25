package me.huzi.gitbucket.commitgraphs.service

import java.io.File
import scala.util.Using
import gitbucket.core.util.Directory._
import gitbucket.core.util.SyntaxSugars._
import me.huzi.gitbucket.commitgraphs.service.CommitGraphsSettingsService._

trait CommitGraphsSettingsService {

  val CommitGraphsConf = new File(GitBucketHome, "commitgraphs.conf")

  def saveCommitGraphsSettings(settings: CommitGraphsSettings): Unit =
    defining(new java.util.Properties()) { props =>
      props.setProperty(CommitGraphsGitCommand, settings.CommitGraphsGitCommand)
      Using.resource(new java.io.FileOutputStream(CommitGraphsConf)) { out =>
        props.store(out, null)
      }
    }

  def loadCommitGraphsSettings(): CommitGraphsSettings =
    defining(new java.util.Properties()) { props =>
      if (CommitGraphsConf.exists) {
        Using.resource(new java.io.FileInputStream(CommitGraphsConf)) { in =>
          props.load(in)
        }
      }
      CommitGraphsSettings(
        getValue[String](props, CommitGraphsGitCommand, "git"),
        "check"
      )
    }
}

object CommitGraphsSettingsService {
  import scala.reflect.ClassTag

  case class CommitGraphsSettings(CommitGraphsGitCommand: String,action: String)

  private val CommitGraphsGitCommand = "CommitGraphsGitCommand"
  private val action = "action"

  private def getValue[A: ClassTag](props: java.util.Properties,
                                    key: String,
                                    default: A): A =
    defining(props.getProperty(key)) { value =>
      if (value == null || value.isEmpty) default
      else convertType(value).asInstanceOf[A]
    }

  private def getOptionValue[A: ClassTag](props: java.util.Properties,
                                          key: String,
                                          default: Option[A]): Option[A] =
    defining(props.getProperty(key)) { value =>
      if (value == null || value.isEmpty) default
      else Some(convertType(value)).asInstanceOf[Option[A]]
    }

  private def convertType[A: ClassTag](value: String) =
    defining(implicitly[ClassTag[A]].runtimeClass) { c =>
      if (c == classOf[Boolean]) value.toBoolean
      else if (c == classOf[Int]) value.toInt
      else value
    }
}
