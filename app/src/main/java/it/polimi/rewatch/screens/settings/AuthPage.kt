package it.polimi.rewatch.screens.settings

import android.content.Intent
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.IconTopBar
import it.polimi.rewatch.common.LinkResult
import it.polimi.rewatch.common.Logo
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import jakarta.inject.Inject
import kotlinx.coroutines.launch


fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// --- ViewModel ---
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: PersistRepository,
) : ViewModel() {
    var email by mutableStateOf("")
    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)

    // Hold collision info so we can resolve it after user decides
    private var pendingCollision: LinkResult.Collision? = null

    fun reset() {
        uiState = AuthUiState.Idle
        email = ""
    }

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
                            -> uiState = AuthUiState.Success

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
}

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data class Loading(val message: String) : AuthUiState
    data object LinkSent : AuthUiState
    data object Success : AuthUiState
    data object MergePrompt : AuthUiState
    data class Error(val message: String) : AuthUiState
}

// --- Compose Screen ---
@Composable
fun AuthPage(
    outerPadding: PaddingValues,
    viewModel: AuthViewModel = hiltViewModel(),
    actions: NavigationActions,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            // This fires exactly when the Composable leaves the screen.
            // In Nav3, this happens AFTER the exit transition animation finishes.
            viewModel.reset()
        }
    }

    DisposableEffect(context) {
        val activity = context as? MainActivity

        // Function to process the link if we find one
        fun processIntent(intent: Intent?) {
            intent?.data?.toString()?.let { link ->
                // Route this to your AuthViewModel — via shared state,
                // navigation argument, or a SavedStateHandle
                viewModel.handleDeepLink(link)
            }
        }

        println("RECEIVED!!!")
        // 1. Check if the app was completely closed and launched via the link
        processIntent(activity?.intent)
        // 2. Listen for the link if the app was already open in the background
        val listener = { newIntent: Intent ->
            processIntent(newIntent)
        }
        activity?.addOnNewIntentListener(listener)
        // Cleanup when the Composable leaves the screen
        onDispose {
            activity?.removeOnNewIntentListener(listener)
        }
    }

    Scaffold(
        topBar = {
            IconTopBar(
                "",
                rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                color = Color.Transparent,
            ) { actions.onNavigateBack() }
        },
        modifier = Modifier.padding(
            start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
        ).testTag("auth_page"),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .imePadding()     // handle keyboard space need
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = outerPadding.calculateBottomPadding(),
                )
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val ui = viewModel.uiState) {
                is AuthUiState.Loading -> LoadingContent(ui.message)
                is AuthUiState.LinkSent -> LinkSentContent()
                is AuthUiState.Success -> SuccessContent {
                    actions.onNavigateBack()
                }

                is AuthUiState.MergePrompt -> MergePromptContent(
                    onMerge = {
                        viewModel.resolveCollision(merge = true)
                        actions.onNavigateBack()
                    },
                    onDiscard = {
                        viewModel.resolveCollision(merge = false)
                        actions.onNavigateBack()
                    },
                )

                is AuthUiState.Error -> ErrorContent(
                    message = ui.message,
                    onRetry = { viewModel.uiState = AuthUiState.Idle },
                )

                is AuthUiState.Idle -> IdleContent(
                    email = viewModel.email,
                    onEmailChange = { viewModel.email = it },
                    onSendLink = { viewModel.sendMagicLink() },
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendLink: () -> Unit,
) {
    Column(
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
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
        Text(
            "Sign in to synchronize your lists",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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
            modifier = Modifier.fillMaxWidth().testTag("signInTextField"),
        )

        Button(
            onClick = onSendLink,
            enabled = email.isValidEmail(),
            modifier = Modifier.fillMaxWidth().testTag("sendAuthLinkButton"),
        ) {
            Text("Send link")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SuccessContent(
    onMerge: () -> Unit,
) {
    Column(
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Default.CheckCircleOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text("Success", style = MaterialTheme.typography.headlineSmall)
        Text(
            "You are logged in.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onMerge, modifier = Modifier.fillMaxWidth().testTag("doneButton")) {
            Text("Done")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LinkSentContent() {
    Column(
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
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
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Account already exists", style = MaterialTheme.typography.headlineSmall)
        Text(
            "You have lists saved as a guest. Do you want to merge them with your existing account?",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onMerge, modifier = Modifier.fillMaxWidth().testTag("mergeButton")) {
            Text("Merge my lists")
        }
        OutlinedButton(onClick = onDiscard, modifier = Modifier.fillMaxWidth().testTag("discardMergeButton")) {
            Text("Discard guest lists")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
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
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text("Try again") }
    }
}