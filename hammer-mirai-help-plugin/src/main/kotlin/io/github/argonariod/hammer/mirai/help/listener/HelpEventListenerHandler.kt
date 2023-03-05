package io.github.argonariod.hammer.mirai.help.listener

import io.github.argonariod.hammer.mirai.core.listener.EventListenerHandler
import io.github.argonariod.hammer.mirai.core.message.quote
import io.github.argonariod.hammer.mirai.help.HammerHelp
import io.github.argonariod.hammer.mirai.help.config.CustomHelpInfoPluginConfig
import io.github.argonariod.hammer.mirai.help.config.HammerHelpPluginConfig
import io.github.argonariod.hammer.mirai.help.model.PluginHelpData
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages

object HelpEventListenerHandler : EventListenerHandler<MessageEvent> {

    private lateinit var targetEventChannel: EventChannel<MessageEvent>
    private lateinit var helpCommandListeners: List<Listener<MessageEvent>>
    private lateinit var pluginNamesToId: Map<String, String>

    init {
        loadCache()
    }

    override fun registerSubscribeHandler(eventChannel: EventChannel<MessageEvent>) {
        targetEventChannel = eventChannel
        registerHelpCommands()
    }

    private fun registerHelpCommands() {
        check(HammerHelpPluginConfig.helpCommandName.isNotEmpty()) { "help-command-name不能为空" }
        check(!HammerHelpPluginConfig.helpCommandName
            .let { it.endsWith('$') || it.endsWith('*') || it.endsWith('+') }) {
            "help-command-name不能以'$'、'*'或'+'结尾"
        }

        val prefix = HammerHelpPluginConfig.helpCommandName.let {
            if (!it.startsWith('^')) "^$it" else it
        }
        val listeners = mutableListOf<Listener<MessageEvent>>()
        targetEventChannel.subscribeMessages(priority = EventPriority.HIGHEST) {
            (Regex(prefix) matchingReply reply@{
                checkWhetherCanTriggerHelp { return@reply Unit }
                this.intercept()
                return@reply CustomHelpInfoPluginConfig[HammerHelp.id]!!
                    .buildHelpInfo(HammerHelp.id, this)
                    .quote(this)
            }).let {
                listeners.add(it)
            }

            (Regex("$prefix list") matchingReply reply@{
                checkWhetherCanTriggerHelp { return@reply Unit }
                this.intercept()
                return@reply CustomHelpInfoPluginConfig
                    .getAllEnablePlugins()
                    .entries
                    .filter { (_, pluginInfo) -> pluginInfo.checkPermission(this) }
                    .buildHelpListInfo()
                    .quote(this)
            }).let {
                listeners.add(it)
            }

            (Regex("$prefix (?!list)(.*)") matchingReply reply@{ matched ->
                checkWhetherCanTriggerHelp { return@reply Unit }
                val targetPluginId = pluginNamesToId[matched.groupValues.last()] ?: return@reply Unit
                this.intercept()
                return@reply CustomHelpInfoPluginConfig[targetPluginId]
                    ?.buildHelpInfo(pluginNamesToId[matched.groupValues[1]]!!, this)
                    ?.quote(this)
                    ?: Unit
            }).let {
                listeners.add(it)
            }
        }
        helpCommandListeners = listeners
    }

    private inline fun MessageEvent.checkWhetherCanTriggerHelp(onFailed: () -> Unit) {
        if (!HammerHelpPluginConfig.enablePrivateMessages && this !is GroupMessageEvent) {
            onFailed()
        }
    }

    private fun Collection<Map.Entry<String, PluginHelpData>>.buildHelpListInfo(): String {
        if (this.isEmpty()) {
            return "您没有可用的插件帮助信息"
        }
        return this.joinToString("\n\n") { (pluginId, pluginInfo) ->
            val pluginName = pluginInfo.names.first()
            buildString {
                append(pluginName)
                append("（")
                append(pluginId)
                append("）")
                if (pluginInfo.brief.isNotBlank()) {
                    appendLine()
                    append(pluginInfo.brief)
                }
                if (pluginInfo.names.size > 1) {
                    appendLine()
                    append("别名：")
                    append(pluginInfo.names.subList(1, pluginInfo.names.size).joinToString("、"))
                }
            }
        }
    }

    private fun loadCache() {
        pluginNamesToId = CustomHelpInfoPluginConfig
            .getAllEnablePlugins()
            .flatMap { (pluginId, pluginInfo) ->
                pluginInfo.names
                    .map { it to pluginId } + (pluginId to pluginId)
            }.toMap()
    }

    internal fun reload() {
        helpCommandListeners.forEach { it.complete() }
        loadCache()
        registerHelpCommands()
    }
}