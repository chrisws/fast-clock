package com.fastclock.model

/**
 * Preset fasting protocols matching the HTML prototype.
 */
enum class Protocol(val label: String, val fastHours: Int, val eatHours: Int,
                    val eatStart: Int, val eatEnd: Int, val description: String) {
    P16_8("16:8", 16, 8, 12, 20, "Beginner-friendly. 16 h fast, 8 h eat."),
    P18_6("18:6", 18, 6, 12, 18, "Intermediate. 18 h fast, 6 h eat."),
    P20_4("20:4", 20, 4, 12, 16, "Advanced. 20 h fast, 4 h eat."),
    P23_1("23:1", 23, 1, 12, 13, "Expert. 23 h fast, 1 h eat."),
    CUSTOM("custom", 0, 0, 12, 18, "Set your own hours below.");

    companion object {
        fun fromLabel(label: String): Protocol {
            return entries.find { it.label == label } ?: P18_6
        }
    }
}

/**
 * Resolved protocol values (handles custom start/end).
 */
data class ResolvedProtocol(
    val fastHours: Int,
    val eatHours: Int,
    val eatStart: Int,
    val eatEnd: Int
) {
    companion object {
        fun resolve(protocol: Protocol, customEatStart: Int, customEatEnd: Int): ResolvedProtocol {
            if (protocol == Protocol.CUSTOM) {
                var eatH = (customEatEnd - customEatStart + 24) % 24
                if (eatH == 0) eatH = 24
                return ResolvedProtocol(24 - eatH, eatH, customEatStart, customEatEnd)
            }
            return ResolvedProtocol(protocol.fastHours, protocol.eatHours, protocol.eatStart, protocol.eatEnd)
        }
    }
}