package com.fastclock.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocolTest {

    // ── Protocol.fromLabel ──

    @Test
    fun `fromLabel - known labels`() {
        assertEquals(Protocol.P16_8, Protocol.fromLabel("16:8"))
        assertEquals(Protocol.P18_6, Protocol.fromLabel("18:6"))
        assertEquals(Protocol.P20_4, Protocol.fromLabel("20:4"))
        assertEquals(Protocol.P23_1, Protocol.fromLabel("23:1"))
        assertEquals(Protocol.CUSTOM, Protocol.fromLabel("custom"))
    }

    @Test
    fun `fromLabel - unknown label falls back to P18_6`() {
        assertEquals(Protocol.P18_6, Protocol.fromLabel("unknown"))
        assertEquals(Protocol.P18_6, Protocol.fromLabel(""))
        assertEquals(Protocol.P18_6, Protocol.fromLabel("12:12"))
    }

    // ── ResolvedProtocol.resolve for presets ──

    @Test
    fun `resolve - P16_8`() {
        val r = ResolvedProtocol.resolve(Protocol.P16_8, 0, 0)
        assertEquals(16, r.fastHours)
        assertEquals(8, r.eatHours)
        assertEquals(12, r.eatStart)
        assertEquals(20, r.eatEnd)
    }

    @Test
    fun `resolve - P18_6`() {
        val r = ResolvedProtocol.resolve(Protocol.P18_6, 0, 0)
        assertEquals(18, r.fastHours)
        assertEquals(6, r.eatHours)
        assertEquals(12, r.eatStart)
        assertEquals(18, r.eatEnd)
    }

    @Test
    fun `resolve - P20_4`() {
        val r = ResolvedProtocol.resolve(Protocol.P20_4, 0, 0)
        assertEquals(20, r.fastHours)
        assertEquals(4, r.eatHours)
        assertEquals(12, r.eatStart)
        assertEquals(16, r.eatEnd)
    }

    @Test
    fun `resolve - P23_1`() {
        val r = ResolvedProtocol.resolve(Protocol.P23_1, 0, 0)
        assertEquals(23, r.fastHours)
        assertEquals(1, r.eatHours)
        assertEquals(12, r.eatStart)
        assertEquals(13, r.eatEnd)
    }

    // ── ResolvedProtocol.resolve for CUSTOM ──

    @Test
    fun `resolve - CUSTOM normal hours (12 to 18)`() {
        val r = ResolvedProtocol.resolve(Protocol.CUSTOM, 12, 18)
        assertEquals(6, r.eatHours)
        assertEquals(18, r.fastHours)
        assertEquals(12, r.eatStart)
        assertEquals(18, r.eatEnd)
    }

    @Test
    fun `resolve - CUSTOM wrap-around (20 to 4)`() {
        val r = ResolvedProtocol.resolve(Protocol.CUSTOM, 20, 4)
        // eatH = (4 - 20 + 24) % 24 = 8
        assertEquals(8, r.eatHours)
        assertEquals(16, r.fastHours)
        assertEquals(20, r.eatStart)
        assertEquals(4, r.eatEnd)
    }

    @Test
    fun `resolve - CUSTOM start equals end (eatH becomes 24)`() {
        val r = ResolvedProtocol.resolve(Protocol.CUSTOM, 12, 12)
        // eatH = (12 - 12 + 24) % 24 = 0 → corrected to 24
        assertEquals(24, r.eatHours)
        assertEquals(0, r.fastHours)
        assertEquals(12, r.eatStart)
        assertEquals(12, r.eatEnd)
    }

    @Test
    fun `resolve - CUSTOM full day eating (0 to 0)`() {
        val r = ResolvedProtocol.resolve(Protocol.CUSTOM, 0, 0)
        // eatH = (0 - 0 + 24) % 24 = 0 → corrected to 24
        assertEquals(24, r.eatHours)
        assertEquals(0, r.fastHours)
    }

    @Test
    fun `resolve - CUSTOM small window (6 to 8)`() {
        val r = ResolvedProtocol.resolve(Protocol.CUSTOM, 6, 8)
        // eatH = (8 - 6 + 24) % 24 = 2
        assertEquals(2, r.eatHours)
        assertEquals(22, r.fastHours)
        assertEquals(6, r.eatStart)
        assertEquals(8, r.eatEnd)
    }

    // ── Invariant: fastHours + eatHours == 24 ──

    @Test
    fun `resolve - fastHours plus eatHours always equals 24`() {
        val protocols = listOf(Protocol.P16_8, Protocol.P18_6, Protocol.P20_4, Protocol.P23_1)
        val customPairs = listOf(12 to 18, 20 to 4, 0 to 0, 6 to 8, 12 to 12)

        for (p in protocols) {
            val r = ResolvedProtocol.resolve(p, 0, 0)
            assertEquals(24, r.fastHours + r.eatHours)
        }
        for ((start, end) in customPairs) {
            val r = ResolvedProtocol.resolve(Protocol.CUSTOM, start, end)
            assertEquals(24, r.fastHours + r.eatHours)
        }
    }
}