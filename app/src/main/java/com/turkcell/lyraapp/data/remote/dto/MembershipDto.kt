package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.JsonClass

// region — Membership Plans
@JsonClass(generateAdapter = false)
data class MembershipPlanDto(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val priceKurus: Int,
    val price: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean,
)

@JsonClass(generateAdapter = false)
data class MembershipPlanListResponseDto(
    val data: List<MembershipPlanDto>,
)
// endregion

// region — Membership (user's active membership)
@JsonClass(generateAdapter = false)
data class MembershipDto(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val startedAt: String,
    val expiresAt: String,
)
// endregion

// region — Checkout
@JsonClass(generateAdapter = false)
data class CheckoutCardDto(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String? = null,
)

@JsonClass(generateAdapter = false)
data class CheckoutRequestDto(
    val plan: String,
    val card: CheckoutCardDto,
)

@JsonClass(generateAdapter = false)
data class CheckoutPaymentDto(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String,
)

@JsonClass(generateAdapter = false)
data class CheckoutDataDto(
    val payment: CheckoutPaymentDto,
    val membership: MembershipDto,
)

@JsonClass(generateAdapter = false)
data class CheckoutResponseDto(
    val data: CheckoutDataDto,
)
// endregion
