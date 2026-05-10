package com.jobmatch.data.repository

import com.jobmatch.data.api.JobMatchApiService
import com.jobmatch.data.model.*
import com.jobmatch.domain.model.*
import com.jobmatch.domain.repository.*
import com.jobmatch.util.Result
import com.jobmatch.util.safeCall

class AuthRepositoryImpl(
    private val api: JobMatchApiService,
) : AuthRepository {

    override suspend fun sendOtp(phone: String): Result<Unit> =
        safeCall { api.sendOtp(phone) }

    override suspend fun verifyOtp(
        phone: String,
        otp: String,
    ): Result<Pair<String, User>> = safeCall {
        val response = api.verifyOtp(phone, otp)
        response.token to response.user.toDomain()
    }
}

class UserRepositoryImpl(
    private val api: JobMatchApiService,
) : UserRepository {

    override suspend fun getProfile(): Result<User> =
        safeCall { api.getProfile().toDomain() }

    override suspend fun updateProfile(
        name: String,
        skills: List<String>,
        location: String,
    ): Result<User> = safeCall {
        api.updateProfile(UpdateProfileRequest(name, skills, location)).toDomain()
    }
}

class JobRepositoryImpl(
    private val api: JobMatchApiService,
) : JobRepository {

    override suspend fun listJobs(
        page: Int,
        size: Int,
        query: String?,
    ): Result<PageResult<Job>> = safeCall {
        api.listJobs(page, size, query).toDomain { it.toDomain() }
    }

    override suspend fun getJobDetail(jobId: Long): Result<Job> =
        safeCall { api.getJobDetail(jobId).toDomain() }
}

class ApplicationRepositoryImpl(
    private val api: JobMatchApiService,
) : ApplicationRepository {

    override suspend fun applyToJob(jobId: Long): Result<Triple<Long, String, String>> =
        safeCall {
            val r = api.applyToJob(jobId)
            Triple(r.applicationId, r.status, r.message)
        }

    override suspend fun getMyApplications(
        page: Int,
        size: Int,
    ): Result<PageResult<Application>> = safeCall {
        api.getMyApplications(page, size).toDomain { it.toDomain() }
    }
}