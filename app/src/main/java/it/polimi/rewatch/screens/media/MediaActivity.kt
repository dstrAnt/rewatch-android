/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.polimi.rewatch.screens.media

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import dagger.hilt.android.AndroidEntryPoint
import it.polimi.rewatch.theme.RewatchTheme

@AndroidEntryPoint // 2. Add this annotation!
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MediaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val movieId = intent.getIntExtra("EXTRA_MEDIA_ID", -1)
        val mediaType = intent.getSerializableExtra("EXTRA_MEDIA_TYPE", MediaType::class.java)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {
            RewatchTheme {
                Scaffold { padding ->
                    MediaPage(
                        movieId = movieId,
                        mediaType = mediaType ?: MediaType.MOVIE,
                        padding = padding,
                        onNavigateBack = { finish() },
                    )
                }
            }
        }
    }
}
