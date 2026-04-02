package it.polimi.rewatch.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.AuthState
import it.polimi.rewatch.common.LinkResult
import it.polimi.rewatch.common.Logo
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import jakarta.inject.Inject
import kotlinx.coroutines.launch


// --- ViewModel ---
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: PersistRepository,
) : ViewModel() {
    val authState = repo.authState
    var email by mutableStateOf("")
    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)

    // Hold collision info so we can resolve it after user decides
    private var pendingCollision: LinkResult.Collision? = null


    fun sendMagicLink() {
        if (email.isBlank()) return
        uiState = AuthUiState.Loading("Sending link…")
        viewModelScope.launch {
            repo.sendSignInLink(email)
                .onSuccess { uiState = AuthUiState.LinkSent }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Failed to send link") }
        }
    }

    fun handleDeepLink(link: String) {
        uiState = AuthUiState.Loading("Signing in…")
        viewModelScope.launch {
            repo.verifySignInLink(email, link)
                .onSuccess { result ->
                    when (result) {
                        is LinkResult.Linked,
                        is LinkResult.SignedIn,
                            -> uiState = AuthUiState.Idle

                        is LinkResult.Collision -> {
                            pendingCollision = result
                            uiState = AuthUiState.MergePrompt
                        }
                    }
                }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Sign-in failed") }
        }
    }

    fun resolveCollision(merge: Boolean) {
        val collision = pendingCollision ?: return
        uiState = AuthUiState.Loading(if (merge) "Merging lists…" else "Signing in…")
        viewModelScope.launch {
            repo.resolveCollision(collision.anonymousUid, collision.credential, merge)
                .onSuccess {
                    pendingCollision = null
                    uiState = AuthUiState.Idle
                }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Failed") }
        }
    }

    fun signInAnonymously() {
        uiState = AuthUiState.Loading("Setting up…")
        viewModelScope.launch {
            repo.signInAnonymously()
                .onSuccess { uiState = AuthUiState.Idle }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Failed") }
        }
    }

    fun logout() {
        repo.logout()
        uiState = AuthUiState.Idle
    }
}

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data class Loading(val message: String) : AuthUiState
    data object LinkSent : AuthUiState
    data object MergePrompt : AuthUiState
    data class Error(val message: String) : AuthUiState
}

// --- Compose Screen ---
@Composable
fun AuthPage(
    viewModel: AuthViewModel = hiltViewModel(),
    actions: NavigationActions,
    onContinue: () -> Unit,
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle(AuthState.SignedOut)
    // If authenticated (non-anonymous), navigate away
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onContinue()
    }
    val context = LocalContext.current


    DisposableEffect(context) {
        val activity = context as MainActivity

        // Function to process the link if we find one
        fun processIntent(intent: Intent) {
            intent.data?.toString()?.let { link ->
                // if (Firebase.auth.isSignInWithEmailLink(link)) {
                // Route this to your AuthViewModel — via shared state,
                // navigation argument, or a SavedStateHandle
                viewModel.handleDeepLink(link)
                //     }
            }
        }

        println("RECEIVED!!!")
        // 1. Check if the app was completely closed and launched via the link
        processIntent(activity.intent)
        // 2. Listen for the link if the app was already open in the background
        val listener = { newIntent: Intent ->
            processIntent(newIntent)
        }
        activity.addOnNewIntentListener(listener)
        // Cleanup when the Composable leaves the screen
        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (val ui = viewModel.uiState) {
                is AuthUiState.Loading -> LoadingContent(ui.message)
                is AuthUiState.LinkSent -> LinkSentContent()
                is AuthUiState.MergePrompt -> MergePromptContent(
                    onMerge = {
                        viewModel.resolveCollision(merge = true)
                        actions.onClearAndNavigateToSettings()
                    },
                    onDiscard = {
                        viewModel.resolveCollision(merge = false)
                    },
                )

                is AuthUiState.Error -> ErrorContent(
                    message = ui.message,
                    onRetry = { viewModel.uiState = AuthUiState.Idle },
                )

                is AuthUiState.Idle -> IdleContent(
                    authState = authState,
                    email = viewModel.email,
                    onEmailChange = { viewModel.email = it },
                    onSendLink = { viewModel.sendMagicLink() },
                    onSkip = { viewModel.signInAnonymously() },
                    onLogout = { viewModel.logout() },
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    authState: AuthState,
    email: String,
    onEmailChange: (String) -> Unit,
    onSendLink: () -> Unit,
    onSkip: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Logo()
            Spacer(Modifier.width(10.dp))
            Text("ReWatch", style = MaterialTheme.typography.headlineLarge)
        }
//        Logo()

        when (authState) {
            is AuthState.Anonymous -> {
                Text(
                    "You're browsing as a guest. Sign in to sync your lists on this device.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                Text(
                    "Sign in to start building your lists",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSendLink() }),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = onSendLink,
            enabled = email.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send magic link")
        }

        if (authState is AuthState.SignedOut) {
            TextButton(onClick = onSkip) {
                Text("Skip for now")
            }
        }

        if (authState is AuthState.Anonymous) {
            TextButton(onClick = onLogout) {
                Text("Sign out guest account")
            }
        }
    }
}

@Composable
private fun LinkSentContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text("Check your email", style = MaterialTheme.typography.headlineSmall)
        Text(
            "We sent you a sign-in link. Tap it to continue.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MergePromptContent(
    onMerge: () -> Unit,
    onDiscard: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Account already exists", style = MaterialTheme.typography.headlineSmall)
        Text(
            "You have lists saved as a guest. Do you want to merge them with your existing account?",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onMerge, modifier = Modifier.fillMaxWidth()) {
            Text("Merge my lists")
        }
        OutlinedButton(onClick = onDiscard, modifier = Modifier.fillMaxWidth()) {
            Text("Discard guest lists")
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text("Try again") }
    }
}