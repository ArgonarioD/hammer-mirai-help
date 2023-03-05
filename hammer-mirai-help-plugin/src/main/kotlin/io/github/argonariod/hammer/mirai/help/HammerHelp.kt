package io.github.argonariod.hammer.mirai.help

import io.github.argonariod.hammer.mirai.core.listener.registerHandlers
import io.github.argonariod.hammer.mirai.help.api.help
import io.github.argonariod.hammer.mirai.help.command.HammerHelpAdminCommand
import io.github.argonariod.hammer.mirai.help.config.CustomHelpInfoPluginConfig
import io.github.argonariod.hammer.mirai.help.config.HammerHelpPluginConfig
import io.github.argonariod.hammer.mirai.help.listener.HelpEventListenerHandler
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.globalEventChannel

object HammerHelp : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.argonariod.hammer-help",
        name = "Hammer Help",
        version = "1.0.0",
    ) {
        author("ArgonarioD")
        info("""帮助Bot主组织各插件的帮助信息""")
    }
) {
    @OptIn(ConsoleExperimentalApi::class)
    override fun onEnable() {
        help {
            names.addAll("hammer-help", "h-help")
            usage = """
                |欢迎使用Hammer Help插件！
                |下文中的帮助指令前缀就是你呼出本帮助信息的指令前缀，如：你的帮助指令前缀为"help"时，你可以通过发送"help"来呼出本帮助信息。
                |<帮助指令前缀> - 查看本帮助信息
                |<帮助指令前缀> list - 查看所有你能够查看帮助信息的插件的列表
                |<帮助指令前缀> <插件名> - 查看插件<插件名>的帮助信息
                |
                |在以下指令中，/hammer-help可以替换为/h-help
                |/hammer-help reload - 重新加载Hammer Help的配置
                |/hammer-help loadDefaults - 令Hammer Help插件加载所有插件的默认帮助信息（不会改变已经存在的自定义配置）
                |/hammer-help resetToDefault - 将指定插件的自定义配置的帮助信息置为默认帮助信息
            """.trimMargin()
        }

        loader.configStorage.run {
            load(this@HammerHelp, HammerHelpPluginConfig)
            load(this@HammerHelp, CustomHelpInfoPluginConfig)
        }

        CommandManager.run {
            registerCommand(HammerHelpAdminCommand)
        }

        globalEventChannel().run {
            registerHandlers(HelpEventListenerHandler)
        }

        logger.info("Hammer Help插件已启用")
    }
}