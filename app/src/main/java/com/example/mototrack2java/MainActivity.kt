package com.example.mototrack2java

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.presentation.MainScreen
import com.example.mototrack2java.presentation.MainUiState
import com.example.mototrack2java.presentation.MainViewModel
import com.example.mototrack2java.presentation.map.YandexMapController
import com.example.mototrack2java.ui.theme.MotoTrackTheme
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        initializeYandexMapKit()
        super.onCreate(savedInstanceState)

        setContent {
            MotoTrackTheme {
                val state by viewModel.uiState.collectAsState()
                PermissionRequester(onPermissionResult = viewModel::refreshLocationFromPermissionResult)
                MotoTrackApp(
                    state = state,
                    onDestinationChanged = viewModel::onDestinationChanged,
                    onNavigationAction = viewModel::onNavigationAction,
                    onModeChanged = viewModel::onModeChanged,
                    onVoiceChanged = viewModel::onVoiceChanged,
                    onNotificationChanged = viewModel::onNotificationChanged,
                    onStartStopTripClicked = viewModel::onStartStopTripClicked,
                    onErrorShown = viewModel::clearError
                )
            }
        }
    }

    private fun initializeYandexMapKit() {
        if (isYandexMapKitInitialized) return

        MapKitFactory.setApiKey(getString(R.string.yandex_api))
        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)
        isYandexMapKitInitialized = true
    }

    companion object {
        private var isYandexMapKitInitialized = false
    }
}

@Composable
private fun PermissionRequester(onPermissionResult: () -> Unit) {
    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { onPermissionResult() }
    )
    LaunchedEffect(Unit) {
        launcher.launch(permissions.toTypedArray())
    }
}

@Composable
fun MotoTrackApp(
    state: MainUiState,
    onDestinationChanged: (String) -> Unit,
    onNavigationAction: () -> Unit,
    onModeChanged: (TripMode) -> Unit,
    onVoiceChanged: (Boolean) -> Unit,
    onNotificationChanged: (Boolean) -> Unit,
    onStartStopTripClicked: () -> Unit,
    onErrorShown: () -> Unit,
    mapContent: @Composable (MainUiState) -> Unit = { MotoTrackMapScreen(state = it) }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.errorMessageRes?.let { stringResource(it) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    val icon = if (state.currentScreen == MainScreen.MAP) {
                        Icons.Default.Settings
                    } else {
                        Icons.Default.ArrowBack
                    }
                    val description = if (state.currentScreen == MainScreen.MAP) {
                        stringResource(R.string.settings_content_description)
                    } else {
                        stringResource(R.string.back_content_description)
                    }
                    IconButton(
                        onClick = onNavigationAction,
                        modifier = Modifier.testTag(AppConfig.UiTestTags.NAVIGATION_ACTION)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = description
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.settings.destination,
                onValueChange = onDestinationChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag(AppConfig.UiTestTags.DESTINATION_FIELD),
                singleLine = true,
                label = { Text(stringResource(R.string.destination_label)) }
            )

            when (state.currentScreen) {
                MainScreen.MAP -> mapContent(state)
                MainScreen.SETTINGS -> SettingsScreen(
                    state = state,
                    onModeChanged = onModeChanged,
                    onVoiceChanged = onVoiceChanged,
                    onNotificationChanged = onNotificationChanged,
                    onStartStopTripClicked = onStartStopTripClicked
                )
            }
        }
    }
}

@Composable
private fun MotoTrackMapScreen(state: MainUiState) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { YandexMapController(context.applicationContext) }
    val mapView = remember {
        MapView(context).also { controller.attach(it) }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                    controller.centerOnUser()
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onStop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            update = {
                controller.render(state.settings, state.motos)
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag(AppConfig.UiTestTags.MAP_VIEW)
        )
        MotoTrackMapOverlay(
            state = state,
            onCenterOnUser = controller::centerOnUser,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun MotoTrackMapOverlay(
    state: MainUiState,
    onCenterOnUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(elevation = 6.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Moped, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        R.string.nearby_motos_count,
                        state.settings.nearbyMotoCount
                    ),
                    modifier = Modifier.testTag(AppConfig.UiTestTags.NEARBY_MOTOS_COUNT),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        IconButton(
            onClick = onCenterOnUser,
            modifier = Modifier.testTag(AppConfig.UiTestTags.MY_LOCATION_BUTTON)
        ) {
            Icon(
                Icons.Default.GpsFixed,
                contentDescription = stringResource(R.string.my_location_content_description)
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    state: MainUiState,
    onModeChanged: (TripMode) -> Unit,
    onVoiceChanged: (Boolean) -> Unit,
    onNotificationChanged: (Boolean) -> Unit,
    onStartStopTripClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.status_title),
            modifier = Modifier.testTag(AppConfig.UiTestTags.STATUS_TITLE),
            style = MaterialTheme.typography.h6
        )
        TripModeSelector(selected = state.settings.mode, onModeChanged = onModeChanged)

        SettingSwitchRow(
            title = stringResource(R.string.voice_warning_title),
            enabled = state.settings.voiceEnabled,
            enabledIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null) },
            disabledIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) },
            switchTestTag = AppConfig.UiTestTags.VOICE_SWITCH,
            onChanged = onVoiceChanged
        )
        SettingSwitchRow(
            title = stringResource(R.string.notification_setting_title),
            enabled = state.settings.notificationEnabled,
            enabledIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            disabledIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) },
            switchTestTag = AppConfig.UiTestTags.NOTIFICATION_SWITCH,
            onChanged = onNotificationChanged
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStartStopTripClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(AppConfig.UiTestTags.START_TRIP_BUTTON)
        ) {
            Text(stringResource(state.startButtonTextRes))
        }
    }
}

@Composable
private fun TripModeSelector(
    selected: TripMode,
    onModeChanged: (TripMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TripMode.values().forEach { mode ->
            val icon = if (mode == TripMode.CAR) Icons.Default.DirectionsCar else Icons.Default.Moped
            val label = stringResource(
                if (mode == TripMode.CAR) R.string.mode_car else R.string.mode_moto
            )
            val testTag = if (mode == TripMode.CAR) {
                AppConfig.UiTestTags.MODE_CAR
            } else {
                AppConfig.UiTestTags.MODE_MOTO
            }
            val radioTestTag = if (mode == TripMode.CAR) {
                AppConfig.UiTestTags.MODE_CAR_RADIO
            } else {
                AppConfig.UiTestTags.MODE_MOTO_RADIO
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected == mode,
                        onClick = { onModeChanged(mode) }
                    )
                    .padding(vertical = 8.dp)
                    .testTag(testTag),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == mode,
                    onClick = { onModeChanged(mode) },
                    modifier = Modifier.testTag(radioTestTag)
                )
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(label, style = MaterialTheme.typography.body1)
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    enabled: Boolean,
    enabledIcon: @Composable () -> Unit,
    disabledIcon: @Composable () -> Unit,
    switchTestTag: String,
    onChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (enabled) enabledIcon() else disabledIcon()
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = onChanged,
            modifier = Modifier.testTag(switchTestTag)
        )
    }
}
