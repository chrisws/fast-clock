package com.fastclock.model

/**
 * Serialization helpers for persisting [FastState] fields to/from strings.
 */
object StateSerializer {

    fun serializeFastHistory(list: List<FastSession>): String {
        return list.joinToString(";") { s ->
            "${s.epoch},${s.onTime}"
        }
    }

    fun parseFastHistory(str: String): List<FastSession> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size != 2) return@mapNotNull null
            try {
                FastSession(
                    epoch = parts[0].toLong(),
                    onTime = parts[1].toBoolean()
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    fun serializeWeightLog(list: List<WeighIn>): String {
        return list.joinToString(";") { w ->
            "${w.dateEpoch},${w.weightKg}"
        }
    }

    fun parseWeightLog(str: String): List<WeighIn> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size != 2) return@mapNotNull null
            try {
                WeighIn(
                    dateEpoch = parts[0].toLong(),
                    weightKg = parts[1].toDouble()
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}