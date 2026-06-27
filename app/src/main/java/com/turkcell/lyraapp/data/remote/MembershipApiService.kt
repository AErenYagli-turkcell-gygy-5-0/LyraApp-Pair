package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.CheckoutRequestDto
import com.turkcell.lyraapp.data.remote.dto.CheckoutResponseDto
import com.turkcell.lyraapp.data.remote.dto.MembershipPlanListResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MembershipApiService {

    @GET("api/v1/memberships/plans")
    suspend fun getPlans(): MembershipPlanListResponseDto

    @POST("api/v1/memberships/checkout")
    suspend fun checkout(@Body body: CheckoutRequestDto): Response<CheckoutResponseDto>
}
