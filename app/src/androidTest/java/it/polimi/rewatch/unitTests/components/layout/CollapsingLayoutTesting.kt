package it.polimi.rewatch.unitTests.components.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import it.polimi.rewatch.components.layout.CollapsingLayout
import androidx.compose.ui.unit.*
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test

class CollapsingLayoutTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCollapsingLayout() {
        composeTestRule.setContent {
            CollapsingLayout(
                10.dp, 10.dp, Modifier.testTag("collapsingLayout"),
                collapsingTopBar = {
                    Row() {}
                        /*Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            //  .padding(top = 16.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Logo()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ReWatch",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }*/
                },
                pinnedBottomBar = {},
                content = { _, _ -> Row() {}
                    /*Row(
                        Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            //  .padding(top = 16.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Logo()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ReWatch",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }*/
                }
            )
        }

        val node = composeTestRule.onNodeWithTag("collapsingLayout").assertExists()

        //node.performScrollTo()

        // composeTestRule.onNodeWithText("Rewatch").assertExists().assertIsDisplayed()
    }
}