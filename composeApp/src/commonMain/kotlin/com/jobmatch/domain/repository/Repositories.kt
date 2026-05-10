package com.jobmatch.domain.repository

import com.jobmatch.domain.model.*
import com.jobmatch.util.Result

/**
 * Repository interfaces define the contract that the domain layer requires.
 * Implementations live in the data layer and know about Ktor, DataStore, etc.
 */

interface AuthRepository {
    suspend fun sendOtp(phone: String): Result<Unit>
    /** Returns the JWT token paired with the authenticated user. */
    suspend fun verifyOtp(phone: String, otp: String): Result<Pair<String, User>>
}

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(
        name: String,
        skills: List<String>,
        location: String,
    ): Result<User>
}

interface JobRepository {
    suspend fun listJobs(
        page: Int,
        size: Int      = 10,
        query: String? = null,
    ): Result<PageResult<Job>>

    suspend fun getJobDetail(jobId: Long): Result<Job>
}

interface ApplicationRepository {
    /** Returns (applicationId, statusString, userFacingMessage). */
    suspend fun applyToJob(jobId: Long): Result<Triple<Long, String, String>>
    suspend fun getMyApplications(
        page: Int,
        size: Int = 10,
    ): Result<PageResult<Application>>
}