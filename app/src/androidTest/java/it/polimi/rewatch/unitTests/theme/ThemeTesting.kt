package it.polimi.rewatch.unitTests.theme

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import it.polimi.rewatch.theme.*;
import org.junit.Rule

internal class ThemeTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppTheme() {

        composeTestRule.setContent { RewatchTheme(darkTheme = true, dynamicColor =  true) { Text("Hello") } }

        Thread.sleep(60000);

        composeTestRule.onNodeWithText("Hello").assertExists();

        assert(true);
    }

}