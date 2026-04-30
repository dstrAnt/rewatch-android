package it.polimi.rewatch.unitTests.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.printToString
import org.junit.Test
import it.polimi.rewatch.theme.*;
import org.junit.Rule

internal class ThemeTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppTheme() {

        composeTestRule.setContent { RewatchTheme(darkTheme = true, dynamicColor =  true) { Text("Hello") } }

        Thread.sleep(10000);

        val node = composeTestRule.onNodeWithText("Hello").assertExists();

        Thread.sleep(10000);

        node.onParent().assertExists().assertIsDisplayed()

        Thread.sleep(10000);

        val parentNode = node.onParent();

        //val theme = (parentNode as MaterialTheme)

        node.onParent().printToLog("Printing RewatchTheme's MaterialTheme")

        Thread.sleep(10000);

        //assert(true);
    }

}