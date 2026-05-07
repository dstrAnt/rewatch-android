package it.polimi.rewatch.unitTests.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.LoginNavPage
import it.polimi.rewatch.MediaNavPage
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.scrollToBottomTab
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

class PosterItemTesting {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPosterItem(){
        val media = MovieId(0)
        var flag = mutableStateOf(false)

        val content = composeTestRule.setContent { PosterItem(PosterInfo(media), actions = NavigationActions(onNavigateToPerson = { id -> },

            onNavigateToMedia = { id ->  id.parentId + 1},
            onNavigateBack = {

            },
            onFullscreenImage = { url ->

            },
            onBottomSheet = { info ->  },
            onNavigateToLogin = {

            },
            onBackToHome = { tab ->

            }),
            modifier = Modifier.testTag("posterItem") )}

        val posterItem = composeTestRule.onNodeWithTag("posterItem")

        posterItem.performTouchInput {
            click()
        }

        posterItem.assertExists()

        assert(media.parentId == 1)

        posterItem.performTouchInput {
            longClick()
        }

        assert(flag.value)
    }
}