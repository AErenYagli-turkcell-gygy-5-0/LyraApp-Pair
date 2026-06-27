package com.turkcell.lyraapp.data.membership

import com.squareup.moshi.Moshi
import com.turkcell.lyraapp.data.remote.MembershipApiService
import com.turkcell.lyraapp.data.remote.dto.ApiErrorResponseDto
import com.turkcell.lyraapp.data.remote.dto.CheckoutCardDto
import com.turkcell.lyraapp.data.remote.dto.CheckoutRequestDto
import retrofit2.Response
import javax.inject.Inject

class RealMembershipRepository @Inject constructor(
    private val membershipApiService: MembershipApiService,
    private val moshi: Moshi,
) : MembershipRepository {

    override suspend fun getPlans(): Result<List<MembershipPlan>> = runCatching {
        val response = membershipApiService.getPlans()
        response.data.map { dto ->
            MembershipPlan(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                description = dto.description,
                priceKurus = dto.priceKurus,
                priceLira = dto.price,
                currency = dto.currency,
                durationDays = dto.durationDays,
                autoRenew = dto.autoRenew,
            )
        }
    }

    override suspend fun checkout(planType: String, card: CheckoutCard): Result<CheckoutResult> =
        runCatching {
            val response = membershipApiService.checkout(
                CheckoutRequestDto(
                    plan = planType,
                    card = CheckoutCardDto(
                        number = card.number,
                        expMonth = card.expMonth,
                        expYear = card.expYear,
                        cvc = card.cvc,
                        holderName = card.holderName,
                    ),
                ),
            )
            if (response.isSuccessful) {
                val data = response.body()!!.data
                val m = data.membership
                CheckoutResult(
                    transactionId = data.payment.transactionId,
                    amountKurus = data.payment.amountKurus,
                    currency = data.payment.currency,
                    membership = Membership(
                        planId = m.planId,
                        type = m.type,
                        status = m.status,
                        autoRenew = m.autoRenew,
                        startedAt = m.startedAt,
                        expiresAt = m.expiresAt,
                    ),
                )
            } else {
                throw apiException(response)
            }
        }

    private fun apiException(response: Response<*>): Exception {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val adapter = moshi.adapter(ApiErrorResponseDto::class.java)
                val parsed = adapter.fromJson(errorBody)
                if (parsed != null) {
                    return Exception(parsed.error.message)
                }
            } catch (_: Exception) { }
        }
        return when (response.code()) {
            400 -> Exception("Gecersiz plan veya kart bilgileri.")
            401 -> Exception("Oturum suresi dolmus. Lutfen tekrar giris yapin.")
            402 -> Exception("Odeme reddedildi. Lutfen kart bilgilerinizi kontrol edin.")
            else -> Exception("Beklenmeyen bir hata olustu (${response.code()}).")
        }
    }
}
