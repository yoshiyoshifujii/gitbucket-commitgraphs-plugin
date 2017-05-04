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
    new Version("4.9.0"),
    new Version("4.10.0"),
    new Version("4.11.0"),
    new Version("4.12.0")
  )

  override val controllers = Seq(
    "/*" -> new CommitGraphsController
  )

  override val repositoryMenus = Seq(
    (repository: RepositoryInfo, context: Context) =>
      Some(Link(
        id = pluginId,
        label = pluginName,
        path = s"/commitgraphs",
        icon = Some("menu-icon octicon octicon-graph")
      ))
  )

  override val systemSettingMenus: Seq[(Context) => Option[Link]] = Seq(
    (ctx: Context) => Some(Link("Commit Graph","Commit Graph","admin/commitgraphs"))
  )

}
