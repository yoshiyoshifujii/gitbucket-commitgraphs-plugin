

import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService.SystemSettings
import gitbucket.core.util.Version
import javax.servlet.ServletContext
import me.huzi.gitbucket.commitgraphs.controller.CommitGraphsController

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "commitgraphs"

  override val pluginName: String = "Commit Graphs"

  override val description: String = "Viewing the commit count in the graph."

  override val versions: Seq[Version] = Seq(
    Version(1, 0))

  override val controllers = Seq(
    "/*" -> new CommitGraphsController
  )

  override def javaScripts(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Seq[(String, String)] = {
    // Add Snippet link to the header
    val path = settings.baseUrl.getOrElse(context.getContextPath)
    Seq(
      "/[^/]*?/[^/]*?" -> s"""
        |$$('ul.sidemenu .menu-icon.octicon-book').parents('ul.sidemenu').map(function(i) {
        |  var owner = $$("input[type=hidden][name=owner]").val();
        |  var repository = $$("input[type=hidden][name=repository]").val();
        |  var s = $$(this);
        |  var lc = s.children(':last').remove().clone();
        |  s.append(
        |    $$('<li>').append(
        |      $$('<a><i class="menu-icon octicon octicon-graph"></i> Commit Graphs</a></li>').attr('href', '${path}/' + owner + '/' + repository + '/graphs')
        |    )
        |  );
        |  s.append(lc);
        |});
      """.stripMargin,
      "/[^/]*?/[^/]*?/(?!graphs).*" -> s"""
        |$$('ul.sidemenu .menu-icon.octicon-book').parents('ul.sidemenu').map(function(i) {
        |  var owner = $$("input[type=hidden][name=owner]").val();
        |  var repository = $$("input[type=hidden][name=repository]").val();
        |  var s = $$(this);
        |  var lc = s.children(':last').remove().clone();
        |  s.append(
        |    $$('<li data-toggle="tooltip" data-placement="left" data-original-title="Commit Graphs"></li>').append(
        |      $$('<a href=""><i class="menu-icon octicon octicon-graph"></i></a>').attr('href', '${path}/' + owner + '/' + repository + '/graphs')
        |    )
        |  );
        |  s.append(lc);
        |});
      """.stripMargin,
            "/[^/]*?/[^/]*?/graphs" -> s"""
        |$$('ul.sidemenu .menu-icon.octicon-book').parents('ul.sidemenu').map(function(i) {
        |  var owner = $$("input[type=hidden][name=owner]").val();
        |  var repository = $$("input[type=hidden][name=repository]").val();
        |  var s = $$(this);
        |  var lc = s.children(':last').remove().clone();
        |  s.append(
        |    $$('<li class="active" data-toggle="tooltip" data-placement="left" data-original-title="Commit Graphs"></li>').append(
        |      $$('<a href=""><i class="menu-icon menu-icon-active octicon octicon-graph"></i></a>').attr('href', '${path}/' + owner + '/' + repository + '/graphs')
        |    )
        |  );
        |  s.append(lc);
        |});
      """.stripMargin)
  }

}