package com.fastclock.model

/**
 * A single completed fast session.
 */
data class FastSession(
    val startEpoch: Long,
    val endEpoch: Long,
    val durationHours: Double,
    val onTime: Boolean
) {
    val durationMinutes: Int get() = (durationHours * 60).toInt()
}

/**
 * A weigh-in entry.
 */
data class WeighIn(
    val dateEpoch: Long,
    val weightKg: Double
)

/**
 * Core app state, persisted via SharedPreferences.
 */
data class FastState(
    val protocol: Protocol = Protocol.P18_6,
    val customEatStart: Int = 12,
    val customEatEnd: Int = 18,
    val clockMode: Boolean = false,
    val fastStartEpoch: Long? = null,
    val dailyScore: Int = 100,
    val scoreDate: String = "",
    val fastHistory: List<FastSession> = emptyList(),
    val weightLog: List<WeighIn> = emptyList()
) {
    fun resolvedProtocol(): ResolvedProtocol =
        ResolvedProtocol.resolve(protocol, customEatStart, customEatEnd)
}