package io.github.argonariod.hammer.mirai.help.config

import io.github.argonariod.hammer.mirai.help.HammerHelp
import io.github.argonariod.hammer.mirai.help.api.DEFAULT_HELPS_PATH
import io.github.argonariod.hammer.mirai.help.api.DefaultPluginHelpModel
import io.github.argonariod.hammer.mirai.help.api.IMAGE_PROTOCOL
import io.github.argonariod.hammer.mirai.help.model.PluginHelpData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import kotlin.io.path.*

object CustomHelpInfoPluginConfig : AutoSavePluginConfig("custom-helps") {
    @ValueDescription(
        """
        自定义帮助信息，
        enable是该插件的帮助信息是否启用，当其为false时，该插件不会出现在帮助列表中，也不会响应对于该插件的详细帮助查询。
        names是该插件的名称及别名，该项的值是一个字符串列表。
        brief是该插件的简介，会显示在插件帮助列表中。
        displayPermissionId是显示该插件的帮助信息所需的权限，当其为null时，代表不需要权限。
        usage是该插件的帮助信息。
            特别的是，当其为"image://"开头时，代表该插件的帮助信息为一张图片。
            如，当Hammer Help插件的usage为"image://default.jpg"时，
               Hammer Help的帮助信息将为该data文件夹下的"./io.github.argonariod.hammer-help/default.jpg"图片
        """
    )
    val plugins: MutableMap<String, PluginHelpData> by value()

    operator fun get(pluginId: String): PluginHelpData? {
        return plugins.getOrPut(pluginId) {
            getFromDefault(pluginId) ?: return null
        }
    }

    fun getAllEnablePlugins(): Map<String, PluginHelpData> {
        return plugins.filterValues { it.enable }
    }

    operator fun set(pluginId: String, pluginHelpData: PluginHelpData) {
        plugins[pluginId] = pluginHelpData
    }

    inline fun putIfAbsent(
        pluginId: String,
        pluginHelpData: PluginHelpData,
        orElse: (pluginId: String, existing: PluginHelpData) -> Unit = { _, _ -> }
    ) {
        if (pluginId !in plugins) {
            plugins[pluginId] = pluginHelpData
        } else {
            orElse(pluginId, plugins[pluginId]!!)
        }
    }

    fun getFromDefault(pluginId: String): PluginHelpData? {
        return (DEFAULT_HELPS_PATH / "$pluginId.json")
            .takeIf { it.exists() }
            ?.useLines {
                Json.decodeFromString<DefaultPluginHelpModel>(it.joinToString(""))
            }
            ?.let { default ->
                default.usage
                    .takeIf { it.startsWith(IMAGE_PROTOCOL) }
                    ?.substringAfter(IMAGE_PROTOCOL)
                    ?.also { imageFileName ->
                        (DEFAULT_HELPS_PATH / imageFileName).copyTo(
                            (HammerHelp.configFolderPath / pluginId / imageFileName).apply { parent.createDirectories() },
                            overwrite = true
                        )
                    }

                PluginHelpData(
                    true,
                    default.names,
                    default.brief,
                    default.permission,
                    default.usage,
                )
            }
    }
}