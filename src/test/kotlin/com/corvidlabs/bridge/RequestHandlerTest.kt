package com.corvidlabs.bridge

import com.corvidlabs.bridge.security.CapabilityGuard
import com.corvidlabs.bridge.ws.BridgeRequest
import com.corvidlabs.bridge.ws.RequestHandler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequestHandlerTest {

    private fun handler(
        read: Boolean = true,
        write: Boolean = false,
        exec: Boolean = false,
    ): RequestHandler {
        val guard = CapabilityGuard(
            allowRead = read,
            allowWrite = write,
            allowExec = exec,
            sandboxRoot = "/tmp/bridge-test",
        )
        return RequestHandler(guard)
    }

    @Test
    fun `ping returns pong`() = runTest {
        val h = handler()
        val response = h.handle(BridgeRequest(id = "1", type = "ping"))
        assertEquals("pong", response.type)
        assertTrue(response.success)
    }

    @Test
    fun `unknown type returns error`() = runTest {
        val h = handler()
        val response = h.handle(BridgeRequest(id = "2", type = "unknown"))
        assertFalse(response.success)
        assertEquals("error", response.type)
    }

    @Test
    fun `file read without capability returns error`() = runTest {
        val h = handler(read = false)
        val response = h.handle(BridgeRequest(id = "3", type = "file.read", path = "test.txt"))
        assertFalse(response.success)
    }

    @Test
    fun `exec without capability returns error`() = runTest {
        val h = handler(exec = false)
        val response = h.handle(BridgeRequest(id = "4", type = "exec", command = "ls"))
        assertFalse(response.success)
    }
}
