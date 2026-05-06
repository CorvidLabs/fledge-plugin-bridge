package com.corvidlabs.bridge.ws

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BridgeRequest(
    val id: String,
    val type: String,
    val path: String? = null,
    val content: String? = null,
    val pattern: String? = null,
    val command: String? = null,
    val cwd: String? = null,
    val timeout: Long? = null,
)

@Serializable
data class BridgeResponse(
    val id: String,
    val type: String,
    val success: Boolean,
    val data: JsonElement? = null,
    val error: String? = null,
)

@Serializable
data class AuthMessage(
    val type: String = "auth",
    val token: String,
    val projectId: String = "",
    val capabilities: CapabilitySet,
)

@Serializable
data class CapabilitySet(
    val read: Boolean = true,
    val write: Boolean = false,
    val exec: Boolean = false,
)
