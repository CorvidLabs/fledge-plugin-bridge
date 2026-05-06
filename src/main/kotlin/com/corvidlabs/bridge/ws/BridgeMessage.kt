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

/**
 * Server's response to the `auth` frame. Either `auth-ok` (proceed) or
 * `auth-failed` (terminate). The plugin reads exactly one of these as the
 * first frame after sending its credentials and refuses to enter the
 * request loop until it does — this is what protects against a server (or
 * MITM) that skips authentication entirely.
 */
@Serializable
data class AuthResponse(
    val type: String,
    val reason: String? = null,
)
