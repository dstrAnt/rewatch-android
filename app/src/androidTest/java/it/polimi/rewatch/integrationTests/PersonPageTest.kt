package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.person.PersonPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonPageTest {
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
            PersonPage(actions = actions, outerPadding = outerPadding, personId = 0)
        }
    }

    @Test
    fun testBio() {

    }

    @Test
    fun testBirthInfo() {

    }

    @Test
    fun testNameAndJob() {

    }

    @Test
    fun testProfilePicture() {

    }
}