package it.polimi.rewatch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.model.PosterMedia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(media: PosterMedia, onDismiss: () -> Unit) {
    //var showBottomSheet by remember { mutableStateOf(false) }
    //val sheetState = rememberModalBottomSheetState()
    // 1. Create a scroll state
    // val scrollState = rememberScrollState()
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Button(onClick = { showBottomSheet = true }) {
//            Text("Show Stretchy Overlay")
//        }
//    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        //sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        //contentColor = MaterialTheme.colorScheme.surface,
    ) {
        val iconSize = 45.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 2. Add the verticalScroll modifier to your content
                //verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // I'm adding a few extra rows here so you can easily
            // test the drag and stretch effect.
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    modifier = Modifier.padding(vertical = 12.dp),
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Info,
//                        contentDescription = "Info Icon",
//                    )
//                    Text(
//                        text = "Scroll and pull me to see the stretch effect!",
//                        style = MaterialTheme.typography.bodyLarge,
//                    )
//                }
            //PosterItem(media,{}, Modifier.size(90.dp, 110.dp))
            Column {
                Text(
                    media.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    media.releaseDate.toString(),
                )
            }
            HorizontalDivider(Modifier.layoutEvade(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                IconButton(
                    onClick = { println("Share clicked!") },
                    modifier = Modifier.size(60.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(37.dp),
                    )
                }
                IconButton(
                    onClick = { println("Share clicked!") },
                    modifier = Modifier.size(60.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(37.dp),
                    )
                }
            }
            HorizontalDivider(Modifier.layoutEvade(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                StarRating(
                    4.0f,
                    starSize = iconSize,
                )
            }
            HorizontalDivider(Modifier.layoutEvade(16.dp))
            Text(
                "Movie was watched",
            )
            HorizontalDivider(Modifier.layoutEvade(16.dp))
            Text(
                "Open Movie",
            )


            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
