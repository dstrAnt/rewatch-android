package it.polimi.rewatch.screens.settings

import android.annotation.SuppressLint
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions

enum class LoginState(val label: String, val kb: KeyboardType, val button: String, val error: String) {
    LOGIN("Email", KeyboardType.Email, "Next", "Insert a valid email address"),
    CODE("Verification Code", KeyboardType.Number, "Log in", "Code is not valid")
}

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginPage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
    fakeRepo: FakeRepository = FakeRepository(),
) {
    var loginState by remember { mutableStateOf(LoginState.LOGIN) }
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    fun isLogin() = loginState == LoginState.LOGIN

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleLogin() {
        if (isLogin()) {
            if (text.isValidEmail()) {
                isError = false
                loginState = LoginState.CODE
                fakeRepo.generateCode()
                text = ""
            } else isError = true
        } else {
            if (fakeRepo.isValidCode(text)) {
                isError = false
                // TODO: real login logic
                loginState = LoginState.LOGIN
                text = ""
            } else isError = true
        }
    }

    Scaffold(
        topBar = {
            BasicTopBar(actions.onNavigateBack)
        },
        modifier = Modifier
            .padding(
                start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) { paddingValues ->
        Box(
            Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = outerPadding.calculateBottomPadding(),
                )
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(loginState.label) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = loginState.kb),
                    singleLine = true,
                )

                Button(
                    onClick = ::handleLogin,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(loginState.button)
                }

                if (isError) Text(loginState.error, color = Color.Red)
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicTopBar(
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text("Log in")
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