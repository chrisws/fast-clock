package com.fastclock.model

/**
 * A single "I ate" event logged by the user.
 */
data class FastSession(
    val epoch: Long,
    val onTime: Boolean
)

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
    val dailyScore: Int = 100,
    val scoreDate: String = "",
    val fastHistory: List<FastSession> = emptyList(),
    val weightLog: List<WeighIn> = emptyList()
) {
    fun resolvedProtocol(): ResolvedProtocol =
        ResolvedProtocol.resolve(protocol, customEatStart, customEatEnd)
}