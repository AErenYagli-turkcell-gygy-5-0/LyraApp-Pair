package com.turkcell.lyraapp.data.membership

data class MembershipPlan(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val priceKurus: Int,
    val priceLira: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean,
)

data class Membership(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val startedAt: String,
    val expiresAt: String,
)

data class CheckoutCard(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String?,
)

data class CheckoutResult(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String,
    val membership: Membership,
)

interface MembershipRepository {

    suspend fun getPlans(): Result<List<MembershipPlan>>

    suspend fun checkout(planType: String, card: CheckoutCard): Result<CheckoutResult>
}
