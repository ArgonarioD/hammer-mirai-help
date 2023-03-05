package io.github.argonariod.hammer.mirai.help.api

import kotlinx.serialization.Serializable

@Serializable
data class DefaultPluginHelpModel(
    var names: List<String>,
    var brief: String,
    var usage: String,
    var permission: String?,
) {
    @PublishedApi
    internal constructor(dsl: PluginHelpDsl) : this(
        dsl.names,
        dsl.brief,
        dsl.usage,
        dsl.permission,
    )
}