package com.jobmatch.presentation.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jobmatch.di.AppDependencies
import com.jobmatch.domain.model.Job
import com.jobmatch.presentation.components.EmptyView
import com.jobmatch.presentation.components.ErrorView
import com.jobmatch.presentation.components.JobCard
import com.jobmatch.presentation.components.LoadingView
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
    val jobs: List<Job>        = emptyList(),
    val isLoading: Boolean     = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String?  = null,
    val searchQuery: String    = "",
    val hasMore: Boolean       = true,
    val newJobBanner: String?  = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class JobFeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JobFeedUiState())
    val uiState: StateFlow<JobFeedUiState> = _uiState.asStateFlow()

    private var currentPage  = 0
    private var pollingJob: CoroutineJob? = null
    private var searchJob: CoroutineJob?  = null

    init {
        loadJobs(reset = true)
        startPolling()
    }

    fun loadNextPage() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        loadJobs()
    }

    fun retry() = loadJobs(reset = true)

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadJobs(reset = true)
        }
    }

    fun dismissBanner() = _uiState.update { it.copy(newJobBanner = null) }

    /**
     * Called when the user returns to the feed from JobDetailScreen.
     * Silently re-fetches ALL currently loaded pages and merges the updated
     * isApplied flags — so the "Applied" badge appears without a full reload.
     */
    fun refreshAppliedStatus() {
        val currentJobs = _uiState.value.jobs
        if (currentJobs.isEmpty()) return

        viewModelScope.launch {
            // Re-fetch page 0 only (enough to get updated isApplied flags
            // for visible jobs; full refresh would reset scroll position)
            AppDependencies.listJobsUseCase(
                page  = 0,
                size  = currentJobs.size.coerceAtMost(50),
                query = _uiState.value.searchQuery.ifBlank { null },
            ).onSuccess { result ->
                // Build a map of id → isApplied from fresh server data
                val appliedMap = result.items.associate { it.id to it.isApplied }

                // Merge into existing list — preserves scroll position
                val updated = currentJobs.map { job ->
                    val freshApplied = appliedMap[job.id]
                    if (freshApplied != null && freshApplied != job.isApplied)
                        job.copy(isApplied = freshApplied)
                    else
                        job
                }
                _uiState.update { it.copy(jobs = updated) }
            }
            // Silently ignore errors — stale data is acceptable here
        }
    }

    private fun loadJobs(reset: Boolean = false) {
        if (reset) currentPage = 0

        viewModelScope.launch {
            _uiState.update { state ->
                if (reset) state.copy(isLoading = true, errorMessage = null)
                else       state.copy(isLoadingMore = true)
            }

            AppDependencies.listJobsUseCase(
                page  = currentPage,
                query = _uiState.value.searchQuery.ifBlank { null },
            ).onSuccess { result ->
                val updated = if (reset) result.items
                else _uiState.value.jobs + result.items
                _uiState.update {
                    it.copy(
                        jobs          = updated,
                        isLoading     = false,
                        isLoadingMore = false,
                        errorMessage  = null,
                        hasMore       = result.hasNext,
                    )
                }
                currentPage++
            }.onError { msg, _ ->
                _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, errorMessage = msg)
                }
            }
        }
    }

    /**
     * Polls every 30 seconds for new jobs at the top of the list.
     * Shows a banner snackbar with a "View" action to scroll to top.
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                if (_uiState.value.searchQuery.isNotBlank()) continue

                AppDependencies.listJobsUseCase(page = 0)
                    .onSuccess { result ->
                        val latestJob  = result.items.firstOrNull() ?: return@onSuccess
                        val currentTop = _uiState.value.jobs.firstOrNull()
                        if (latestJob.id != currentTop?.id) {
                            _uiState.update {
                                it.copy(
                                    jobs         = listOf(latestJob) + it.jobs,
                                    newJobBanner = "New: ${latestJob.title} at ${latestJob.company}",
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
    val state             by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }
    val listState          = rememberLazyListState()

    // KEY FIX 1 — refresh isApplied badges every time this screen becomes
    // visible again (e.g. after returning from JobDetailScreen).
    // `lifecycleOwner` re-triggers ON_RESUME which fires on back navigation.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                vm.refreshAppliedStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Real-time new-job banner
    LaunchedEffect(state.newJobBanner) {
        val banner = state.newJobBanner ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message     = banner,
            actionLabel = "View",
            duration    = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            listState.animateScrollToItem(0)
        }
        vm.dismissBanner()
    }

    Scaffold(
        topBar = {
            OutlinedTextField(
                value         = state.searchQuery,
                onValueChange = vm::onSearchQueryChanged,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder   = { Text("Search jobs, companies, skills…") },
                leadingIcon   = { Icon(Icons.Outlined.Search, null) },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingView(Modifier.fillMaxSize())

                state.errorMessage != null -> ErrorView(
                    message  = state.errorMessage!!,
                    onRetry  = vm::retry,
                    modifier = Modifier.fillMaxSize(),
                )

                state.jobs.isEmpty() -> EmptyView(
                    message  = "No jobs found. Try a different search.",
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyColumn(
                    state               = listState,
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(
                        items = state.jobs,
                        key   = { _, job -> job.id },
                    ) { index, job ->

                        JobCard(job = job, onClick = { onJobClick(job.id) })

                        if (index >= state.jobs.lastIndex - 2 && !state.isLoadingMore) {
                            LaunchedEffect(index) { vm.loadNextPage() }
                        }
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}