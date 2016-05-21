import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService.SystemSettings
import io.github.gitbucket.solidbase.model.Version
import javax.servlet.ServletContext
import me.huzi.gitbucket.commitgraphs.controller.CommitGraphsController

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "commitgraphs"

  override val pluginName: String = "Commit Graphs"

  override val description: String = "Viewing the commit count in the graph."

  override val versions: Seq[Version] = Seq(
    new Version("4.0.0"),
    new Version("3.12"),
    new Version("1.0"))

  override val controllers = Seq(
    "/*" -> new CommitGraphsController
  )

  override def javaScripts(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Seq[(String, String)] = {
    // Add Snippet link to the header
    val path = settings.baseUrl.getOrElse(context.getContextPath)
    Seq(
      ".*/(?!graphs)[^/]*" -> s"""
        |$$('div.main-sidebar ul:not(ul#system-admin-menu-container)').map(function(i) {
        |  var owner = $$("input[type=hidden][name=owner]").val();
        |  var repository = $$("input[type=hidden][name=repository]").val();
        |  if (owner && repository) {
        |    var s = $$(this);
        |    var lc = s.children(':last').remove().clone();
        |    s.append(
        |      $$('<li></li>').append(
        |        $$('<a><i class="menu-icon octicon octicon-graph"></i> Commit Graphs</a>').attr('href', '${path}/' + owner + '/' + repository + '/graphs')
        |      )
        |    );
        |    s.append(lc);
        |  }
        |});
      """.stripMargin,
      ".*/graphs" -> s"""
        |$$('div.main-sidebar ul:not(ul#system-admin-menu-container)').map(function(i) {
        |  var owner = $$("input[type=hidden][name=owner]").val();
        |  var repository = $$("input[type=hidden][name=repository]").val();
        |  if (owner && repository) {
        |    var s = $$(this);
        |    var lc = s.children(':last').remove().clone();
        |    s.append(
        |      $$('<li class="active"></li>').append(
        |        $$('<a><i class="menu-icon octicon octicon-graph"></i> Commit Graphs</a>').attr('href', '${path}/' + owner + '/' + repository + '/graphs')
        |      )
        |    );
        |    s.append(lc);
        |  }
        |});
      """.stripMargin)
  }

}
