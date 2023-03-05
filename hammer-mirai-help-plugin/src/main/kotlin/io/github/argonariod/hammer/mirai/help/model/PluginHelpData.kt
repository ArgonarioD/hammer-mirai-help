package io.github.argonariod.hammer.mirai.help.model

import io.github.argonariod.hammer.mirai.core.message.imageFromExternalFiles
import io.github.argonariod.hammer.mirai.core.permission.exactMember
import io.github.argonariod.hammer.mirai.core.permission.exactUser
import io.github.argonariod.hammer.mirai.core.permission.permissionId
import io.github.argonariod.hammer.mirai.help.HammerHelp
import io.github.argonariod.hammer.mirai.help.api.IMAGE_PROTOCOL
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registeredCommands
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.info
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import kotlin.io.path.div


@Serializable
data class PluginHelpData(
    var enable: Boolean = true,
    val names: List<String> = ArrayList(),
    val brief: String = "",
    val displayPermission: String? = null,
    val usage: String = ""
) {
    init {
        require(names.isNotEmpty()) { "names不能为空！" }
    }

    constructor(plugin: Plugin) : this(
        names = listOf(plugin.name),
        brief = plugin.info,
        usage = plugin.registeredCommands
            .mapNotNull { command -> command.usage.takeIf { it.isNotBlank() } }
            .joinToString("\n")
            .takeIf { it.isNotBlank() }
            ?.let { "本条帮助信息由Hammer Help根据其命令自动生成：\n$it" }
            ?: ""
    )

    fun checkPermission(event: MessageEvent): Boolean {
        return displayPermission?.takeIf { it.contains(':') }?.let {
            val permitteeId = when (event) {
                is GroupMessageEvent -> event.exactMember
                else -> event.exactUser
            }
            permitteeId.hasPermission(permissionId(it))
        } ?: true
    }

    suspend fun buildHelpInfo(pluginId: String, event: MessageEvent): Message? {
        if (!enable || !checkPermission(event)) return null
        return if (usage.startsWith(IMAGE_PROTOCOL)) {
            imageFromExternalFiles(
                HammerHelp.configFolderPath / pluginId / usage.substringAfter(IMAGE_PROTOCOL),
                event.subject
            )
        } else {
            (usage.takeIf { it.isNotBlank() }
                ?: "${names.first()}暂无帮助信息！")
                .let { PlainText(it) }
        }
    }
}
