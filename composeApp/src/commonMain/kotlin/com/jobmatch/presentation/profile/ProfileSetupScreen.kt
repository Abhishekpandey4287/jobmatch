//package com.jobmatch.presentation.profile
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.jobmatch.di.AppDependencies
//import com.jobmatch.presentation.components.SkillChip
//import com.jobmatch.util.onError
//import com.jobmatch.util.onSuccess
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//
//// ── ViewModel ─────────────────────────────────────────────────────────────────
//
//data class ProfileSetupUiState(
//    val name: String          = "",
//    val skillsRaw: String     = "",
//    val location: String      = "",
//    val isLoading: Boolean    = false,
//    val errorMessage: String? = null,
//    val saved: Boolean        = false,
//)
//
//class ProfileSetupViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ProfileSetupUiState())
//    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()
//
//    fun onNameChanged(v: String)     { _uiState.update { it.copy(name = v,      errorMessage = null) } }
//    fun onSkillsChanged(v: String)   { _uiState.update { it.copy(skillsRaw = v, errorMessage = null) } }
//    fun onLocationChanged(v: String) { _uiState.update { it.copy(location = v,  errorMessage = null) } }
//
//    fun save() {
//        val s = _uiState.value
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
//            AppDependencies.updateProfileUseCase(s.name, s.skillsRaw, s.location)
//                .onSuccess { _uiState.update { it.copy(isLoading = false, saved = true) } }
//                .onError   { msg, _ -> _uiState.update { it.copy(isLoading = false, errorMessage = msg) } }
//        }
//    }
//}
//
//// ── Screen ────────────────────────────────────────────────────────────────────
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ProfileSetupScreen(
//    onComplete: () -> Unit,
//    vm: ProfileSetupViewModel = viewModel { ProfileSetupViewModel() },
//) {
//    val state by vm.uiState.collectAsStateWithLifecycle()
//
//    LaunchedEffect(state.saved) {
//        if (state.saved) onComplete()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("Set up your profile") })
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(horizontal = 24.dp)
//                .verticalScroll(rememberScrollState()),
//        ) {
//            Spacer(Modifier.height(16.dp))
//
//            Text(
//                text  = "Tell employers about yourself",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//            )
//
//            Spacer(Modifier.height(28.dp))
//
//            // Full name
//            OutlinedTextField(
//                value           = state.name,
//                onValueChange   = vm::onNameChanged,
//                modifier        = Modifier.fillMaxWidth(),
//                label           = { Text("Full name *") },
//                singleLine      = true,
//                shape           = RoundedCornerShape(12.dp),
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // Skills
//            OutlinedTextField(
//                value           = state.skillsRaw,
//                onValueChange   = vm::onSkillsChanged,
//                modifier        = Modifier.fillMaxWidth(),
//                label           = { Text("Skills (comma-separated) *") },
//                placeholder     = { Text("e.g. Kotlin, Android, REST APIs") },
//                minLines        = 2,
//                shape           = RoundedCornerShape(12.dp),
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
//            )
//
//            // Live skill preview
//            val skills = state.skillsRaw
//                .split(",")
//                .map { it.trim() }
//                .filter { it.isNotBlank() }
//            if (skills.isNotEmpty()) {
//                Spacer(Modifier.height(8.dp))
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(6.dp),
//                    modifier = Modifier.fillMaxWidth(),
//                ) {
//                    skills.take(5).forEach { SkillChip(it) }
//                    if (skills.size > 5) SkillChip("+${skills.size - 5}")
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // Location
//            OutlinedTextField(
//                value           = state.location,
//                onValueChange   = vm::onLocationChanged,
//                modifier        = Modifier.fillMaxWidth(),
//                label           = { Text("Location *") },
//                placeholder     = { Text("e.g. Bengaluru") },
//                singleLine      = true,
//                shape           = RoundedCornerShape(12.dp),
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//            )
//
//            if (state.errorMessage != null) {
//                Spacer(Modifier.height(8.dp))
//                Text(
//                    text  = state.errorMessage!!,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.error,
//                )
//            }
//
//            Spacer(Modifier.height(32.dp))
//
//            Button(
//                onClick  = vm::save,
//                modifier = Modifier.fillMaxWidth().height(52.dp),
//                enabled  = !state.isLoading,
//                shape    = RoundedCornerShape(12.dp),
//            ) {
//                if (state.isLoading) {
//                    CircularProgressIndicator(
//                        modifier    = Modifier.size(22.dp),
//                        color       = MaterialTheme.colorScheme.onPrimary,
//                        strokeWidth = 2.dp,
//                    )
//                } else {
//                    Text("Save & Continue", fontWeight = FontWeight.SemiBold)
//                }
//            }
//
//            Spacer(Modifier.height(24.dp))
//        }
//    }
//}

package com.jobmatch.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.jobmatch.platform.MediaPicker
import com.jobmatch.presentation.components.SkillChip
import com.jobmatch.util.onError
import com.jobmatch.util.onSuccess
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val name: String = "",
    val skillsRaw: String = "",
    val location: String = "",
    val audioVideoUri: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

class ProfileSetupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onNameChanged(v: String) {
        _uiState.update {
            it.copy(name = v, errorMessage = null)
        }
    }

    fun onSkillsChanged(v: String) {
        _uiState.update {
            it.copy(skillsRaw = v, errorMessage = null)
        }
    }

    fun onLocationChanged(v: String) {
        _uiState.update {
            it.copy(location = v, errorMessage = null)
        }
    }

    fun onMediaPicked(uri: String?) {
        _uiState.update {
            it.copy(audioVideoUri = uri)
        }
    }

    fun save() {
        val s = _uiState.value

        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true)
            }

            AppDependencies.updateProfileUseCase(
                s.name,
                s.skillsRaw,
                s.location
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saved = true
                        )
                    }
                }
                .onError { msg, _ ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = msg
                        )
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onComplete: () -> Unit,
    vm: ProfileSetupViewModel = viewModel {
        ProfileSetupViewModel()
    },
) {

    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onComplete()
        }
    }

    val mediaPicker = MediaPicker

    val launchPicker = mediaPicker.registerPicker { uri ->
        vm.onMediaPicked(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Complete your profile",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Step 2 of 2",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tell employers about yourself",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("Full name *")

            OutlinedTextField(
                value = state.name,
                onValueChange = vm::onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("e.g. Abhishek Pandey")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
            )

            Spacer(Modifier.height(20.dp))

            SectionLabel("Skills * (comma-separated)")

            OutlinedTextField(
                value = state.skillsRaw,
                onValueChange = vm::onSkillsChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("e.g. Kotlin, Android, REST APIs")
                },
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
            )

            val skills = state.skillsRaw
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (skills.isNotEmpty()) {

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {

                    skills.take(5).forEach {
                        SkillChip(it)
                    }

                    if (skills.size > 5) {
                        SkillChip("+${skills.size - 5}")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionLabel("Location *")

            OutlinedTextField(
                value = state.location,
                onValueChange = vm::onLocationChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("e.g. Mumbai")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.LocationOn,
                        null
                    )
                },
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("Short intro (optional)")

            Text(
                text = "Upload a short audio or video to stand out to recruiters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(10.dp))

            MediaUploadBox(
                uri = state.audioVideoUri,
                onClick = launchPicker,
                onClear = {
                    vm.onMediaPicked(null)
                },
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = vm::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
            ) {

                if (state.isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )

                } else {

                    Text(
                        "Save & Find Jobs",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun MediaUploadBox(
    uri: String?,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),

        contentAlignment = Alignment.Center,
    ) {

        if (uri == null) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Tap to add audio/video intro",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                verticalAlignment = Alignment.CenterVertically,
            ) {

                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = uri,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                )

                IconButton(onClick = onClear) {

                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = null
                    )
                }
            }
        }
    }
}