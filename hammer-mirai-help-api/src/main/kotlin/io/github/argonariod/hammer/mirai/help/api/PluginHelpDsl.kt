package io.github.argonariod.hammer.mirai.help.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.info
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.name
import java.io.FileNotFoundException
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

const val HAMMER_HELP_PLUGIN_ID = "io.github.argonariod.hammer-help"
const val RESOURCE_PROTOCOL = "res://"
const val IMAGE_PROTOCOL = "image://"

val DEFAULT_HELPS_PATH = (PluginManager.pluginsDataPath / HAMMER_HELP_PLUGIN_ID / "defaults").createDirectories()

private val json = Json { prettyPrint = true }

@DslMarker
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class PluginHelpDslMarker

/**
 * 用于配置插件的默认帮助信息，一般在[JvmPlugin.onEnable]中调用，
 * 这些信息作为默认信息留档，真正显示的信息可以被用户通过配置文件的形式手动修改。
 *
 * 其中特别的是，当[usage][PluginHelpDsl.usage]的开头为"res://"，且其后面的内容为一个指向`resources`中的图片的路径时，将会将该图片作为默认帮助信息。
 * 如：`usage = "res://assets/help.jpg"`，将会将`/resources/assets/help.jpg`作为默认帮助信息。其中，受到支持的图片格式有：jpg、png、gif、tif、bmp。
 *
 * 例：
 * ```kotlin
 * override fun onEnable() {
 *   // do something
 *   help {
 *      names.addAll("别名1", "别名2")
 *      brief = "简介"
 *      usage = """
 *      |详细用法
 *      |看看详细用法 - 作用是详细用法
 *      |  子命令 - 用缩进可能会增加可读性
 *      """.trimMargin() // 或者如"res://assets/help.jpg"
 *      permission = "console:command.help" // 不需要权限时不设置即可
 *   }
 *   // do something
 * }
 * ```
 * @property names 插件的名称以及别名，第一个元素会成为插件的名称，其他元素会成为插件的别名，默认第一个元素为[JvmPlugin.name]
 * @property brief 插件的简介，默认为[JvmPlugin.info]
 * @property usage 插件的详细帮助信息，特别信息请看上文文档注释
 * @property permission 显示该帮助信息所需的权限的全名，如"console:command.help"，当不需要权限时为null，默认为null
 * @see PluginHelpDsl
 */
@Suppress("KDocUnresolvedReference")
@PluginHelpDslMarker
inline fun JvmPlugin.help(init: PluginHelpDsl.() -> Unit) {
    PluginHelpDsl(this)
        .apply(init)
        .saveModel()
}


/**
 * @property names 插件的名称以及别名，第一个元素会成为插件的名称，其他元素会成为插件的别名，不修改的情况下下第一个元素为[JvmPlugin.name]。
 * @property brief 插件的简介，默认为[JvmPlugin.info]
 * @property usage 插件的详细帮助信息，特别信息请看[help]的文档注释
 * @property permission 显示该帮助信息所需的权限的全名，如"console:command.help"，当不需要权限时为null，不修改的情况下为null
 * @see help
 */
@Suppress("MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")
@PluginHelpDslMarker
class PluginHelpDsl
@PublishedApi internal constructor(private val plugin: JvmPlugin) {
    var names: MutableList<String> = mutableListOf(plugin.name)
    var brief: String = plugin.info
    var usage: String = ""
    var permission: String? = null

    inline fun <T> MutableList<T>.addAll(vararg names: T) {
        this.addAll(names)
    }

    @PublishedApi
    internal fun saveModel() {
        val pluginId = plugin.id
        require(names.isNotEmpty()) { "names cannot be empty" }
        if (usage.startsWith(RESOURCE_PROTOCOL)) {
            usage.substringAfter(RESOURCE_PROTOCOL).let { resourcePathString ->
                val extension = resourcePathString.substringAfterLast('.')
                    .takeIf { it.isSupportedImageExtension() }
                    ?: throw IllegalArgumentException(UNSUPPORTED_IMAGE_EXTENSION_EXCEPTION_MESSAGE)

                (DEFAULT_HELPS_PATH / "$pluginId.$extension")
                    .outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    .buffered()
                    .use { output ->
                        plugin::class.java.getResourceAsStream("/$resourcePathString")?.transferTo(output)
                            ?: throw FileNotFoundException("Resource /$resourcePathString not found")
                        output.flush()
                    }
                usage = "$IMAGE_PROTOCOL$pluginId.$extension"
            }
        }
        (DEFAULT_HELPS_PATH / "$pluginId.json")
            .writeText(json.encodeToString(DefaultPluginHelpModel(this)))
    }

    private inline fun String.isSupportedImageExtension(): Boolean {
        return this == "jpg" || this == "png" || this == "gif" || this == "tif" || this == "bmp"
    }
}

private const val UNSUPPORTED_IMAGE_EXTENSION_EXCEPTION_MESSAGE =
    "Unsupported Hammer-Help image extension, only support jpg, png, gif, tif, bmp"