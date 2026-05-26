package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.settings.AuthPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {

        val actions = NavigationActions(onNavigateToPerson = { id -> },

            onNavigateToMedia = { id -> },
            onNavigateBack = {

            },
            onFullscreenImage = { url ->

            },
            onBottomSheet = { info ->  },
            onNavigateToLogin = {

            },
            onBackToHome = { tab ->

            })

        val outerPadding = PaddingValues();

        composeTestRule.setContent {
            AuthPage(actions = actions, outerPadding = outerPadding)
        }
    }

    @Test
    fun testIdleContent() {

    }

    @Test
    fun testSuccessContent() {

    }

    @Test
    fun testLinkSentContent() {

    }

    @Test
    fun testMergePromptContent() {

    }
    @Test
    fun testLoadingContent() {

    }

    @Test
    fun testLogIn() {

    }

}