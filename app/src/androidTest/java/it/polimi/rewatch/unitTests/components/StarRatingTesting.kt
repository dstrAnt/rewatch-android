package it.polimi.rewatch.unitTests.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.components.StarRating
import org.junit.Rule
import org.junit.Test

class StarRatingTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStarRating() {

        composeTestRule.setContent { StarRating(0f) }

        Thread.sleep(40000);

        // Trova TUTTI i nodi nell'albero profondo, inclusi i contenitori
        val tuttiINodi = composeTestRule.onAllNodes(isRoot(), useUnmergedTree = true);

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("STRUTTURA_UI");


    }

    @Test
    fun testMidStarsRating() {
        var finalRating = 0f
        val maxStars = 5

        composeTestRule.setContent {
            StarRating(
                rating = 0f,
                maxRating = maxStars,
                starSize = 15.dp,               // Standard Dimension for the test
                onRatingChanged = { finalRating = it },
                modifier = Modifier.testTag("starIcon")     // Necessary unique tag
            )
        }

        // 1. Finding the container
        val node = composeTestRule.onNodeWithTag("starIcon")

        node.assertExists().assertIsDisplayed();

        // 2. Simulate a Tap to the center (50% of width)
        // Along 5 stars, it should return 2.5
        node.performTouchInput {
            click(center)
        }

        // 3. Verification of the result
        assert(finalRating == 2.5f)
    }

    @Test
    fun testSwipingStarRating() {
        var finalRating = 0f
        composeTestRule.setContent {
            StarRating(
                rating = 0f,
                onRatingChanged = { finalRating = it },
                modifier = Modifier.testTag("starIcon")
            )
        }

        composeTestRule.onNodeWithTag("starIcon").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithTag("starIcon").performTouchInput {
            // I swiper from 20% width to 80% width
            swipeLeft(startX = width * 0.2f, endX = width * 0.8f)
        }

        // Verifying that the value had changed to 4 (80% width corresponds to 4 of 5 stars)
        assert(finalRating == 4f)
    }

    val IconKey = SemanticsPropertyKey<ImageVector>("IconKey")
    var SemanticsPropertyReceiver.iconType by IconKey


    fun hasIcon(icon: ImageVector): SemanticsMatcher =
        SemanticsMatcher.expectValue(IconKey, icon)

    @Test
    fun testNumberOfStarIcons(){

        val maxStars = 5

        composeTestRule.setContent {
            StarRating(
                rating = 0f,
                maxRating = maxStars,
                starSize = 15.dp,               // Standard Dimension for the test
                modifier = Modifier.testTag("starIcon")     // Necessary unique tag
            )
        }

        composeTestRule
            .onAllNodes(hasIcon(Icons.Default.Star))
            .assertCountEquals(maxStars)

    }

}