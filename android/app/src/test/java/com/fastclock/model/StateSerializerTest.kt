package com.fastclock.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StateSerializerTest {

    // ── FastHistory ──

    @Test
    fun `serializeFastHistory - empty list`() {
        assertEquals("", StateSerializer.serializeFastHistory(emptyList()))
    }

    @Test
    fun `serializeFastHistory - single entry`() {
        val list = listOf(FastSession(epoch = 1000L, onTime = true))
        assertEquals("1000,true", StateSerializer.serializeFastHistory(list))
    }

    @Test
    fun `serializeFastHistory - multiple entries`() {
        val list = listOf(
            FastSession(epoch = 1000L, onTime = true),
            FastSession(epoch = 2000L, onTime = false)
        )
        assertEquals("1000,true;2000,false", StateSerializer.serializeFastHistory(list))
    }

    @Test
    fun `parseFastHistory - empty string`() {
        assertTrue(StateSerializer.parseFastHistory("").isEmpty())
    }

    @Test
    fun `parseFastHistory - blank string`() {
        assertTrue(StateSerializer.parseFastHistory("   ").isEmpty())
    }

    @Test
    fun `parseFastHistory - single entry`() {
        val result = StateSerializer.parseFastHistory("1000,true")
        assertEquals(1, result.size)
        assertEquals(1000L, result[0].epoch)
        assertTrue(result[0].onTime)
    }

    @Test
    fun `parseFastHistory - multiple entries`() {
        val result = StateSerializer.parseFastHistory("1000,true;2000,false;3000,true")
        assertEquals(3, result.size)
        assertEquals(1000L, result[0].epoch)
        assertTrue(result[0].onTime)
        assertEquals(2000L, result[1].epoch)
        assertEquals(false, result[1].onTime)
        assertEquals(3000L, result[2].epoch)
        assertTrue(result[2].onTime)
    }

    @Test
    fun `parseFastHistory - malformed entry is skipped`() {
        val result = StateSerializer.parseFastHistory("1000,true;badentry;2000,false")
        assertEquals(2, result.size)
    }

    @Test
    fun `parseFastHistory - non-numeric epoch is skipped`() {
        val result = StateSerializer.parseFastHistory("abc,true")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fastHistory round-trip`() {
        val original = listOf(
            FastSession(epoch = 1718000000000L, onTime = true),
            FastSession(epoch = 1718100000000L, onTime = false),
            FastSession(epoch = 1718200000000L, onTime = true)
        )
        val serialized = StateSerializer.serializeFastHistory(original)
        val parsed = StateSerializer.parseFastHistory(serialized)
        assertEquals(original, parsed)
    }

    // ── WeightLog ──

    @Test
    fun `serializeWeightLog - empty list`() {
        assertEquals("", StateSerializer.serializeWeightLog(emptyList()))
    }

    @Test
    fun `serializeWeightLog - single entry`() {
        val list = listOf(WeighIn(dateEpoch = 1000L, weightKg = 75.5))
        assertEquals("1000,75.5", StateSerializer.serializeWeightLog(list))
    }

    @Test
    fun `serializeWeightLog - multiple entries`() {
        val list = listOf(
            WeighIn(dateEpoch = 1000L, weightKg = 75.5),
            WeighIn(dateEpoch = 2000L, weightKg = 74.2)
        )
        assertEquals("1000,75.5;2000,74.2", StateSerializer.serializeWeightLog(list))
    }

    @Test
    fun `parseWeightLog - empty string`() {
        assertTrue(StateSerializer.parseWeightLog("").isEmpty())
    }

    @Test
    fun `parseWeightLog - single entry`() {
        val result = StateSerializer.parseWeightLog("1000,75.5")
        assertEquals(1, result.size)
        assertEquals(1000L, result[0].dateEpoch)
        assertEquals(75.5, result[0].weightKg, 0.001)
    }

    @Test
    fun `parseWeightLog - multiple entries`() {
        val result = StateSerializer.parseWeightLog("1000,75.5;2000,74.2;3000,73.8")
        assertEquals(3, result.size)
        assertEquals(75.5, result[0].weightKg, 0.001)
        assertEquals(74.2, result[1].weightKg, 0.001)
        assertEquals(73.8, result[2].weightKg, 0.001)
    }

    @Test
    fun `parseWeightLog - malformed entry is skipped`() {
        val result = StateSerializer.parseWeightLog("1000,75.5;bad;2000,74.2")
        assertEquals(2, result.size)
    }

    @Test
    fun `weightLog round-trip`() {
        val original = listOf(
            WeighIn(dateEpoch = 1718000000000L, weightKg = 80.1),
            WeighIn(dateEpoch = 1718100000000L, weightKg = 79.4)
        )
        val serialized = StateSerializer.serializeWeightLog(original)
        val parsed = StateSerializer.parseWeightLog(serialized)
        assertEquals(original, parsed)
    }
}