package it.polimi.rewatch.screens.settings

import android.annotation.SuppressLint
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
import it.polimi.rewatch.common.NavigationActions

enum class LoginState(val label: String) {
    LOGIN("Email"),
    CODE("Verification Code")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginPage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
) {
    var loginState by remember { mutableStateOf(LoginState.LOGIN) }
    var text by remember { mutableStateOf("") }

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                )

                Button(
                    onClick = {
                        loginState = when (loginState) {
                            LoginState.LOGIN -> {
                                LoginState.CODE
                            }

                            LoginState.CODE -> {
                                LoginState.LOGIN
                            }
                        }
                        text = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (loginState == LoginState.LOGIN) "Next" else "Log in")
                }
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