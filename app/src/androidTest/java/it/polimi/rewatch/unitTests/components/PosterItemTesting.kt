package it.polimi.rewatch.unitTests.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.LoginNavPage
import it.polimi.rewatch.MediaNavPage
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
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
        var mediaId = 0
        var flag = false

        composeTestRule.setContent {
            mediaId = rememberSaveable { media.parentId }
            //flag = rememberSaveable{ false }

            PosterItem(PosterInfo(media),
                actions = NavigationActions(
                    onNavigateToPerson = { id -> },
                    onNavigateToMedia = { id ->  mediaId = id.parentId + 1 },

                    onNavigateBack = {},
                    onFullscreenImage = { url -> },
                    onBottomSheet = { info ->  flag = (info.id as MediaId).type.equals(MediaType.MOVIE)},

                    onNavigateToLogin = {},
                    onBackToHome = { tab -> }),
                modifier = Modifier.testTag("posterItem")
            )
        }

        val posterItem = composeTestRule.onNodeWithTag("posterItem").assertExists()

        posterItem.performTouchInput {
            click()
        }

        posterItem.assertExists()

        assert(mediaId == 1)

        posterItem.performTouchInput {
            longClick()
        }

        assert(flag)
    }
}