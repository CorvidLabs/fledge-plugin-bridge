package com.corvidlabs.bridge.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class InitMessage(
    val type: String,
    val protocol: String,
    val args: List<String> = emptyList(),
    val project: ProjectInfo? = null,
    val plugin: PluginInfo? = null,
    val capabilities: Capabilities? = null,
)

@Serializable
data class ProjectInfo(
    val name: String? = null,
    val root: String? = null,
    val language: String? = null,
)

@Serializable
data class PluginInfo(
    val name: String? = null,
    val version: String? = null,
    val dir: String? = null,
)

@Serializable
data class Capabilities(
    val exec: Boolean = false,
    val store: Boolean = false,
    val metadata: Boolean = false,
)

object FledgeProtocol {

    fun readInit(): InitMessage? {
        return try {
            val line = readlnOrNull() ?: return null
            val msg = json.decodeFromString<InitMessage>(line)
            if (msg.type == "init") msg else null
        } catch (_: Exception) {
            null
        }
    }

    fun output(text: String) {
        val msg = """{"type":"output","text":${Json.encodeToString(text)}}"""
        println(msg)
        System.out.flush()
    }

    fun log(level: String, message: String) {
        val msg = """{"type":"log","level":${Json.encodeToString(level)},"message":${Json.encodeToString(message)}}"""
        println(msg)
        System.out.flush()
    }

    fun error(message: String) = log("error", message)
    fun info(message: String) = log("info", message)
}
