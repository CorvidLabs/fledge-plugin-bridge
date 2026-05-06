package com.corvidlabs.bridge

import com.corvidlabs.bridge.security.CapabilityGuard
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapabilityGuardTest {

    private fun guard(
        read: Boolean = true,
        write: Boolean = false,
        exec: Boolean = false,
        sandbox: String = "/tmp/test-sandbox",
    ) = CapabilityGuard(
        allowRead = read,
        allowWrite = write,
        allowExec = exec,
        sandboxRoot = sandbox,
    )

    @Test
    fun `assertRead succeeds when allowed`() {
        guard(read = true).assertRead()
    }

    @Test
    fun `assertRead fails when not allowed`() {
        assertThrows<IllegalStateException> {
            guard(read = false).assertRead()
        }
    }

    @Test
    fun `assertWrite fails by default`() {
        assertThrows<IllegalStateException> {
            guard().assertWrite()
        }
    }

    @Test
    fun `assertExec fails by default`() {
        assertThrows<IllegalStateException> {
            guard().assertExec()
        }
    }

    @Test
    fun `validatePath allows path within sandbox`() {
        val g = guard(sandbox = "/tmp/test-sandbox")
        val result = g.validatePath("src/Main.kt")
        assertTrue(result.toString().endsWith("src/Main.kt"))
        assertTrue(result.startsWith(g.sandboxPath))
    }

    @Test
    fun `validatePath rejects path traversal`() {
        val g = guard(sandbox = "/tmp/test-sandbox")
        assertThrows<IllegalArgumentException> {
            g.validatePath("../../etc/passwd")
        }
    }

    @Test
    fun `validateCommand allows safe commands`() {
        val g = guard(exec = true)
        assertEquals("gradle build", g.validateCommand("gradle build"))
    }

    @Test
    fun `validateCommand blocks destructive commands`() {
        val g = guard(exec = true)
        assertThrows<IllegalArgumentException> {
            g.validateCommand("rm -rf /")
        }
    }
}
