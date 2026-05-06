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
            if (wsUrl.startsWith("ws://") && !wsUrl.startsWith("ws://localhost") && !wsUrl.startsWith("ws://127.0.0.1")) {
                FledgeProtocol.output(
                    "WARNING: Connecting over plaintext ws:// — your token will be visible to anyone " +
                    "on the network path. Use wss:// for any non-loopback server.\n"
                )
            }
            FledgeProtocol.output("Connecting to $wsUrl...\n")

            client.webSocket(wsUrl) {
                authenticate()
                if (!awaitAuthResponse()) {
                    return@webSocket
                }
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

    /**
     * Read the server's first frame after authentication; refuse to enter
     * the request loop unless it is an explicit `auth-ok`. A server (or
     * MITM) that skips this handshake will see the connection close before
     * any file/exec request is processed.
     */
    private suspend fun DefaultClientWebSocketSession.awaitAuthResponse(): Boolean {
        // 10s budget for the server to ack — adjustable later if needed.
        val deadline = System.currentTimeMillis() + 10_000
        for (frame in incoming) {
            if (System.currentTimeMillis() > deadline) {
                FledgeProtocol.error("Auth timeout: server did not respond within 10s")
                return false
            }
            if (frame !is Frame.Text) continue
            val text = frame.readText()
            val resp = try {
                json.decodeFromString(AuthResponse.serializer(), text)
            } catch (_: Exception) {
                FledgeProtocol.error("Auth failed: server sent unrecognized first frame: $text")
                return false
            }
            return when (resp.type) {
                "auth-ok" -> true
                "auth-failed" -> {
                    FledgeProtocol.error("Auth rejected by server: ${resp.reason ?: "(no reason given)"}")
                    false
                }
                else -> {
                    FledgeProtocol.error("Auth failed: expected 'auth-ok' or 'auth-failed', got '${resp.type}'")
                    false
                }
            }
        }
        FledgeProtocol.error("Auth failed: connection closed before server acknowledged auth")
        return false
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
