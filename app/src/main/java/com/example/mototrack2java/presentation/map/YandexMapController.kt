package com.example.mototrack2java.presentation.map

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import com.example.mototrack2java.R
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.model.TripSettings
import com.yandex.mapkit.GeoObjectCollection
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.traffic.TrafficLayer
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

class YandexMapController(
    private val context: Context
) : Session.SearchListener {

    private var mapView: MapView? = null
    private var motoObjects: MapObjectCollection? = null
    private var routeObjects: MapObjectCollection? = null
    private var searchManager: SearchManager? = null
    private var drivingRouter: DrivingRouter? = null
    private var trafficLayer: TrafficLayer? = null
    private var userLocationLayer: UserLocationLayer? = null
    private var lastRouteKey: String? = null
    private var latestSettings = TripSettings()

    fun attach(mapView: MapView) {
        this.mapView = mapView
        val root = mapView.map.mapObjects
        motoObjects = root.addCollection()
        routeObjects = root.addCollection()
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        trafficLayer = com.yandex.mapkit.MapKitFactory.getInstance()
            .createTrafficLayer(mapView.mapWindow)
            .also { it.setTrafficVisible(false) }
        setupUserLocation(mapView)
    }

    fun render(settings: TripSettings, motos: List<Moto>) {
        latestSettings = settings
        val view = mapView ?: return
        val location = settings.location

        if (!location.isZero()) {
            view.map.move(CameraPosition(location.toPoint(), AppConfig.Map.DEFAULT_ZOOM, 0f, 0f))
        }

        trafficLayer?.setTrafficVisible(settings.isTripActive)
        motoObjects?.clear()
        motos.forEach { moto ->
            motoObjects?.addPlacemark(
                Point(moto.lat.toDouble(), moto.lon.toDouble()),
                ImageProvider.fromResource(context, R.drawable.motopng),
                IconStyle()
                    .setAnchor(PointF(0.1f, 0.1f))
                    .setRotationType(RotationType.NO_ROTATION)
                    .setZIndex(AppConfig.Map.MOTO_ICON_Z_INDEX)
                    .setScale(AppConfig.Map.MOTO_ICON_SCALE)
            )
        }

        val routeKey = listOf(
            settings.destination,
            location.lat,
            location.lon,
            settings.isTripActive
        ).joinToString(AppConfig.Map.ROUTE_KEY_SEPARATOR)
        if (settings.isTripActive && settings.destination.isNotBlank() && routeKey != lastRouteKey) {
            lastRouteKey = routeKey
            routeObjects?.clear()
            searchManager?.submit(
                settings.destination,
                VisibleRegionUtils.toPolygon(view.map.visibleRegion),
                SearchOptions(),
                this
            )
        }
        if (!settings.isTripActive) {
            lastRouteKey = null
            routeObjects?.clear()
        }
    }

    fun centerOnUser() {
        val location = latestSettings.location
        if (!location.isZero()) {
            mapView?.map?.move(CameraPosition(location.toPoint(), AppConfig.Map.DEFAULT_ZOOM, 0f, 0f))
        }
    }

    override fun onSearchResponse(response: Response) {
        val result = response.collection.children.firstNotNullOfOrNull(GeoObjectCollection.Item::getObj)
            ?.geometry
            ?.firstOrNull()
            ?.point
            ?: return

        val start = latestSettings.location
        if (start.isZero()) return

        val requestPoints = arrayListOf(
            RequestPoint(start.toPoint(), RequestPointType.WAYPOINT, null),
            RequestPoint(result, RequestPointType.WAYPOINT, null)
        )
        drivingRouter?.requestRoutes(
            requestPoints,
            DrivingOptions(),
            VehicleOptions(),
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                    routes.firstOrNull()?.let { route ->
                        routeObjects?.addPlacemark(
                            result,
                            ImageProvider.fromResource(context, R.drawable.search_result),
                            IconStyle()
                                .setAnchor(PointF(0.5f, 0.5f))
                                .setRotationType(RotationType.ROTATE)
                            .setZIndex(AppConfig.Map.DESTINATION_ICON_Z_INDEX)
                            .setScale(AppConfig.Map.DESTINATION_ICON_SCALE)
                        )
                        routeObjects?.addPolyline(route.geometry)
                    }
                }

                override fun onDrivingRoutesError(error: Error) = Unit
            }
        )
    }

    override fun onSearchError(error: Error) = Unit

    private fun setupUserLocation(view: MapView) {
        userLocationLayer = com.yandex.mapkit.MapKitFactory.getInstance()
            .createUserLocationLayer(view.mapWindow)
            .apply {
                isVisible = true
                isHeadingEnabled = true
                setObjectListener(object : UserLocationObjectListener {
                    override fun onObjectAdded(userLocationView: UserLocationView) {
                        setAnchor(
                            PointF(
                                view.width * AppConfig.Map.USER_LOCATION_ANCHOR_X_RATIO,
                                view.height * AppConfig.Map.USER_LOCATION_ANCHOR_Y_RATIO
                            ),
                            PointF(
                                view.width * AppConfig.Map.USER_LOCATION_ANCHOR_X_RATIO,
                                view.height * AppConfig.Map.USER_LOCATION_HEADING_Y_RATIO
                            )
                        )
                        userLocationView.arrow.setIcon(
                            ImageProvider.fromResource(context, R.drawable.user_arrow)
                        )
                        userLocationView.accuracyCircle.fillColor =
                            Color.BLUE and AppConfig.Map.USER_ACCURACY_COLOR
                    }

                    override fun onObjectRemoved(userLocationView: UserLocationView) = Unit
                    override fun onObjectUpdated(userLocationView: UserLocationView, event: com.yandex.mapkit.layers.ObjectEvent) = Unit
                })
            }
    }

    private fun AppLocation.toPoint(): Point = Point(lat.toDouble(), lon.toDouble())

    private fun AppLocation.isZero(): Boolean = lat == 0f && lon == 0f
}
