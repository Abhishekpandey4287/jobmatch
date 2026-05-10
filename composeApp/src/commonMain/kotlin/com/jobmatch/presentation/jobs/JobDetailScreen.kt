package com.jobmatch.presentation.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jobmatch.di.AppDependencies
import com.jobmatch.domain.model.Job
import com.jobmatch.presentation.components.ErrorView
import com.jobmatch.presentation.components.LoadingView
import com.jobmatch.presentation.components.SkillChip
import com.jobmatch.util.onError
import com.jobmatch.util.onSuccess
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

data class JobDetailUiState(
    val job: Job?             = null,
    val isLoading: Boolean    = false,
    val isApplying: Boolean   = false,
    val errorMessage: String? = null,
    val applySuccess: String? = null,
    val applyError: String?   = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class JobDetailViewModel(private val jobId: Long) : ViewModel() {

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    init { loadJob() }

    fun retry() = loadJob()

    private fun loadJob() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            AppDependencies.getJobDetailUseCase(jobId)
                .onSuccess { job ->
                    _uiState.update { it.copy(isLoading = false, job = job) }
                }
                .onError { msg, _ ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    fun apply() {
        val job = _uiState.value.job ?: return
        if (job.isApplied || _uiState.value.isApplying) return

        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true, applyError = null, applySuccess = null) }
            AppDependencies.applyToJobUseCase(jobId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isApplying   = false,
                            applySuccess = "Application submitted successfully! 🎉",
                            job          = it.job?.copy(isApplied = true),
                        )
                    }
                }
                .onError { msg, _ ->
                    _uiState.update { it.copy(isApplying = false, applyError = msg) }
                }
        }
    }

    fun clearFeedback() = _uiState.update { it.copy(applySuccess = null, applyError = null) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobDetailScreen(
    jobId: Long,
    onBack: () -> Unit,
    vm: JobDetailViewModel = viewModel { JobDetailViewModel(jobId) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.applySuccess, state.applyError) {
        val message = state.applySuccess ?: state.applyError
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            vm.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.job?.company ?: "Job details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            state.job?.let { job ->
                Button(
                    onClick  = vm::apply,
                    enabled  = !job.isApplied && !state.isApplying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                ) {
                    if (state.isApplying) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (job.isApplied) "Already Applied" else "Apply Now")
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.fillMaxSize())

            state.errorMessage != null -> ErrorView(
                message  = state.errorMessage!!,
                onRetry  = vm::retry,
                modifier = Modifier.fillMaxSize(),
            )

            state.job != null -> JobDetailContent(
                job      = state.job!!,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobDetailContent(job: Job, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(job.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(job.company, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkillChip(job.location)
            SkillChip(job.jobType.name.replace("_", " "))
            SkillChip(job.status.name)
        }

        job.salaryDisplay?.let { salary ->
            Spacer(Modifier.height(12.dp))
            Text(
                text       = salary,
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("About this role", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(job.description, style = MaterialTheme.typography.bodyLarge)

        if (job.requiredSkills.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Required Skills", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
            ) {
                job.requiredSkills.forEach { SkillChip(it) }
            }
        }

        // Extra bottom padding so content clears the Apply button
        Spacer(Modifier.height(100.dp))
    }
}