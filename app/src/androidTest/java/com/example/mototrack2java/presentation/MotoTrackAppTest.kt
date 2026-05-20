package com.example.mototrack2java.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.mototrack2java.R
import com.example.mototrack2java.MotoTrackApp
import com.example.mototrack2java.MotoTrackMapOverlay
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.ui.theme.MotoTrackTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MotoTrackAppTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsScreenShowsTripControls() {
        setAppContent(state = MainUiState(currentScreen = MainScreen.SETTINGS))

        composeRule.onNodeWithTag(AppConfig.UiTestTags.STATUS_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_CAR).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.VOICE_SWITCH).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.NOTIFICATION_SWITCH).assertIsDisplayed()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.START_TRIP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun settingsScreenInvokesModeAndStartCallbacks() {
        var selectedMode: TripMode? = null
        var startClicked = false

        setAppContent(
            state = MainUiState(currentScreen = MainScreen.SETTINGS),
            onModeChanged = { selectedMode = it },
            onStartStopTripClicked = { startClicked = true }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO).performClick()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.START_TRIP_BUTTON).performClick()

        assertEquals(TripMode.MOTO, selectedMode)
        assertTrue(startClicked)
    }

    @Test
    fun destinationFieldInvokesTextChangeCallback() {
        var destination = ""

        setAppContent(
            state = MainUiState(currentScreen = MainScreen.SETTINGS),
            onDestinationChanged = { destination = it }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.DESTINATION_FIELD)
            .performTextInput("Moscow")

        assertEquals("Moscow", destination)
    }

    @Test
    fun activeTripShowsCancelButtonText() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        setAppContent(
            state = MainUiState(
                currentScreen = MainScreen.SETTINGS,
                settings = TripSettings(isTripActive = true),
                startButtonTextRes = R.string.cancel_trip
            )
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.START_TRIP_BUTTON)
            .assertTextEquals(context.getString(R.string.cancel_trip))
    }

    @Test
    fun navigationActionInvokesCallback() {
        var clicked = false

        setAppContent(
            state = MainUiState(currentScreen = MainScreen.SETTINGS),
            onNavigationAction = { clicked = true }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.NAVIGATION_ACTION).performClick()

        assertTrue(clicked)
    }

    @Test
    fun mapScreenNavigationActionInvokesCallbackWithoutRealMap() {
        var clicked = false

        setAppContent(
            state = MainUiState(currentScreen = MainScreen.MAP),
            onNavigationAction = { clicked = true },
            mapContent = { Text("Map placeholder") }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.NAVIGATION_ACTION).performClick()

        assertTrue(clicked)
    }

    @Test
    fun modeRadioButtonsExposeSelectedStateAndInvokeCallbacks() {
        var selectedMode: TripMode? = null

        setAppContent(
            state = MainUiState(
                currentScreen = MainScreen.SETTINGS,
                settings = TripSettings(mode = TripMode.CAR)
            ),
            onModeChanged = { selectedMode = it }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_CAR).assertIsSelected()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO).assertIsNotSelected()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_CAR_RADIO).assertIsSelected()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO_RADIO).assertIsNotSelected()

        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO_RADIO).performClick()

        assertEquals(TripMode.MOTO, selectedMode)
    }

    @Test
    fun carModeRowInvokesCallbackWhenMotoIsCurrentlySelected() {
        var selectedMode: TripMode? = null

        setAppContent(
            state = MainUiState(
                currentScreen = MainScreen.SETTINGS,
                settings = TripSettings(mode = TripMode.MOTO)
            ),
            onModeChanged = { selectedMode = it }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_MOTO).assertIsSelected()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MODE_CAR).performClick()

        assertEquals(TripMode.CAR, selectedMode)
    }

    @Test
    fun enabledSwitchesShowOnStateAndPassFalseWhenClicked() {
        var voiceEnabled = true
        var notificationsEnabled = true

        setAppContent(
            state = MainUiState(
                currentScreen = MainScreen.SETTINGS,
                settings = TripSettings(voiceEnabled = true, notificationEnabled = true)
            ),
            onVoiceChanged = { voiceEnabled = it },
            onNotificationChanged = { notificationsEnabled = it }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.VOICE_SWITCH).assertIsOn()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.NOTIFICATION_SWITCH).assertIsOn()

        composeRule.onNodeWithTag(AppConfig.UiTestTags.VOICE_SWITCH).performClick()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.NOTIFICATION_SWITCH).performClick()

        assertFalse(voiceEnabled)
        assertFalse(notificationsEnabled)
    }

    @Test
    fun disabledSwitchesShowOffStateAndPassTrueWhenClicked() {
        var voiceEnabled = false
        var notificationsEnabled = false

        setAppContent(
            state = MainUiState(
                currentScreen = MainScreen.SETTINGS,
                settings = TripSettings(voiceEnabled = false, notificationEnabled = false)
            ),
            onVoiceChanged = { voiceEnabled = it },
            onNotificationChanged = { notificationsEnabled = it }
        )

        composeRule.onNodeWithTag(AppConfig.UiTestTags.VOICE_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.NOTIFICATION_SWITCH).assertIsOff()

        composeRule.onNodeWithTag(AppConfig.UiTestTags.VOICE_SWITCH).performClick()
        composeRule.onNodeWithTag(AppConfig.UiTestTags.NOTIFICATION_SWITCH).performClick()

        assertTrue(voiceEnabled)
        assertTrue(notificationsEnabled)
    }

    @Test
    fun mapOverlayShowsNearbyMotoCountAndInvokesLocationButton() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        var centered = false

        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackMapOverlay(
                    state = MainUiState(settings = TripSettings(nearbyMotoCount = 3)),
                    onCenterOnUser = { centered = true }
                )
            }
        }

        composeRule.onNodeWithTag(AppConfig.UiTestTags.NEARBY_MOTOS_COUNT)
            .assertTextEquals(context.getString(R.string.nearby_motos_count, 3))
        composeRule.onNodeWithTag(AppConfig.UiTestTags.MY_LOCATION_BUTTON).performClick()

        assertTrue(centered)
    }

    @Test
    fun errorSnackbarIsShownAndCallbackIsInvoked() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        var errorShown = false

        setAppContent(
            state = MainUiState(
                currentScreen = MainScreen.SETTINGS,
                errorMessageRes = R.string.no_connection_message
            ),
            onErrorShown = { errorShown = true }
        )

        composeRule.onNodeWithText(context.getString(R.string.no_connection_message))
            .assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) { errorShown }
        assertTrue(errorShown)
    }

    private fun setAppContent(
        state: MainUiState,
        onDestinationChanged: (String) -> Unit = {},
        onNavigationAction: () -> Unit = {},
        onModeChanged: (TripMode) -> Unit = {},
        onVoiceChanged: (Boolean) -> Unit = {},
        onNotificationChanged: (Boolean) -> Unit = {},
        onStartStopTripClicked: () -> Unit = {},
        onErrorShown: () -> Unit = {},
        mapContent: @Composable (MainUiState) -> Unit = { Text("Map placeholder") }
    ) {
        composeRule.setContent {
            MotoTrackTheme {
                MotoTrackApp(
                    state = state,
                    onDestinationChanged = onDestinationChanged,
                    onNavigationAction = onNavigationAction,
                    onModeChanged = onModeChanged,
                    onVoiceChanged = onVoiceChanged,
                    onNotificationChanged = onNotificationChanged,
                    onStartStopTripClicked = onStartStopTripClicked,
                    onErrorShown = onErrorShown,
                    mapContent = mapContent
                )
            }
        }
    }
}
