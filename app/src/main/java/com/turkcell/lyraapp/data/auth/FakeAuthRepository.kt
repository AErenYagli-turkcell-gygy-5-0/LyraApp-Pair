package com.turkcell.lyraapp.data.auth

import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class FakeAuthRepository @Inject constructor() : AuthRepository {

    private val registeredPhones = mutableSetOf<String>()

    override suspend fun requestOtp(phoneNumber: String): Result<OtpRequestResult> {
        delay(NETWORK_DELAY_MS)
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Telefon numarasi bos olamaz."))
        }
        val firstTime = phoneNumber !in registeredPhones
        return Result.success(OtpRequestResult(sent = true, firstTime = firstTime))
    }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Result<OtpVerifyResult> {
        delay(NETWORK_DELAY_MS)
        if (code !in VALID_CODES) {
            return Result.failure(IllegalStateException("Girdigin kod hatali. Tekrar dene."))
        }
        val firstTime = phoneNumber !in registeredPhones
        registeredPhones.add(phoneNumber)
        return Result.success(
            OtpVerifyResult(
                accessToken = "fake_access_${UUID.randomUUID()}",
                refreshToken = "fake_refresh_${UUID.randomUUID()}",
                firstTime = firstTime,
                expiresIn = 900,
            ),
        )
    }

    override suspend fun completeProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        if (firstName.isBlank() || lastName.isBlank() || birthDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Tum alanlar doldurulmalidir."))
        }
        return Result.success(Unit)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 1_000L
        val VALID_CODES = setOf("280600", "260702", "250506", "101000", "346134", "123456")
    }
}
