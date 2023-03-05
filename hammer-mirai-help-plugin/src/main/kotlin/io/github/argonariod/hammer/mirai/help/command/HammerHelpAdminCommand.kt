package io.github.argonariod.hammer.mirai.help.command

import io.github.argonariod.hammer.mirai.help.HammerHelp
import io.github.argonariod.hammer.mirai.help.HammerHelp.reload
import io.github.argonariod.hammer.mirai.help.config.CustomHelpInfoPluginConfig
import io.github.argonariod.hammer.mirai.help.config.HammerHelpPluginConfig
import io.github.argonariod.hammer.mirai.help.listener.HelpEventListenerHandler
import io.github.argonariod.hammer.mirai.help.model.PluginHelpData
import io.github.argonariod.hammer.mirai.core.message.quoteReply
import net.mamoe.mirai.console.command.CommandContext
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.id

object HammerHelpAdminCommand : CompositeCommand(
    HammerHelp, "hammer-help", "h-help",
    description = "Hammer Help Admin Commands"
) {
    @SubCommand
    @Description("重新加载Hammer Help的配置")
    suspend fun reload(context: CommandContext) {
        HammerHelpPluginConfig.reload()
        CustomHelpInfoPluginConfig.reload()
        HelpEventListenerHandler.reload()
        context.quoteReply("成功地重新加载了Hammer Help的配置！")
    }

    @SubCommand
    @Description("令Hammer Help插件加载所有插件的默认帮助信息")
    suspend fun loadDefaults(context: CommandContext) {
        val differentInfoPlugins = mutableListOf<String>()
        PluginManager.plugins
            .forEach { plugin ->
                val pluginId = plugin.id
                CustomHelpInfoPluginConfig
                    .getFromDefault(pluginId)
                    ?.let { helpData ->
                        CustomHelpInfoPluginConfig.putIfAbsent(pluginId, helpData) { pluginId, existing ->
                            if (existing != helpData.apply { enable = existing.enable }) {
                                differentInfoPlugins += pluginId
                            }
                        }
                    }
                    ?: run {
                        CustomHelpInfoPluginConfig.putIfAbsent(pluginId, PluginHelpData(plugin))
                    }
            }
        differentInfoPlugins
            .takeIf { it.isNotEmpty() }
            ?.also {
                context.quoteReply("有部分插件的默认帮助信息与自定义配置帮助信息不同，请注意检查控制台输出！")
                HammerHelp.logger.warning("以下插件的默认帮助信息与自定义配置的帮助信息不同，请注意检查插件帮助信息是否有变动并决定应用到自定义配置中：")
            }
            ?.forEach { HammerHelp.logger.warning(it) }
        HelpEventListenerHandler.reload()
        context.quoteReply("成功地加载了所有的默认帮助信息！")
    }

    @SubCommand
    @Description("将指定插件的自定义配置的帮助信息置为默认帮助信息")
    suspend fun resetToDefault(context: CommandContext, pluginId: String) {
        CustomHelpInfoPluginConfig
            .getFromDefault(pluginId)
            ?.let {
                CustomHelpInfoPluginConfig[pluginId] = it
                context.quoteReply("成功地将插件 $pluginId 的自定义配置的帮助信息置为默认帮助信息！")
            }
            ?: context.quoteReply("插件 $pluginId 不存在或没有默认帮助信息！")
        HelpEventListenerHandler.reload()
    }
}