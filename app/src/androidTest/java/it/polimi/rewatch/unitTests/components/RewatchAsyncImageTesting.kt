package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.components.RewatchAsyncImage
import org.junit.Rule
import org.junit.Test

class RewatchAsyncImageTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRewatchAsyncImage(){

        try {
            val asyncImage = composeTestRule.setContent { RewatchAsyncImage(null, null, modifier = Modifier) }

            assert(true);
        } catch (_: NullPointerException){
            assert(false);
        }


    }
}