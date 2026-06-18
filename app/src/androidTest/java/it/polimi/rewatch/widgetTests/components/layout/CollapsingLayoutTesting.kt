package it.polimi.rewatch.widgetTests.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.Logo
import it.polimi.rewatch.common.headerColor
import it.polimi.rewatch.components.layout.CollapsingLayout
import it.polimi.rewatch.components.layout.CustomLazyGrid
import it.polimi.rewatch.theme.Purple80
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test Cases designed to test Composable CollapsingLayout
 */

class CollapsingLayoutTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    val trivialList = List(200) { it }

    /* Initializing Composable CollapsingLayout with similar Content with respect to the ReWatch Application */
    @Before
    fun setup(){
        composeTestRule.setContent {

            CollapsingLayout(
                collapsingTopHeight = 64.dp,
                pinnedBottomHeight = 50.dp,
                Modifier.testTag("collapsingLayout"),
                collapsingTopBar = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            .padding(top = 10.dp)
                            .testTag("collapseTopBar"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Logo()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ReWatch",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Purple80
                        )
                        Text(
                            text = " test",
                            color = Purple80
                        )
                    }
                },
                pinnedBottomBar = {
                    Row(modifier = Modifier.testTag("pinBottomBar")){
                        Text(text = "Bottom ReWatch", color = Purple80)
                    }
                },
                content = { listState, _ ->
                    CustomLazyGrid(
                        state = listState,
                        modifier = Modifier
                            .padding(vertical = 120.dp)
                            .testTag("myLazyGrid"),
                    ) {
                        items(
                            items = trivialList,
                            key = { it.toString() },
                        ) {
                            Text(it.toString())
                        }
                    }
                },
            )
        }
    }

    @Test
    fun testCollapsingLayoutHidesWhenScrollingDown() {

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("collapsingLayout").assertExists()
        composeTestRule.onNodeWithTag("collapseTopBar").assertExists()
        composeTestRule.onNodeWithTag("pinBottomBar").assertExists()

        composeTestRule.onNodeWithText("ReWatch").assertExists().assertIsDisplayed()

        val myLazyGrid = composeTestRule.onNodeWithTag("myLazyGrid").assertExists().assertIsDisplayed()

        myLazyGrid.performTouchInput {
                swipeUp()
            }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("collapseTopBar").assertIsNotDisplayed()

        composeTestRule.onNodeWithText("ReWatch").assertExists().assertIsNotDisplayed()
    }

    @Test
    fun testCollapsingLayoutReappearsWhenScrollingUp() {

        val myLazyGrid = composeTestRule.onNodeWithTag("myLazyGrid")

        myLazyGrid.performTouchInput {
            swipeUp()
        }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        myLazyGrid.performTouchInput {
            swipeDown()
        }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("collapseTopBar").assertIsDisplayed()
    }

    @Test
    fun testCollapsingLayoutPinnedBottomBarStaysVisible() {

        composeTestRule
            .onNodeWithTag("myLazyGrid")
            .performTouchInput {
                swipeUp()
            }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("pinBottomBar")
            .assertIsDisplayed()
    }
}