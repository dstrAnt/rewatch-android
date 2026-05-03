package it.polimi.rewatch.unitTests.components.layout

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.layout.CustomLazyGrid
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CustomLazyGridTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    val trivialList = listOf(1, 2, 3)

    @Test
    fun testCustomLazyGrid(){
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

        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithText("3").assertExists()
    }
}