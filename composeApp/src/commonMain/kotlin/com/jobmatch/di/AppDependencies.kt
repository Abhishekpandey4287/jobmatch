package com.jobmatch.di

import com.jobmatch.data.api.JobMatchApiService
import com.jobmatch.data.api.createHttpClient
import com.jobmatch.data.local.SessionManager
import com.jobmatch.data.local.createDataStore
import com.jobmatch.data.repository.*
import com.jobmatch.domain.repository.*
import com.jobmatch.domain.usecase.*
import kotlinx.coroutines.runBlocking

object AppDependencies {

    private const val BASE_URL = "http://192.168.0.101:8080/"
    //    private const val BASE_URL = "http://10.0.2.2:8080/"

    val sessionManager: SessionManager by lazy {
        SessionManager(createDataStore())
    }

    private var _httpClient: io.ktor.client.HttpClient? = null
    private var _apiService: JobMatchApiService? = null

    private val httpClient: io.ktor.client.HttpClient
        get() {
            if (_httpClient == null) {
                _httpClient = createHttpClient(
                    baseUrl = BASE_URL,
                    tokenProvider = { runBlocking { sessionManager.getToken() } },
                )
            }
            return _httpClient!!
        }

    private val apiService: JobMatchApiService
        get() {
            if (_apiService == null) {
                _apiService = JobMatchApiService(httpClient)
            }
            return _apiService!!
        }

    // Repositories — always derived from the current apiService instance
    val authRepository: AuthRepository
        get() = AuthRepositoryImpl(apiService)

    val userRepository: UserRepository
        get() = UserRepositoryImpl(apiService)

    val jobRepository: JobRepository
        get() = JobRepositoryImpl(apiService)

    val applicationRepository: ApplicationRepository
        get() = ApplicationRepositoryImpl(apiService)

    val sendOtpUseCase: SendOtpUseCase
        get() = SendOtpUseCase(authRepository)

    val verifyOtpUseCase: VerifyOtpUseCase
        get() = VerifyOtpUseCase(authRepository)

    val updateProfileUseCase: UpdateProfileUseCase
        get() = UpdateProfileUseCase(userRepository)

    val getProfileUseCase: GetProfileUseCase
        get() = GetProfileUseCase(userRepository)

    val listJobsUseCase: ListJobsUseCase
        get() = ListJobsUseCase(jobRepository)

    val getJobDetailUseCase: GetJobDetailUseCase
        get() = GetJobDetailUseCase(jobRepository)

    val applyToJobUseCase: ApplyToJobUseCase
        get() = ApplyToJobUseCase(applicationRepository)

    val getMyApplicationsUseCase: GetMyApplicationsUseCase
        get() = GetMyApplicationsUseCase(applicationRepository)


    fun reset() {
        _httpClient?.close()
        _httpClient = null
        _apiService = null
    }
}