package it.polimi.rewatch.screens.settings

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BorderBox
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import jakarta.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    val repository: PersistRepository,
) : ViewModel()

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsPage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
    vm: SettingsViewModel = hiltViewModel(),
) {
    Scaffold(
        topBar = {
            SettingsTopBar()
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
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                BorderBox(
                    onClick = if (vm.repository.isLogged()) actions.onNavigateToProfile else actions.onNavigateToLogin,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Icon(
                            if (vm.repository.isLogged()) Icons.Default.Person else Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(45.dp),
                        )
                        Column {
                            Text(
                                if (vm.repository.isLogged()) "Your Profile" else "Sign in",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                if (vm.repository.isLogged()) "See your account and the status of your lists"
                                else "Synchronize your watchlist and watched contents",
                                color = Color.Gray,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Rewatch-Android v0.9b", style = MaterialTheme.typography.bodySmall)
                    Text("Politecnico di Milano © 2026", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar() {
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars), // Keeps it below the system status bar
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
                .height(55.dp), // Standard M3 SearchBar height
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}