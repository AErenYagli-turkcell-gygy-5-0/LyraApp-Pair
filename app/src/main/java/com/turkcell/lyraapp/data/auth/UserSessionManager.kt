package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.membership.Membership
import com.turkcell.lyraapp.data.remote.dto.MembershipDto
import com.turkcell.lyraapp.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class User(
    val id: String,
    val phone: String,
    val displayName: String?,
    val firstName: String?,
    val lastName: String?,
    val membership: Membership?,
)

@Singleton
class UserSessionManager @Inject constructor() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    val isPremium = _user.map { it?.membership?.status == "active" }

    fun setUser(userDto: UserDto) {
        _user.value = userDto.toDomain()
    }

    fun updateMembership(membership: Membership) {
        val current = _user.value ?: return
        _user.value = current.copy(membership = membership)
    }

    fun clear() {
        _user.value = null
    }

    companion object {
        fun UserDto.toDomain() = User(
            id = id,
            phone = phone,
            displayName = displayName,
            firstName = firstName,
            lastName = lastName,
            membership = membership?.toDomain(),
        )

        fun MembershipDto.toDomain() = Membership(
            planId = planId,
            type = type,
            status = status,
            autoRenew = autoRenew,
            startedAt = startedAt,
            expiresAt = expiresAt,
        )

        fun calculateDaysLeft(expiresAt: String): Int {
            return try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val dateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val expiryDate = try {
                    val cleaned = expiresAt.replace("Z", "").substringBefore(".")
                    isoFormat.parse(cleaned)
                } catch (_: Exception) {
                    dateOnly.parse(expiresAt)
                }
                if (expiryDate == null) return 0
                val diffMs = expiryDate.time - System.currentTimeMillis()
                TimeUnit.MILLISECONDS.toDays(diffMs).coerceAtLeast(0).toInt()
            } catch (_: Exception) {
                0
            }
        }
    }
}
