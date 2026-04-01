package it.polimi.rewatch.screens.settings

import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.BasicTopBar
import it.polimi.rewatch.common.Logo
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import jakarta.inject.Inject
import kotlinx.coroutines.launch

enum class LoginState {
    LOGIN, LINK
}

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}


@HiltViewModel
class LoginViewModel @Inject constructor(
    val repository: PersistRepository,
) : ViewModel() {
    fun sendSignInLink(email: String) {
        viewModelScope.launch {
            repository.sendSignInLink(email)
        }
    }

    fun verifySignInLink(email: String, link: String) {
        viewModelScope.launch {
            repository.verifySignInLink(email, link)
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginPage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
    vm: LoginViewModel = hiltViewModel(),
) {
    vm.repository
    var loginState by remember { mutableStateOf(LoginState.LOGIN) }
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    fun isLogin() = loginState == LoginState.LOGIN
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    DisposableEffect(context) {
        val activity = context as MainActivity

        // Function to process the link if we find one
        fun processIntent(intent: Intent?) {
            val link = intent?.data?.toString()
            if (link != null) {
                //val savedEmail = AuthPrefs.getSavedEmail(context)
                if (email.isNotEmpty()) {
                    // Send it to the ViewModel!
                    vm.verifySignInLink(email, link)
                    actions.onClearAndNavigateToSettings()
                    // Clear the email so we don't accidentally reuse it later
                }
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


    fun handleLogin() {
        if (email.isValidEmail()) {
            isError = false
            loginState = LoginState.LINK
            vm.sendSignInLink(email)
        } else isError = true
    }

    Scaffold(
        topBar = {
            BasicTopBar("Log in", rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack)) { actions.onNavigateBack() }
        },
        modifier = Modifier.padding(
            start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
        ),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .imePadding()                // handle keyboard space need
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = outerPadding.calculateBottomPadding(),
                )
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Logo()
            Spacer(Modifier.height(16.dp))

            // variable content
            if (isLogin()) {
                val icon = Icons.AutoMirrored.Filled.Login
                val txtLine = "Sign in to synchronize all of your lists to your account"
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        txtLine,
                        style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Insert a valid email address",
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    trailingIcon = {
                        if (isError) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                if (isError) Spacer(Modifier.height(10.dp))

                // buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = ::handleLogin,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Next")
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 15.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        null, Modifier.size(40.dp),
                        MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(15.dp))
                    Text(
                        "Check your inbox to finalize the login process",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(35.dp))
                    Text("We sent a verification link to")
                    Text(email, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

