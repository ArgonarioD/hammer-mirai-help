package io.github.argonariod.hammer.mirai.help.config

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object HammerHelpPluginConfig : ReadOnlyPluginConfig("config") {
    @ValueDescription(
        """
        本插件帮助指令的前缀名，支持正则表达式
        如：本项配置为"help"时：
            Bot接收到"help"的消息时，将会发送本插件的总览帮助信息
            Bot接收到"help list"的消息时，将会发送所有该发送者能够查看帮助信息的插件的列表
            Bot接收到"help xxx"的消息时，将会发送插件xxx的帮助信息，其中xxx可以是插件的ID、插件的名称、插件的别名
        """
    )
    val helpCommandName: String by value("help")

    @ValueDescription(
        """
        是否允许私聊请求帮助信息
        """
    )
    val enablePrivateMessages: Boolean by value(false)
}