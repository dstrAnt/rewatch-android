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
    fun testAppDarkTheme() {

        composeTestRule.setContent { RewatchTheme(darkTheme = true) { Text("Hello") } }

        Thread.sleep(10000);

        composeTestRule.onNodeWithText("Hello").assertExists();

        Thread.sleep(10000);
    }

    @Test
    fun testAppLightTheme (){

        composeTestRule.setContent { RewatchTheme(darkTheme = false) { Text("World") } }

        Thread.sleep(10000);

        composeTestRule.onNodeWithText("World").assertExists();

        Thread.sleep(10000);


    }

}