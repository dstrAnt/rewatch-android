package it.polimi.rewatch.unitTests.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.layout.CustomLazyGrid
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test Cases designed to test Composable CustomLazyGrid
 */

class CustomLazyGridTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    val trivialList = listOf(1, 2, 3)
    val prettyLongList = List(100) { it }

    @Test
    fun testTrivialCustomLazyGrid(){
        composeTestRule.setContent {
            CustomLazyGrid() {
                items(
                    items = trivialList,
                    key = { it.toString() },
                ) {
                    Text(it.toString())
                }
            }
        }

        Thread.sleep(10000)
        composeTestRule.onNodeWithText("1").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertExists().assertIsDisplayed()
    }

    @Test
    fun testPrettyLongCustomLazyGrid(){
        composeTestRule.setContent {
            Column(modifier = Modifier) {
                Spacer(Modifier.height(16.dp))

                CustomLazyGrid(Modifier.testTag("customLazyGrid")) {

                    items(
                        items = prettyLongList,
                        key = { it.toString() },
                    ) {

                            Text(it.toString(), Modifier.size(140.dp))
                            Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        /* CustomLazyGrid will be tested with 3 different scenarios ("Appearances") */

        val customLazyGridNode = composeTestRule.onNodeWithTag("customLazyGrid")

        verifyFirstAppearance(composeTestRule)

        customLazyGridNode.performTouchInput { swipeUp(400f, 200f) }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        verifySecondAppearance(composeTestRule)

        customLazyGridNode.performTouchInput { swipeUp() }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        verifyThirdAppearance(composeTestRule)

    }
}

private fun SemanticsNodeInteraction.scrollToNodeWithText(text: String) {
    this.performScrollToNode(hasText(text))
}

    /* Scenario where CustomLzaGrid shows the first items at the top */
private fun verifyFirstAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("1").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("3").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("9").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("14").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("23").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithText("40").assertDoesNotExist()
    composeTestRule.onNodeWithText("66").assertDoesNotExist()
    composeTestRule.onNodeWithText("100").assertDoesNotExist()

}

/* Scenario where CustomLzaGrid does not show the first items at the top */
private fun verifySecondAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("14").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("16").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithText("20").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("50").assertDoesNotExist()
    composeTestRule.onNodeWithText("100").assertDoesNotExist()

}

/* Scenario where CustomLzaGrid shows the last items at the bottom */
private fun verifyThirdAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("80").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("85").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("92").assertExists().assertIsDisplayed()


    composeTestRule.onNodeWithText("98").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("99").assertExists().assertIsDisplayed()

}