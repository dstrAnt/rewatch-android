package it.polimi.rewatch.screens.person

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import it.polimi.rewatch.theme.RewatchTheme

@AndroidEntryPoint // 2. Add this annotation!
class PersonActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val personId = intent.getIntExtra("EXTRA_PERSON_ID", -1)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {
            RewatchTheme {
                Scaffold { padding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        // Passiamo il ViewModel già pronto alla pagina
                        PersonPage(
                            personId = personId,
                            padding = padding,
                            onNavigateBack = { finish() },
                        )
                    }
                }
            }
        }
    }
}