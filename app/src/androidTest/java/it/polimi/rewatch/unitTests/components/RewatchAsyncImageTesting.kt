package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import it.polimi.rewatch.R
import it.polimi.rewatch.components.RewatchAsyncImage
import org.junit.Rule
import org.junit.Test

/**
 * Test Case designed to test Composable RewatchAsyncImage
 */

class RewatchAsyncImageTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRewatchAsyncImage(){
        composeTestRule.setContent { RewatchAsyncImage("app/src/main/res/drawable/ticket1.png", "ticketImage", modifier = Modifier, fallbackResId = R.drawable.ticket1) }

        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("ticketImage").assertExists().assertIsDisplayed()
    }
}