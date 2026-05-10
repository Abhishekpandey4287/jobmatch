package com.jobmatch.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jobmatch.di.AppDependencies
import com.jobmatch.domain.model.User
import com.jobmatch.presentation.components.SkillChip
import com.jobmatch.util.onError
import com.jobmatch.util.onSuccess
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

data class ProfileEditUiState(
    val user: User?              = null,
    val name: String             = "",
    val skillsRaw: String        = "",
    val location: String         = "",
    val isLoadingProfile: Boolean = true,
    val isSaving: Boolean        = false,
    val isEditing: Boolean       = false,
    val errorMessage: String?    = null,
    val successMessage: String?  = null,
    val showLogoutDialog: Boolean = false,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class ProfileEditViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true, errorMessage = null) }
            AppDependencies.getProfileUseCase()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            user             = user,
                            name             = user.name ?: "",
                            skillsRaw        = user.skills.joinToString(", "),
                            location         = user.location ?: "",
                            isLoadingProfile = false,
                        )
                    }
                }
                .onError { msg, _ ->
                    _uiState.update { it.copy(isLoadingProfile = false, errorMessage = msg) }
                }
        }
    }

    fun startEditing() =
        _uiState.update { it.copy(isEditing = true, errorMessage = null, successMessage = null) }

    fun cancelEditing() {
        val user = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditing  = false,
                name       = user?.name ?: "",
                skillsRaw  = user?.skills?.joinToString(", ") ?: "",
                location   = user?.location ?: "",
                errorMessage = null,
            )
        }
    }

    fun onNameChanged(v: String)     = _uiState.update { it.copy(name = v,      errorMessage = null) }
    fun onSkillsChanged(v: String)   = _uiState.update { it.copy(skillsRaw = v, errorMessage = null) }
    fun onLocationChanged(v: String) = _uiState.update { it.copy(location = v,  errorMessage = null) }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            AppDependencies.updateProfileUseCase(s.name, s.skillsRaw, s.location)
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            user           = updated,
                            name           = updated.name ?: "",
                            skillsRaw      = updated.skills.joinToString(", "),
                            location       = updated.location ?: "",
                            isSaving       = false,
                            isEditing      = false,
                            successMessage = "Profile updated!",
                        )
                    }
                }
                .onError { msg, _ ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = msg) }
                }
        }
    }

    fun showLogoutDialog()    = _uiState.update { it.copy(showLogoutDialog = true) }
    fun dismissLogoutDialog() = _uiState.update { it.copy(showLogoutDialog = false) }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            AppDependencies.sessionManager.clearSession()
            AppDependencies.reset()
            onDone()
        }
    }

    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen — Tab 3
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onLogout: () -> Unit,
    vm: ProfileEditViewModel = viewModel { ProfileEditViewModel() },
) {
    val state             by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); vm.clearSuccess() }
    }

    // Logout confirmation dialog
    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = vm::dismissLogoutDialog,
            title   = { Text("Log out") },
            text    = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = { vm.logout(onLogout) },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissLogoutDialog) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    if (!state.isEditing && state.user != null) {
                        IconButton(onClick = vm::startEditing) {
                            Icon(Icons.Outlined.Edit, "Edit profile")
                        }
                    }
                    IconButton(onClick = vm::showLogoutDialog) {
                        Icon(Icons.Outlined.Logout, "Log out",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoadingProfile -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null && state.user == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = vm::loadProfile) { Text("Retry") }
                    }
                }
            }

            else -> {
                if (state.isEditing) {
                    EditMode(state = state, vm = vm, modifier = Modifier.padding(padding))
                } else {
                    ViewMode(state = state, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// View mode
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewMode(state: ProfileEditUiState, modifier: Modifier = Modifier) {
    val user = state.user ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = user.name?.firstOrNull()?.toString()?.uppercase() ?: "?",
                style      = MaterialTheme.typography.displaySmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(user.name ?: "No name set", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)
        Text(user.phone, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(32.dp))

        InfoRow(icon = Icons.Outlined.LocationOn, label = "Location",
            value = user.location ?: "Not set")

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Skills
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Star, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Skills", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            if (user.skills.isEmpty()) {
                Text("No skills added", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp),
                ) {
                    user.skills.forEach { SkillChip(it) }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit mode
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditMode(
    state: ProfileEditUiState,
    vm: ProfileEditViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text("Full name *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value           = state.name,
            onValueChange   = vm::onNameChanged,
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(20.dp))

        Text("Skills * (comma-separated)", style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value           = state.skillsRaw,
            onValueChange   = vm::onSkillsChanged,
            modifier        = Modifier.fillMaxWidth(),
            placeholder     = { Text("e.g. Kotlin, Android") },
            minLines        = 2,
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        val skills = state.skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (skills.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
            ) {
                skills.take(6).forEach { SkillChip(it) }
                if (skills.size > 6) SkillChip("+${skills.size - 6}")
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Location *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value           = state.location,
            onValueChange   = vm::onLocationChanged,
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            leadingIcon     = { Icon(Icons.Outlined.LocationOn, null) },
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = vm::save,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = !state.isSaving,
            shape    = RoundedCornerShape(12.dp),
        ) {
            if (state.isSaving)
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
            else
                Text("Save Changes", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick  = vm::cancelEditing,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = !state.isSaving,
            shape    = RoundedCornerShape(12.dp),
        ) { Text("Cancel") }

        Spacer(Modifier.height(24.dp))
    }
}