package it.polimi.rewatch.unitTests.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.font.FontWeight
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.layout.CollapsingLayout
import androidx.compose.ui.unit.*
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.Logo
import it.polimi.rewatch.common.headerColor
import it.polimi.rewatch.screens.home.HomeContent
import it.polimi.rewatch.screens.home.HomeSkeleton
import android.R.id.tabs
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class CollapsingLayoutTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCollapsingLayout() {
        composeTestRule.setContent {
            CollapsingLayout(
                10.dp, 10.dp, Modifier.testTag("collapsingLayout"),
                collapsingTopBar = {
                    Row() {}
                        /*Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            //  .padding(top = 16.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Logo()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ReWatch",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }*/
                },
                pinnedBottomBar = {},
                content = { _, _ -> Row() {}
                    /*Row(
                        Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            //  .padding(top = 16.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Logo()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ReWatch",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }*/
                }
            )
        }

        val node = composeTestRule.onNodeWithTag("collapsingLayout").assertExists()
    }
}