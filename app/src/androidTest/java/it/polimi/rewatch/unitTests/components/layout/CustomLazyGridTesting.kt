package it.polimi.rewatch.unitTests.components.layout

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
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.layout.CustomLazyGrid
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CustomLazyGridTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    val trivialList = listOf(1, 2, 3)
    val prettyLongList = listOf(1.. 20)

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
            CustomLazyGrid(Modifier.testTag("customLazyGrid")) {
                items(
                    items = prettyLongList,
                    key = { it.toString() },
                ) {
                    Text(it.toString(), Modifier.size(400.dp, 10.dp))
                }
            }
        }

        Thread.sleep(10000)
        val customLazyGridNode = composeTestRule.onNodeWithTag("customLazyGrid")

        verifyFirstAppearance(composeTestRule)
        
        customLazyGridNode.scrollToNodeWithText("9")

        Thread.sleep(10000)

        verifySecondAppearance(composeTestRule)

        customLazyGridNode.scrollToNodeWithText("18")

        Thread.sleep(10000)

        verifyThirdAppearance(composeTestRule)

        customLazyGridNode.scrollToNodeWithText("90")

        Thread.sleep(10000)

        verifyFourthAppearance(composeTestRule)

    }
}

fun SemanticsNodeInteraction.scrollToNodeWithText(text: String) {
    this.performScrollToNode(hasText(text))
}

fun verifyFirstAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("1").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("3").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("9").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithText("14").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("66").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("100").assertExists().assertIsNotDisplayed()

}

fun verifySecondAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("1").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("3").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("9").assertExists().assertIsNotDisplayed()

    composeTestRule.onNodeWithText("14").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("16").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithText("20").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("100").assertExists().assertIsNotDisplayed()

}

fun verifyThirdAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("1").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("3").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("9").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("14").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("16").assertExists().assertIsNotDisplayed()

    composeTestRule.onNodeWithText("18").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("21").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("24").assertExists().assertIsDisplayed()


    composeTestRule.onNodeWithText("30").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("100").assertExists().assertIsNotDisplayed()

}

fun verifyFourthAppearance(composeTestRule: ComposeContentTestRule){

    composeTestRule.onNodeWithText("1").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("3").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("9").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("14").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("16").assertExists().assertIsNotDisplayed()

    composeTestRule.onNodeWithText("18").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("21").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("24").assertExists().assertIsNotDisplayed()


    composeTestRule.onNodeWithText("91").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("95").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("100").assertExists().assertIsDisplayed()

}
