import gitbucket.core.plugin.Link
import io.github.gitbucket.solidbase.model.Version
import gitbucket.core.controller.Context
import gitbucket.core.service.RepositoryService.RepositoryInfo
import me.huzi.gitbucket.commitgraphs.controller.CommitGraphsController

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "commitgraphs"

  override val pluginName: String = "Commit Graphs"

  override val description: String = "Viewing the commit count in the graph."

  override val versions: Seq[Version] = Seq(
    new Version("1.0"),
    new Version("3.12"),
    new Version("4.0.0"),
    new Version("4.5.0"),
    new Version("4.9.0"))

  override val controllers = Seq(
    "/*" -> new CommitGraphsController
  )

  override val repositoryMenus = Seq(
    (repository: RepositoryInfo, context: Context) =>
      Some(Link(
        id = "CommitGraphs",
        label = "Commit Graphs",
        path = s"/graphs",
        icon = Some("menu-icon octicon octicon-graph")
      ))
  )

}
