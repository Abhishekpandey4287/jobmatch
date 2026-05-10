package com.jobmatch.presentation.applications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jobmatch.di.AppDependencies
import com.jobmatch.domain.model.Application
import com.jobmatch.domain.model.ApplicationStatus
import com.jobmatch.presentation.components.EmptyView
import com.jobmatch.presentation.components.ErrorView
import com.jobmatch.presentation.components.LoadingView
import com.jobmatch.util.onError
import com.jobmatch.util.onSuccess
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// State + ViewModel
// ─────────────────────────────────────────────────────────────────────────────

data class ApplicationsUiState(
    val applications: List<Application> = emptyList(),
    val isLoading: Boolean              = false,
    val isLoadingMore: Boolean          = false,
    val errorMessage: String?           = null,
    val hasMore: Boolean                = true,
)

class ApplicationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    private var currentPage = 0

    init { load(reset = true) }

    fun retry() = load(reset = true)

    fun loadNextPage() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        load()
    }

    private fun load(reset: Boolean = false) {
        if (reset) currentPage = 0

        viewModelScope.launch {
            _uiState.update {
                if (reset) it.copy(isLoading = true, errorMessage = null)
                else       it.copy(isLoadingMore = true)
            }

            AppDependencies.getMyApplicationsUseCase(currentPage)
                .onSuccess { result ->
                    val all = if (reset) result.items
                    else _uiState.value.applications + result.items
                    _uiState.update {
                        it.copy(
                            applications  = all,
                            isLoading     = false,
                            isLoadingMore = false,
                            errorMessage  = null,
                            hasMore       = result.hasNext,
                        )
                    }
                    currentPage++
                }
                .onError { msg, _ ->
                    _uiState.update {
                        it.copy(isLoading = false, isLoadingMore = false, errorMessage = msg)
                    }
                }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    vm: ApplicationsViewModel = viewModel { ApplicationsViewModel() },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Applications") })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingView(Modifier.fillMaxSize())

                state.errorMessage != null -> ErrorView(
                    message  = state.errorMessage!!,
                    onRetry  = vm::retry,
                    modifier = Modifier.fillMaxSize(),
                )

                state.applications.isEmpty() -> EmptyView(
                    message  = "No applications yet. Start applying to jobs!",
                    icon     = Icons.Outlined.Assignment,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(state.applications, key = { _, a -> a.id }) { index, app ->
                        ApplicationCard(app)

                        if (index >= state.applications.lastIndex - 2 && !state.isLoadingMore) {
                            LaunchedEffect(index) { vm.loadNextPage() }
                        }
                    }

                    if (state.isLoadingMore) {
                        item {
                            LoadingView(modifier = Modifier.fillMaxWidth().padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Application card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ApplicationCard(application: Application) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = application.jobTitle,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text  = application.company,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(application.status)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text  = application.statusDisplayName,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor(application.status),
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "Applied ${application.appliedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusBadge(status: ApplicationStatus) {
    val (bg, fg) = statusColors(status)
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text     = status.name.replace("_", " "),
            style    = MaterialTheme.typography.labelSmall,
            color    = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun statusColor(status: ApplicationStatus): Color = when (status) {
    ApplicationStatus.HIRED       -> MaterialTheme.colorScheme.tertiary
    ApplicationStatus.SHORTLISTED -> MaterialTheme.colorScheme.primary
    ApplicationStatus.REJECTED    -> MaterialTheme.colorScheme.error
    else                          -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun statusColors(status: ApplicationStatus): Pair<Color, Color> = when (status) {
    ApplicationStatus.HIRED       ->
        MaterialTheme.colorScheme.tertiaryContainer  to MaterialTheme.colorScheme.onTertiaryContainer
    ApplicationStatus.SHORTLISTED ->
        MaterialTheme.colorScheme.primaryContainer   to MaterialTheme.colorScheme.onPrimaryContainer
    ApplicationStatus.REJECTED    ->
        MaterialTheme.colorScheme.errorContainer     to MaterialTheme.colorScheme.onErrorContainer
    ApplicationStatus.VIEWED      ->
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    else                          ->
        MaterialTheme.colorScheme.surfaceVariant     to MaterialTheme.colorScheme.onSurfaceVariant
}