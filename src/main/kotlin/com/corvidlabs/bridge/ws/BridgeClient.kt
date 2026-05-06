package com.corvidlabs.bridge.ws

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.security.CapabilityGuard
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

class BridgeClient(
    private val serverUrl: String,
    private val token: String,
    private val projectId: String,
    private val capabilities: CapabilityGuard,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val handler = RequestHandler(capabilities)

    suspend fun connect() {
        val client = HttpClient(CIO) {
            install(WebSockets) {
                pingIntervalMillis = 30_000
            }
        }

        try {
            val wsUrl = serverUrl.trimEnd('/') + "/api/bridge"
            FledgeProtocol.output("Connecting to $wsUrl...\n")

            client.webSocket(wsUrl) {
                authenticate()
                FledgeProtocol.output("Connected. Bridge is active. Press Ctrl+C to disconnect.\n")
                listenForRequests()
            }
        } catch (e: Exception) {
            FledgeProtocol.error("Connection failed: ${e.message}")
        } finally {
            client.close()
            FledgeProtocol.output("Disconnected.\n")
        }
    }

    private suspend fun DefaultClientWebSocketSession.authenticate() {
        val authMsg = AuthMessage(
            token = token,
            projectId = projectId,
            capabilities = CapabilitySet(
                read = capabilities.allowRead,
                write = capabilities.allowWrite,
                exec = capabilities.allowExec,
            ),
        )
        send(Frame.Text(json.encodeToString(AuthMessage.serializer(), authMsg)))
    }

    private suspend fun DefaultClientWebSocketSession.listenForRequests() {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    val response = handleRequest(text)
                    if (response != null) {
                        send(Frame.Text(json.encodeToString(BridgeResponse.serializer(), response)))
                    }
                }
                is Frame.Close -> {
                    FledgeProtocol.info("Server closed connection")
                    return
                }
                else -> {}
            }
        }
    }

    private suspend fun handleRequest(raw: String): BridgeResponse? {
        return try {
            val request = json.decodeFromString(BridgeRequest.serializer(), raw)
            handler.handle(request)
        } catch (e: Exception) {
            FledgeProtocol.error("Failed to parse request: ${e.message}")
            null
        }
    }
}
