package com.example.mototrack2java.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.mototrack2java.R
import com.example.mototrack2java.MotoTrackApp
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.ui.theme.MotoTrackTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MotoTrackAppTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsScreenShowsTripControls() {
        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackApp(
                    state = MainUiState(currentScreen = MainScreen.SETTINGS),
                    onDestinationChanged = {},
                    onNavigationAction = {},
                    onModeChanged = {},
                    onVoiceChanged = {},
                    onNotificationChanged = {},
                    onStartStopTripClicked = {},
                    onErrorShown = {}
                )
            }
        }

        composeRule.onNodeWithTag(AppConfig.UiTestTags.STATUS_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_CAR).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.START_TRIP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun settingsScreenInvokesModeAndStartCallbacks() {
        var selectedMode: TripMode? = null
        var startClicked = false

        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackApp(
                    state = MainUiState(currentScreen = MainScreen.SETTINGS),
                    onDestinationChanged = {},
                    onNavigationAction = {},
                    onModeChanged = { selectedMode = it },
                    onVoiceChanged = {},
                    onNotificationChanged = {},
                    onStartStopTripClicked = { startClicked = true },
                    onErrorShown = {}
                )
            }
        }

        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO).performClick()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.START_TRIP_BUTTON).performClick()

        assertEquals(TripMode.MOTO, selectedMode)
        assertTrue(startClicked)
    }

    @Test
    fun destinationFieldInvokesTextChangeCallback() {
        var destination = ""

        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackApp(
                    state = MainUiState(currentScreen = MainScreen.SETTINGS),
                    onDestinationChanged = { destination = it },
                    onNavigationAction = {},
                    onModeChanged = {},
                    onVoiceChanged = {},
                    onNotificationChanged = {},
                    onStartStopTripClicked = {},
                    onErrorShown = {}
                )
            }
        }

        composeRule.onNodeWithTag(AppConfig.UiTestTags.DESTINATION_FIELD)
            .performTextInput("Moscow")

        assertEquals("Moscow", destination)
    }

    @Test
    fun activeTripShowsCancelButtonText() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackApp(
                    state = MainUiState(
                        currentScreen = MainScreen.SETTINGS,
                        settings = TripSettings(isTripActive = true),
                        startButtonTextRes = R.string.cancel_trip
                    ),
                    onDestinationChanged = {},
                    onNavigationAction = {},
                    onModeChanged = {},
                    onVoiceChanged = {},
                    onNotificationChanged = {},
                    onStartStopTripClicked = {},
                    onErrorShown = {}
                )
            }
        }

        composeRule.onNodeWithTag(AppConfig.UiTestTags.START_TRIP_BUTTON)
            .assertTextEquals(context.getString(R.string.cancel_trip))
    }

    @Test
    fun navigationActionInvokesCallback() {
        var clicked = false

        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackApp(
                    state = MainUiState(currentScreen = MainScreen.SETTINGS),
                    onDestinationChanged = {},
                    onNavigationAction = { clicked = true },
                    onModeChanged = {},
                    onVoiceChanged = {},
                    onNotificationChanged = {},
                    onStartStopTripClicked = {},
                    onErrorShown = {}
                )
            }
        }

        composeRule.onNodeWithTag(AppConfig.UiTestTags.NAVIGATION_ACTION).performClick()

        assertTrue(clicked)
    }
}
