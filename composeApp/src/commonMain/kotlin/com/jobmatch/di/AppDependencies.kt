package com.jobmatch.di

import com.jobmatch.data.api.JobMatchApiService
import com.jobmatch.data.api.createHttpClient
import com.jobmatch.data.local.SessionManager
import com.jobmatch.data.local.createDataStore
import com.jobmatch.data.repository.*
import com.jobmatch.domain.repository.*
import com.jobmatch.domain.usecase.*
import kotlinx.coroutines.runBlocking

/**
 * Manual dependency container — no reflection, no annotation processing.
 *
 * All dependencies are wired lazily so the object graph is built exactly once
 * on first access and never more.
 *
 * ─── How to swap the backend ───────────────────────────────────────────────
 * Change [BASE_URL] for a physical device or production server.
 * Android emulator: "http://10.0.2.2:8080/"
 * Local device on same Wi-Fi: "http://<your-machine-ip>:8080/"
 * Production: "https://api.yourapp.com/"
 */
object AppDependencies {

//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "\"http://192.168.0.101:8080/\""

    // ── Session ───────────────────────────────────────────────────────────────

    val sessionManager: SessionManager by lazy {
        SessionManager(createDataStore())
    }

    // ── Ktor client ───────────────────────────────────────────────────────────

    private val httpClient by lazy {
        createHttpClient(
            baseUrl       = BASE_URL,
            tokenProvider = {
                // This lambda runs on an IO thread from Ktor's scheduler.
                // runBlocking here does NOT block the main thread.
                runBlocking { sessionManager.getToken() }
            },
        )
    }

    // ── API service ───────────────────────────────────────────────────────────

    private val apiService: JobMatchApiService by lazy {
        JobMatchApiService(httpClient)
    }

    // ── Repositories ──────────────────────────────────────────────────────────

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(apiService)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(apiService)
    }

    val jobRepository: JobRepository by lazy {
        JobRepositoryImpl(apiService)
    }

    val applicationRepository: ApplicationRepository by lazy {
        ApplicationRepositoryImpl(apiService)
    }

    // ── Use cases ─────────────────────────────────────────────────────────────

    val sendOtpUseCase           by lazy { SendOtpUseCase(authRepository) }
    val verifyOtpUseCase         by lazy { VerifyOtpUseCase(authRepository) }
    val updateProfileUseCase     by lazy { UpdateProfileUseCase(userRepository) }
    val getProfileUseCase        by lazy { GetProfileUseCase(userRepository) }
    val listJobsUseCase          by lazy { ListJobsUseCase(jobRepository) }
    val getJobDetailUseCase      by lazy { GetJobDetailUseCase(jobRepository) }
    val applyToJobUseCase        by lazy { ApplyToJobUseCase(applicationRepository) }
    val getMyApplicationsUseCase by lazy { GetMyApplicationsUseCase(applicationRepository) }
}