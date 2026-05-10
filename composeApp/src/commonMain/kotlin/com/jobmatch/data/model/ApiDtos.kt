package com.jobmatch.data.model

import com.jobmatch.domain.model.*
import kotlinx.serialization.Serializable

// ─── Request DTOs ─────────────────────────────────────────────────────────────

@Serializable data class SendOtpRequest(val phone: String)
@Serializable data class VerifyOtpRequest(val phone: String, val otp: String)
@Serializable data class UpdateProfileRequest(
    val name: String,
    val skills: List<String>,
    val location: String,
)

// ─── Response DTOs ────────────────────────────────────────────────────────────

@Serializable
data class UserDto(
    val id: Long,
    val phone: String,
    val name: String?          = null,
    val skills: List<String>   = emptyList(),
    val location: String?      = null,
    val audioUrl: String?      = null,
    val profileComplete: Boolean,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto,
    val isNewUser: Boolean,
)

@Serializable
data class JobDto(
    val id: Long,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val requiredSkills: List<String> = emptyList(),
    val salaryMin: Int?              = null,
    val salaryMax: Int?              = null,
    val jobType: String,
    val status: String,
    val postedAt: String,
    val isApplied: Boolean           = false,
)

@Serializable
data class PagedResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

@Serializable
data class ApplicationDto(
    val id: Long,
    val jobId: Long,
    val jobTitle: String,
    val company: String,
    val status: String,
    val statusDisplayName: String  = "",
    val appliedAt: String,
    val updatedAt: String,
)

@Serializable
data class ApplyResponseDto(
    val applicationId: Long,
    val status: String,
    val message: String,
)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
)

// ─── DTO → Domain mappers ─────────────────────────────────────────────────────

fun UserDto.toDomain() = User(
    id             = id,
    phone          = phone,
    name           = name,
    skills         = skills,
    location       = location,
    audioUrl       = audioUrl,
    profileComplete = profileComplete,
)

fun JobDto.toDomain() = Job(
    id             = id,
    title          = title,
    company        = company,
    location       = location,
    description    = description,
    requiredSkills = requiredSkills,
    salaryMin      = salaryMin,
    salaryMax      = salaryMax,
    jobType        = runCatching { JobType.valueOf(jobType) }.getOrDefault(JobType.FULL_TIME),
    status         = runCatching { JobStatus.valueOf(status) }.getOrDefault(JobStatus.ACTIVE),
    postedAt       = postedAt,
    isApplied      = isApplied,
)

fun ApplicationDto.toDomain() = Application(
    id                = id,
    jobId             = jobId,
    jobTitle          = jobTitle,
    company           = company,
    status            = runCatching { ApplicationStatus.valueOf(status) }
        .getOrDefault(ApplicationStatus.APPLIED),
    statusDisplayName = statusDisplayName.ifBlank { status },
    appliedAt         = appliedAt,
    updatedAt         = updatedAt,
)

fun <T, R> PagedResponseDto<T>.toDomain(mapper: (T) -> R) = PageResult(
    items      = content.map(mapper),
    page       = page,
    totalPages = totalPages,
    hasNext    = hasNext,
    hasPrevious = hasPrevious,
)