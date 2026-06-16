package it.polimi.rewatch.unitTests.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.components.StarRating
import org.junit.Rule
import org.junit.Test

/**
 * Test Case designed to test Composable StarRating
 */

class StarRatingTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        Thread.sleep(2000)

        // 3. Verification of the result
        composeTestRule.runOnIdle {
            assert(finalRating == 2.5f)
        }

        node.performTouchInput {
            click(Offset(left, center.y))
        }

        Thread.sleep(2000)

        composeTestRule.runOnIdle {
            assert(finalRating == 0f)
        }

        node.performTouchInput {
            click(Offset(right, center.y))
        }

        Thread.sleep(2000)

        composeTestRule.runOnIdle {
            assert(finalRating == 5f)
        }
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

        val starIcon =  composeTestRule.onNodeWithTag("starIcon").assertExists().assertIsDisplayed()

        starIcon.performTouchInput {
            // I swiper from 20% width to 80% width
            swipeRight(startX = width * 0.2f, endX = width * 0.8f)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verifying that the value had changed to 4 (80% width corresponds to 4 of 5 stars)
        assert(finalRating == 4f)

        starIcon.performTouchInput {
            // I swiper from 80% width to 40% width
            swipeLeft(startX = width * 0.8f, endX = width * 0.4f)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verifying that the value had changed to 2 (40% width corresponds to 2 of 5 stars)
        assert(finalRating == 2f)

        starIcon.performTouchInput {
            // I swiper from 40% width to 100% width
            swipeRight(startX = width * 0.4f, endX = width * 1f)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verifying that the value had changed to 5 (100% width corresponds to 5 of 5 stars)
        assert(finalRating == 5f)

        starIcon.performTouchInput {
            // I swiper from 100% width to 0% width
            swipeLeft(startX = width * 1f, endX = width * 0f)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verifying that the value had changed to 0 (0% width corresponds to 0 of 5 stars)
        assert(finalRating == 0f)
    }

    @Test
    fun testNumberOfStarIcons(){

        val maxStars = 7

        composeTestRule.setContent {
            StarRating(
                rating = 0f,
                maxRating = maxStars,
                starSize = 15.dp,               // Standard Dimension for the test
                modifier = Modifier.testTag("starIcon")     // Necessary unique tag
            )
        }

        composeTestRule
            .onAllNodesWithContentDescription("Star rating icon")
            .assertCountEquals(maxStars)

    }

}