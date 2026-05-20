package com.example.mototrack2java.core.config

import com.example.mototrack2java.BuildConfig

object AppConfig {
    object Network {
        val baseUrl: String = BuildConfig.MOTO_API_BASE_URL.trimEnd('/')
        const val MOTO_ENDPOINT = "moto"
        const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    }

    object JsonFields {
        const val ID = "id"
        const val LAT = "lat"
        const val LON = "lon"
    }

    object Database {
        const val NAME = "mototrack2.db"
        const val SCHEMA = 2
        const val TABLE_MOTO = "moto"
        const val COLUMN_ID = "id"
        const val COLUMN_LAT = "lat"
        const val COLUMN_LON = "lon"
    }

    object Preferences {
        const val NAME = "mototrack_trip"
        const val KEY_DESTINATION = "destination"
        const val KEY_TRIP_ACTIVE = "trip_active"
        const val KEY_MODE = "mode"
        const val KEY_VOICE_ENABLED = "voice_enabled"
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_MOTO_ID = "moto_id"
        const val KEY_MOTO_COUNT = "moto_count"
        const val KEY_LAT = "lat"
        const val KEY_LON = "lon"
    }

    object Notifications {
        const val CHANNEL_ID = "mototrack_trip"
        const val CHANNEL_NAME = "MotoTrack"
        const val TRIP_NOTIFICATION_ID = 1
        const val REQUEST_CODE_MAIN_ACTIVITY = 0
    }

    object Trip {
        const val SYNC_INTERVAL_MS = 2_000L
        const val NEARBY_RADIUS_METERS = 1_000
    }

    object Geo {
        const val EARTH_RADIUS_METERS = 6_372_795L
    }

    object Map {
        const val DEFAULT_ZOOM = 14f
        const val MOTO_ICON_SCALE = 0.5f
        const val DESTINATION_ICON_SCALE = 0.5f
        const val MOTO_ICON_Z_INDEX = 0.5f
        const val DESTINATION_ICON_Z_INDEX = 1f
        const val USER_LOCATION_ANCHOR_X_RATIO = 0.5f
        const val USER_LOCATION_ANCHOR_Y_RATIO = 0.5f
        const val USER_LOCATION_HEADING_Y_RATIO = 0.83f
        const val USER_ACCURACY_COLOR = 0x99ffffff.toInt()
        const val ROUTE_KEY_SEPARATOR = "|"
    }

    object UiTestTags {
        const val NAVIGATION_ACTION = "navigation_action"
        const val DESTINATION_FIELD = "destination_field"
        const val MAP_VIEW = "map_view"
        const val NEARBY_MOTOS_COUNT = "nearby_motos_count"
        const val MY_LOCATION_BUTTON = "my_location_button"
        const val STATUS_TITLE = "status_title"
        const val START_TRIP_BUTTON = "start_trip_button"
        const val MODE_CAR = "mode_car"
        const val MODE_MOTO = "mode_moto"
        const val MODE_CAR_RADIO = "mode_car_radio"
        const val MODE_MOTO_RADIO = "mode_moto_radio"
        const val VOICE_SWITCH = "voice_switch"
        const val NOTIFICATION_SWITCH = "notification_switch"
    }
}
