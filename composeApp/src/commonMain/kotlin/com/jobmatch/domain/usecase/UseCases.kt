package com.jobmatch.domain.usecase

import com.jobmatch.domain.model.*
import com.jobmatch.domain.repository.*
import com.jobmatch.util.Result

// ─── Auth ─────────────────────────────────────────────────────────────────────

class SendOtpUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        if (phone.isBlank()) return Result.Error("Phone number is required")
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 7) return Result.Error("Enter a valid phone number")
        return repo.sendOtp(phone.trim())
    }
}

class VerifyOtpUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(phone: String, otp: String): Result<Pair<String, User>> {
        if (otp.isBlank())  return Result.Error("OTP is required")
        if (otp.length < 4) return Result.Error("OTP must be at least 4 digits")
        return repo.verifyOtp(phone.trim(), otp.trim())
    }
}

// ─── Profile ──────────────────────────────────────────────────────────────────

class UpdateProfileUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(
        name: String,
        skillsRaw: String,
        location: String,
    ): Result<User> {
        val skills = skillsRaw
            .split(",", "\n")
            .map(String::trim)
            .filter(String::isNotBlank)

        if (name.isBlank())   return Result.Error("Name is required")
        if (skills.isEmpty()) return Result.Error("At least one skill is required")
        if (location.isBlank()) return Result.Error("Location is required")

        return repo.updateProfile(name.trim(), skills, location.trim())
    }
}

class GetProfileUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(): Result<User> = repo.getProfile()
}

// ─── Jobs ─────────────────────────────────────────────────────────────────────

class ListJobsUseCase(private val repo: JobRepository) {
    suspend operator fun invoke(
        page: Int,
        size: Int      = 10,
        query: String? = null,
    ): Result<PageResult<Job>> = repo.listJobs(page, size, query)
}

class GetJobDetailUseCase(private val repo: JobRepository) {
    suspend operator fun invoke(jobId: Long): Result<Job> = repo.getJobDetail(jobId)
}

// ─── Applications ─────────────────────────────────────────────────────────────

class ApplyToJobUseCase(private val repo: ApplicationRepository) {
    suspend operator fun invoke(jobId: Long): Result<Triple<Long, String, String>> =
        repo.applyToJob(jobId)
}

class GetMyApplicationsUseCase(private val repo: ApplicationRepository) {
    suspend operator fun invoke(
        page: Int,
        size: Int = 10,
    ): Result<PageResult<Application>> = repo.getMyApplications(page, size)
}