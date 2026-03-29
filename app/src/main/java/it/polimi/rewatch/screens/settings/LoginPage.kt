package it.polimi.rewatch.screens.settings

import android.annotation.SuppressLint
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.R
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import jakarta.inject.Inject

enum class LoginState(val label: String, val kb: KeyboardType, val button: String, val error: String) {
    LOGIN("Email", KeyboardType.Email, "Next", "Insert a valid email address"),
    CODE("Verification Code", KeyboardType.Number, "Log in", "Code is not valid")
}

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel // Tells Hilt to manage this ViewModel
class LoginViewModel @Inject constructor(
    repository: FakeRepository,
) : ViewModel() {
    val fakeRepo = repository
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginPage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
    vm: LoginViewModel = hiltViewModel(),
) {
    val fr = vm.fakeRepo
    var loginState by remember { mutableStateOf(LoginState.LOGIN) }
    var text by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    fun isLogin() = loginState == LoginState.LOGIN

    val scrollState = rememberScrollState()

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleLogin() {
        if (isLogin()) {
            if (text.isValidEmail()) {
                isError = false
                loginState = LoginState.CODE
                email = text
                fr.generateCode()
                text = ""
            } else isError = true
        } else {
            if (fr.isValidCode(text)) {
                isError = false
                fr.login(email)
                actions.onClearAndNavigateToSettings()
            } else isError = true
        }
    }

    Scaffold(
        topBar = {
            BasicTopBar(actions.onNavigateBack, "Log in")
        },
        modifier = Modifier
            .padding(
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

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_launcher_monochrome), contentDescription = null,
                )
                Text(
                    "Rewatch",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )

            }
            Spacer(Modifier.height(16.dp))
            val icon = if (isLogin()) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Send
            val txtLine = if (isLogin()) "Sign in to synchronize all of your lists to your account"
            else "Check your inbox and enter here your verification code"
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
                value = text,
                onValueChange = { text = it },
                label = { Text(loginState.label) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = loginState.kb),
                singleLine = true,
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text = loginState.error,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error, // Usa il rosso del tema
                        )
                    }
                },
                trailingIcon = {
                    if (isError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Errore",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )

            if (isError) Spacer(Modifier.height(10.dp))

            Button(
                onClick = ::handleLogin,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(loginState.button)
            }
            Spacer(Modifier.height(16.dp))
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicTopBar(
    onBack: () -> Unit,
    title: String,
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
    )
}