package com.jobmatch.presentation.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jobmatch.di.AppDependencies
import com.jobmatch.domain.model.Job
import com.jobmatch.presentation.components.*
import com.jobmatch.util.onError
import com.jobmatch.util.onSuccess
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

data class JobFeedUiState(
    val jobs: List<Job>       = emptyList(),
    val isLoading: Boolean    = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String   = "",
    val hasMore: Boolean      = true,
    val bannerMessage: String? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class JobFeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JobFeedUiState())
    val uiState: StateFlow<JobFeedUiState> = _uiState.asStateFlow()

    private var currentPage   = 0
    private var pollingJob: CoroutineJob? = null
    private var searchJob: CoroutineJob?  = null

    init {
        loadJobs(reset = true)
        startPolling()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun loadNextPage() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        loadJobs()
    }

    fun refresh() = loadJobs(reset = true, refresh = true)

    fun retry() = loadJobs(reset = true)

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)          // debounce
            loadJobs(reset = true)
        }
    }

    fun dismissBanner() = _uiState.update { it.copy(bannerMessage = null) }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun loadJobs(reset: Boolean = false, refresh: Boolean = false) {
        if (reset) currentPage = 0

        viewModelScope.launch {
            _uiState.update { state ->
                when {
                    refresh -> state.copy(isRefreshing  = true,  errorMessage = null)
                    reset   -> state.copy(isLoading     = true,  errorMessage = null)
                    else    -> state.copy(isLoadingMore = true)
                }
            }

            AppDependencies.listJobsUseCase(
                page  = currentPage,
                query = _uiState.value.searchQuery.ifBlank { null },
            ).onSuccess { result ->
                val updatedJobs = if (reset) result.items
                else _uiState.value.jobs + result.items

                _uiState.update {
                    it.copy(
                        jobs          = updatedJobs,
                        isLoading     = false,
                        isRefreshing  = false,
                        isLoadingMore = false,
                        errorMessage  = null,
                        hasMore       = result.hasNext,
                    )
                }
                currentPage++
            }.onError { message, _ ->
                _uiState.update {
                    it.copy(
                        isLoading     = false,
                        isRefreshing  = false,
                        isLoadingMore = false,
                        errorMessage  = message,
                    )
                }
            }
        }
    }

    /**
     * Polls the first page every 30 seconds and prepends any new jobs
     * that have appeared since the last known top item.
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                AppDependencies.listJobsUseCase(page = 0)
                    .onSuccess { result ->
                        val latestJob  = result.items.firstOrNull() ?: return@onSuccess
                        val currentTop = _uiState.value.jobs.firstOrNull()
                        if (latestJob.id != currentTop?.id) {
                            _uiState.update {
                                it.copy(
                                    jobs          = listOf(latestJob) + it.jobs,
                                    bannerMessage = "New job: ${latestJob.title} at ${latestJob.company}",
                                )
                            }
                        }
                    }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        searchJob?.cancel()
        super.onCleared()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun JobFeedScreen(
    onJobClick: (Long) -> Unit,
    vm: JobFeedViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Banner snackbar for new jobs / real-time events
    val bannerMessage = state.bannerMessage
    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            snackbarHostState.showSnackbar(
                message  = bannerMessage,
                duration = SnackbarDuration.Short,
            )
            vm.dismissBanner()
        }
    }

    Scaffold(
        topBar       = {
            SearchBar(
                query         = state.searchQuery,
                onQueryChange = vm::onSearchQueryChanged,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading -> {
                    LoadingView(modifier = Modifier.fillMaxSize())
                }

                state.errorMessage != null -> {
                    ErrorView(
                        message = state.errorMessage!!,
                        onRetry = vm::retry,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                state.jobs.isEmpty() -> {
                    EmptyView(
                        message  = "No jobs found. Try a different search.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(
                            items = state.jobs,
                            key   = { _, item -> item.id },
                        ) { index, job ->

                            JobCard(job = job, onClick = { onJobClick(job.id) })

                            // Trigger next page load when approaching the end
                            if (index >= state.jobs.lastIndex - 2 && !state.isLoadingMore) {
                                LaunchedEffect(index) { vm.loadNextPage() }
                            }
                        }

                        if (state.isLoadingMore) {
                            item {
                                LoadingView(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = Modifier.fillMaxWidth().padding(16.dp),
        placeholder   = { Text("Search jobs, companies, skills…") },
        leadingIcon   = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine    = true,
    )
}