//package com.jobmatch.presentation.auth
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.jobmatch.di.AppDependencies
//import com.jobmatch.util.onError
//import com.jobmatch.util.onSuccess
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//
//// ─────────────────────────────────────────────────────────────────────────────
//// Phone screen
//// ─────────────────────────────────────────────────────────────────────────────
//
//data class PhoneUiState(
//    val phone: String         = "",
//    val isLoading: Boolean    = false,
//    val errorMessage: String? = null,
//    val otpSent: Boolean      = false,
//)
//
//class PhoneViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(PhoneUiState())
//    val uiState: StateFlow<PhoneUiState> = _uiState.asStateFlow()
//
//    fun onPhoneChanged(value: String) {
//        _uiState.update { it.copy(phone = value, errorMessage = null) }
//    }
//
//    fun sendOtp() {
//        val phone = _uiState.value.phone.trim()
//        if (phone.isBlank()) {
//            _uiState.update { it.copy(errorMessage = "Please enter your phone number") }
//            return
//        }
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
//            AppDependencies.sendOtpUseCase(phone)
//                .onSuccess { _uiState.update { s -> s.copy(isLoading = false, otpSent = true) } }
//                .onError   { msg, _ -> _uiState.update { s -> s.copy(isLoading = false, errorMessage = msg) } }
//        }
//    }
//
//    fun resetOtpSent() { _uiState.update { it.copy(otpSent = false) } }
//}
//
//@Composable
//fun PhoneScreen(
//    onOtpSent: (phone: String) -> Unit,
//    vm: PhoneViewModel = viewModel { PhoneViewModel() },
//) {
//    val state by vm.uiState.collectAsStateWithLifecycle()
//
//    LaunchedEffect(state.otpSent) {
//        if (state.otpSent) {
//            onOtpSent(state.phone.trim())
//            vm.resetOtpSent()
//        }
//    }
//
//    Column(
//        modifier            = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//            .padding(horizontal = 28.dp),
//        verticalArrangement = Arrangement.Center,
//    ) {
//        // Brand header
//        Text(
//            text       = "JobMatch",
//            style      = MaterialTheme.typography.displayMedium,
//            color      = MaterialTheme.colorScheme.primary,
//            fontWeight = FontWeight.ExtraBold,
//        )
//        Text(
//            text  = "Find your next opportunity",
//            style = MaterialTheme.typography.bodyLarge,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//        )
//
//        Spacer(Modifier.height(48.dp))
//
//        Text(
//            text       = "Enter your phone number",
//            style      = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.SemiBold,
//        )
//        Text(
//            text  = "We'll send you a one-time password",
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value           = state.phone,
//            onValueChange   = vm::onPhoneChanged,
//            modifier        = Modifier.fillMaxWidth(),
//            label           = { Text("Phone number") },
//            placeholder     = { Text("+91 98765 43210") },
//            prefix          = { Text("+") },
//            keyboardOptions = KeyboardOptions(
//                keyboardType = KeyboardType.Phone,
//                imeAction    = ImeAction.Done,
//            ),
//            keyboardActions = KeyboardActions(onDone = { vm.sendOtp() }),
//            isError         = state.errorMessage != null,
//            singleLine      = true,
//            shape           = RoundedCornerShape(12.dp),
//        )
//
//        if (state.errorMessage != null) {
//            Spacer(Modifier.height(6.dp))
//            Text(
//                text  = state.errorMessage!!,
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.error,
//            )
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        Button(
//            onClick  = vm::sendOtp,
//            modifier = Modifier.fillMaxWidth().height(52.dp),
//            enabled  = !state.isLoading && state.phone.isNotBlank(),
//            shape    = RoundedCornerShape(12.dp),
//        ) {
//            if (state.isLoading) {
//                CircularProgressIndicator(
//                    modifier    = Modifier.size(22.dp),
//                    color       = MaterialTheme.colorScheme.onPrimary,
//                    strokeWidth = 2.dp,
//                )
//            } else {
//                Text("Send OTP", style = MaterialTheme.typography.titleSmall)
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        // Demo hint
//        Surface(
//            shape    = RoundedCornerShape(8.dp),
//            color    = MaterialTheme.colorScheme.surfaceVariant,
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            Text(
//                text      = "Demo mode: OTP is always 123456",
//                style     = MaterialTheme.typography.labelSmall,
//                color     = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier  = Modifier.padding(12.dp),
//                textAlign = TextAlign.Center,
//            )
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
//// OTP screen
//// ─────────────────────────────────────────────────────────────────────────────
//
//data class OtpUiState(
//    val otp: String           = "",
//    val isLoading: Boolean    = false,
//    val errorMessage: String? = null,
//    val verified: Boolean     = false,
//    val isNewUser: Boolean    = false,
//)
//
//class OtpViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(OtpUiState())
//    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()
//
//    fun onOtpChanged(value: String) {
//        if (value.length <= 6 && value.all { it.isDigit() }) {
//            _uiState.update { it.copy(otp = value, errorMessage = null) }
//        }
//    }
//
//    fun verifyOtp(phone: String) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
//            AppDependencies.verifyOtpUseCase(phone, _uiState.value.otp.trim())
//                .onSuccess { (token, user) ->
//                    AppDependencies.sessionManager.saveSession(token, user.phone)
//                    _uiState.update { s ->
//                        s.copy(isLoading = false, verified = true, isNewUser = !user.profileComplete)
//                    }
//                }
//                .onError { msg, _ ->
//                    _uiState.update { s -> s.copy(isLoading = false, errorMessage = msg) }
//                }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OtpScreen(
//    phone: String,
//    onVerified: (isNewUser: Boolean) -> Unit,
//    onBack: () -> Unit,
//    vm: OtpViewModel = viewModel { OtpViewModel() },
//) {
//    val state by vm.uiState.collectAsStateWithLifecycle()
//
//    LaunchedEffect(state.verified) {
//        if (state.verified) onVerified(state.isNewUser)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Verify OTP") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
//                    }
//                },
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier            = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(horizontal = 28.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Spacer(Modifier.height(40.dp))
//
//            Text(
//                "Enter OTP",
//                style      = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//            )
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text      = "Sent to $phone",
//                style     = MaterialTheme.typography.bodyMedium,
//                color     = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center,
//            )
//
//            Spacer(Modifier.height(48.dp))
//
//            OtpInputRow(otp = state.otp, onChanged = vm::onOtpChanged)
//
//            if (state.errorMessage != null) {
//                Spacer(Modifier.height(12.dp))
//                Text(
//                    text  = state.errorMessage!!,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.error,
//                )
//            }
//
//            Spacer(Modifier.height(40.dp))
//
//            Button(
//                onClick  = { vm.verifyOtp(phone) },
//                modifier = Modifier.fillMaxWidth().height(52.dp),
//                enabled  = !state.isLoading && state.otp.length >= 4,
//                shape    = RoundedCornerShape(12.dp),
//            ) {
//                if (state.isLoading) {
//                    CircularProgressIndicator(
//                        modifier    = Modifier.size(22.dp),
//                        color       = MaterialTheme.colorScheme.onPrimary,
//                        strokeWidth = 2.dp,
//                    )
//                } else {
//                    Text("Verify & Continue", style = MaterialTheme.typography.titleSmall)
//                }
//            }
//        }
//    }
//}
////
/////** Six individual digit boxes backed by a zero-size hidden text field. */
////@Composable
////private fun OtpInputRow(otp: String, onChanged: (String) -> Unit) {
////    val focusRequester = remember { FocusRequester() }
////
////    Box {
////        OutlinedTextField(
////            value           = otp,
////            onValueChange   = onChanged,
////            modifier        = Modifier
////                .size(0.dp)
////                .focusRequester(focusRequester),
////            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
////        )
////        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
////            repeat(6) { idx ->
////                val char      = otp.getOrNull(idx)?.toString() ?: ""
////                val isFocused = idx == otp.length
////                Box(
////                    modifier = Modifier
////                        .size(46.dp)
////                        .border(
////                            width = if (isFocused) 2.dp else 1.dp,
////                            color = if (isFocused) MaterialTheme.colorScheme.primary
////                            else MaterialTheme.colorScheme.outline,
////                            shape = RoundedCornerShape(10.dp),
////                        )
////                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
////                    contentAlignment = Alignment.Center,
////                ) {
////                    Text(
////                        text       = char,
////                        style      = MaterialTheme.typography.headlineSmall,
////                        fontWeight = FontWeight.Bold,
////                        textAlign  = TextAlign.Center,
////                    )
////                }
////            }
////        }
////    }
////
////    LaunchedEffect(Unit) {
////        delay(100)
////        focusRequester.requestFocus()
////    }
////}
//
//
//@Composable
//private fun OtpInputRow(otp: String, onChanged: (String) -> Unit) {
//    val focusRequester = remember { FocusRequester() }
//    var isMounted by remember { mutableStateOf(false) }
//
//    // Wait until after first layout pass before requesting focus
//    LaunchedEffect(isMounted) {
//        if (isMounted) {
//            delay(200)
//            runCatching { focusRequester.requestFocus() }  // runCatching prevents crash if still not ready
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .onGloballyPositioned { isMounted = true }  // fires after layout is placed
//    ) {
//        // Hidden text field that captures keyboard input
//        BasicTextField(
//            value = otp,
//            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onChanged(it) },
//            modifier = Modifier
//                .size(1.dp)          // 1dp instead of 0dp — 0dp can cause layout issues
//                .focusRequester(focusRequester),
//            keyboardOptions = KeyboardOptions(
//                keyboardType = KeyboardType.NumberPassword,
//                imeAction    = ImeAction.Done,
//            ),
//        )
//
//        // Visual digit boxes
//        Row(
//            modifier              = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center,
//        ) {
//            repeat(6) { idx ->
//                val char      = otp.getOrNull(idx)?.toString() ?: ""
//                val isFocused = idx == otp.length
//
//                Box(
//                    modifier = Modifier
//                        .size(46.dp)
//                        .padding(horizontal = 4.dp)
//                        .border(
//                            width = if (isFocused) 2.dp else 1.dp,
//                            color = if (isFocused) MaterialTheme.colorScheme.primary
//                            else MaterialTheme.colorScheme.outline,
//                            shape = RoundedCornerShape(10.dp),
//                        )
//                        .background(
//                            color = MaterialTheme.colorScheme.surface,
//                            shape = RoundedCornerShape(10.dp),
//                        )
//                        .clickable { runCatching { focusRequester.requestFocus() } },
//                    contentAlignment = Alignment.Center,
//                ) {
//                    Text(
//                        text       = char,
//                        style      = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold,
//                        textAlign  = TextAlign.Center,
//                    )
//                }
//            }
//        }
//    }
//}

package com.jobmatch.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jobmatch.di.AppDependencies
import com.jobmatch.util.onError
import com.jobmatch.util.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Phone screen — Step 1
// ─────────────────────────────────────────────────────────────────────────────

data class PhoneUiState(
    val phone: String         = "",
    val isLoading: Boolean    = false,
    val errorMessage: String? = null,
    val otpSent: Boolean      = false,
)

class PhoneViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneUiState())
    val uiState: StateFlow<PhoneUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun sendOtp() {
        val phone = _uiState.value.phone.trim()
        if (phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your phone number") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            AppDependencies.sendOtpUseCase(phone)
                .onSuccess { _uiState.update { s -> s.copy(isLoading = false, otpSent = true) } }
                .onError   { msg, _ -> _uiState.update { s -> s.copy(isLoading = false, errorMessage = msg) } }
        }
    }

    fun resetOtpSent() { _uiState.update { it.copy(otpSent = false) } }
}

@Composable
fun PhoneScreen(
    onOtpSent: (phone: String) -> Unit,
    vm: PhoneViewModel = viewModel { PhoneViewModel() },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            onOtpSent(state.phone.trim())
            vm.resetOtpSent()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        // Brand header
        Text(
            text       = "JobMatch",
            style      = MaterialTheme.typography.displayMedium,
            color      = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text  = "Find your next opportunity",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text       = "Enter your phone number",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text  = "We'll send you a one-time password",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value           = state.phone,
            onValueChange   = vm::onPhoneChanged,
            modifier        = Modifier.fillMaxWidth(),
            label           = { Text("Phone number") },
            placeholder     = { Text("+91 98765 43210") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction    = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { vm.sendOtp() }),
            isError         = state.errorMessage != null,
            singleLine      = true,
            shape           = RoundedCornerShape(12.dp),
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text  = state.errorMessage!!,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = vm::sendOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled  = !state.isLoading && state.phone.isNotBlank(),
            shape    = RoundedCornerShape(12.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Send OTP", style = MaterialTheme.typography.titleSmall)
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            shape    = RoundedCornerShape(8.dp),
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text      = "Demo mode: OTP is always 123456",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.padding(12.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP screen — Step 2
// ─────────────────────────────────────────────────────────────────────────────

data class OtpUiState(
    val otp: String           = "",
    val isLoading: Boolean    = false,
    val errorMessage: String? = null,
    val verified: Boolean     = false,
    val isNewUser: Boolean    = false,
)

class OtpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun onOtpChanged(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(otp = value, errorMessage = null) }
        }
    }

    fun verifyOtp(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            AppDependencies.verifyOtpUseCase(phone, _uiState.value.otp.trim())
                .onSuccess { (token, user) ->
                    AppDependencies.sessionManager.saveSession(token, user.phone)
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            verified  = true,
                            // isNewUser = true means profile is NOT complete → go to profile setup
                            isNewUser = !user.profileComplete,
                        )
                    }
                }
                .onError { msg, _ ->
                    _uiState.update { s -> s.copy(isLoading = false, errorMessage = msg) }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phone: String,
    onVerified: (isNewUser: Boolean) -> Unit,
    onBack: () -> Unit,
    vm: OtpViewModel = viewModel { OtpViewModel() },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.verified) {
        if (state.verified) onVerified(state.isNewUser)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                "Enter OTP",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Sent to $phone",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            // Fixed OTP input — no BringIntoViewRequester crash
            OtpInputRow(
                otp       = state.otp,
                onChanged = vm::onOtpChanged,
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = state.errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick  = { vm.verifyOtp(phone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = !state.isLoading && state.otp.length >= 4,
                shape    = RoundedCornerShape(12.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Verify & Continue", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP input row — Fixed: uses BasicTextField + onGloballyPositioned
// No OutlinedTextField at size(0.dp) → no BringIntoViewRequester crash
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OtpInputRow(otp: String, onChanged: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var isPlaced by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaced) {
        if (isPlaced) {
            delay(200)
            runCatching { focusRequester.requestFocus() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { isPlaced = true },
    ) {
        // Invisible 1dp text field — captures keyboard, no scroll/bringIntoView behavior
        BasicTextField(
            value         = otp,
            onValueChange = { v ->
                if (v.length <= 6 && v.all { it.isDigit() }) onChanged(v)
            },
            modifier      = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction    = ImeAction.Done,
            ),
        )

        // Visual digit boxes
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(6) { idx ->
                val char      = otp.getOrNull(idx)?.toString() ?: ""
                val isFocused = idx == otp.length

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .padding(horizontal = 4.dp)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = if (isFocused) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable {
                            runCatching { focusRequester.requestFocus() }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = char,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                    )
                }
            }
        }
    }
}