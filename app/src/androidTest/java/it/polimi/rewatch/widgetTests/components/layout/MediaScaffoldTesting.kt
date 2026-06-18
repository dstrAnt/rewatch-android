package it.polimi.rewatch.widgetTests.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import it.polimi.rewatch.components.layout.MediaScaffold
import org.junit.Rule
import org.junit.Test

/**
 * Test Case designed to test Composable MediaScaffold
 */

class MediaScaffoldTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    /* Test Case to verify MediaScaffold Compositions */

    @Test
    fun testMediaScaffold(){
        composeTestRule.setContent {
            MediaScaffold(title = null, scrollState = rememberLazyListState(), onNavigateBack = { }, modifier = Modifier.testTag("mediaScaffold"),
                content = { padding -> Text(modifier = Modifier.padding(PaddingValues(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr))),
                    text = "MyPadding") })
        }

        val mediaScaffold = composeTestRule.onNodeWithTag("mediaScaffold").assertExists()
    }
}