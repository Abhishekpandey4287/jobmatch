package com.jobmatch.domain.model

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Domain models — pure Kotlin, no platform or framework dependencies.
// These are the canonical types used across all features.
// ─────────────────────────────────────────────────────────────────────────────

enum class JobType   { FULL_TIME, PART_TIME, CONTRACT, REMOTE, HYBRID }
enum class JobStatus { ACTIVE, CLOSED, DRAFT }

enum class ApplicationStatus(val displayName: String) {
    APPLIED("Applied"),
    VIEWED("Viewed by recruiter"),
    SHORTLISTED("Shortlisted"),
    REJECTED("Not selected"),
    HIRED("Hired!"),
}

data class Job(
    val id: Long,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val requiredSkills: List<String>,
    val salaryMin: Int?,
    val salaryMax: Int?,
    val jobType: JobType,
    val status: JobStatus,
    val postedAt: String,
    val isApplied: Boolean,
) {
    /** Human-readable salary range, e.g. "₹16L – ₹28L" */
    val salaryDisplay: String? get() = when {
        salaryMin == null && salaryMax == null -> null
        salaryMin == null -> "Up to ₹${salaryMax!! / 100_000}L"
        salaryMax == null -> "From ₹${salaryMin / 100_000}L"
        else              -> "₹${salaryMin / 100_000}L – ₹${salaryMax / 100_000}L"
    }
}

data class User(
    val id: Long,
    val phone: String,
    val name: String?,
    val skills: List<String>,
    val location: String?,
    val audioUrl: String?,
    val profileComplete: Boolean,
)

data class Application(
    val id: Long,
    val jobId: Long,
    val jobTitle: String,
    val company: String,
    val status: ApplicationStatus,
    val statusDisplayName: String,
    val appliedAt: String,
    val updatedAt: String,
)

/** Generic paginated result — used for both Job and Application lists. */
data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

/**
 * Real-time event received over STOMP WebSocket or produced by the polling layer.
 * A single type covers all server-push scenarios.
 */
@Serializable
data class RealtimeEvent(
    val type: String,
    val jobId: Long?               = null,
    val applicationId: Long?       = null,
    val newStatus: String?         = null,
    val statusDisplayName: String? = null,
    val message: String            = "",
    val jobTitle: String?          = null,
    val company: String?           = null,
)