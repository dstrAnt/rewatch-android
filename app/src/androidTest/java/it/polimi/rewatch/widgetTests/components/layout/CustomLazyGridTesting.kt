package it.polimi.rewatch.widgetTests.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.components.layout.CustomLazyGrid
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
    fun testTrivialCustomLazyGrid() {
        composeTestRule.setContent {
            CustomLazyGrid {
                items(
                    items = trivialList,
                    key = { it.toString() },
                ) {
                    Text(it.toString())
                }
            }
        }

        // waitForIdle aspetta che Compose abbia finito di disegnare la UI
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertExists().assertIsDisplayed()
    }

    @Test
    fun testPrettyLongCustomLazyGrid() {
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

        val customLazyGridNode = composeTestRule.onNodeWithTag("customLazyGrid")

        // --- SCENARIO 1: Elementi in cima ---
        // I primissimi elementi devono essere visibili subito
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()

        // Elementi molto lontani non devono esistere (dimostra che è Lazy)
        composeTestRule.onNodeWithText("50").assertDoesNotExist()
        composeTestRule.onNodeWithText("99").assertDoesNotExist()

        // --- SCENARIO 2: Scroll verso il centro ---
        // Scorriamo fino a trovare il 23 e verifichiamo che sia visibile
        customLazyGridNode.performScrollToNode(hasText("23"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("23").assertIsDisplayed()

        // Scorriamo ancora più giù fino a metà lista
        customLazyGridNode.performScrollToNode(hasText("50"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("50").assertIsDisplayed()

        // --- SCENARIO 3: Scroll fino alla fine ---
        customLazyGridNode.performScrollToNode(hasText("99"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("99").assertIsDisplayed()
        composeTestRule.onNodeWithText("98").assertIsDisplayed()

        // Ora che siamo in fondo, il primo elemento (1) deve essere stato rimosso dalla memoria
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
    }
}