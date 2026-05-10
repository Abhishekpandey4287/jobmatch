package com.jobmatch.data.api

import com.jobmatch.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Thin API layer — each function maps exactly one backend endpoint.
 * All functions throw on error; [safeCall] in the repository catches them.
 */
class JobMatchApiService(private val client: HttpClient) {

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun sendOtp(phone: String) {
        client.post("api/v1/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody(SendOtpRequest(phone))
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): AuthResponseDto =
        client.post("api/v1/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(VerifyOtpRequest(phone, otp))
        }.body()

    // ── User ──────────────────────────────────────────────────────────────────

    suspend fun getProfile(): UserDto =
        client.get("api/v1/users/me").body()

    suspend fun updateProfile(request: UpdateProfileRequest): UserDto =
        client.put("api/v1/users/me/profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Jobs ──────────────────────────────────────────────────────────────────

    suspend fun listJobs(
        page: Int,
        size: Int    = 10,
        query: String? = null,
    ): PagedResponseDto<JobDto> = client.get("api/v1/jobs") {
        parameter("page", page)
        parameter("size", size)
        query?.let { parameter("q", it) }
    }.body()

    suspend fun getJobDetail(jobId: Long): JobDto =
        client.get("api/v1/jobs/$jobId").body()

    // ── Applications ──────────────────────────────────────────────────────────

    suspend fun applyToJob(jobId: Long): ApplyResponseDto =
        client.post("api/v1/applications/jobs/$jobId/apply").body()

    suspend fun getMyApplications(
        page: Int,
        size: Int = 10,
    ): PagedResponseDto<ApplicationDto> = client.get("api/v1/applications/my") {
        parameter("page", page)
        parameter("size", size)
    }.body()
}