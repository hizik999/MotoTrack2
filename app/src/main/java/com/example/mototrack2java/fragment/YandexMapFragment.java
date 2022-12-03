package com.example.mototrack2java.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.mototrack2java.MainActivity;
import com.example.mototrack2java.R;
import com.example.mototrack2java.database.DataBaseHelper;
import com.example.mototrack2java.domain.Moto;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.directions.navigation_layer.CameraListener;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.traffic.TrafficLayer;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YandexMapFragment extends Fragment implements Session.SearchListener, CameraListener {

    private MapObjectCollection mapObjects;
    private SearchManager searchManager;
    private boolean thread;
    private MyThread123 myThread;

    private UserLocationLayer userLocationLayer;
    private Context context;
    private DrivingRouter drivingRouter;
    private MapView mapView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        MapKitFactory.setApiKey(getString(R.string.yandex_api));
        MapKitFactory.initialize(requireContext());
        SearchFactory.initialize(requireContext());
        super.onCreate(savedInstanceState);
        context = getContext();
        thread = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_yandex_map, null);

        mapView = mainView.findViewById(R.id.mapView);
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        TrafficLayer traffic = MapKitFactory.getInstance().createTrafficLayer(mapView.getMapWindow());
        traffic.setTrafficVisible(false);
        double lat = ((MainActivity) context).loadDataFloat("lat");
        double lon = ((MainActivity) context).loadDataFloat("lon");
        mapView.getMap().move(new CameraPosition(
                new Point(lat, lon), 14, 0, 0));

        userLocation();

        AppCompatButton btnLocation = mainView.findViewById(R.id.btnLocation);

        btnLocation.setOnClickListener(view -> {
            double lat1 = ((MainActivity) context).loadDataFloat("lat");
            double lon1 = ((MainActivity) context).loadDataFloat("lon");
            mapView.getMap().move(new CameraPosition(
                    new Point(lat1, lon1), 14, 0, 0));
        });

        traffic.setTrafficVisible(false);

        if (((MainActivity) context).loadDataBoolean("status")) {
            traffic.setTrafficVisible(true);

            mapObjects.clear();
            String text = ((MainActivity) context).loadDataString("loc");
            submitQuery(text);

            if (((MainActivity) context).loadDataInt("car_status") == 0) {

                myThread = new MyThread123();
                thread = true;
                myThread.setDaemon(true);
                myThread.start();
            }


        }


        return mainView;
    }

    @Override
    public void onCameraModeChanged() {

    }

    private List<PlacemarkMapObject> printMotos() {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());

        List<Moto> list = dataBaseHelper.getAllMoto();

        List<PlacemarkMapObject> placemarkMapObjects = new ArrayList<>();

        for (Moto moto : list) {

            Point point = new Point(moto.getLat(), moto.getLon());

            try {
                PlacemarkMapObject placemarkMapObject = mapObjects.addPlacemark(point, ImageProvider.fromResource(requireContext(), R.drawable.motopng),
                        new IconStyle().setAnchor(new PointF(0.1f, 0.1f))
                                .setRotationType(RotationType.NO_ROTATION)
                                .setZIndex(0.5f)
                                .setScale(0.5f));
                placemarkMapObjects.add(placemarkMapObject);
            } catch (Exception e) {
                Log.d(getString(R.string.delete_placemark), e.getMessage());
            }

        }
        return placemarkMapObjects;
    }

    private void deleteMotos(List<PlacemarkMapObject> placemarkMapObjects) {

        for (PlacemarkMapObject marker : placemarkMapObjects) {
            marker.setVisible(false);
        }
    }

    private class MyThread123 extends Thread {
        private Handler handler;
        private List<PlacemarkMapObject> placemarkMapObjects123;

        @Override
        public void run() {
            handler = new Handler(Looper.getMainLooper());
            Runnable runnable = () -> placemarkMapObjects123 = printMotos();
            Runnable runnable1 = () -> deleteMotos(placemarkMapObjects123);
            while (thread) {
                if (((MainActivity) context).loadDataInt("car_status") == 0) {
                    try {
                        handler.post(runnable);
                        sleep(2 * 1000);
                        handler.post(runnable1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }


        }


    }

    private void submitQuery(String query) {
        searchManager.submit(query, VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()), new SearchOptions(), this);
    }


    @Override
    public void onSearchResponse(@NonNull Response response) {

        for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {
            Point resultLocation = Objects.requireNonNull(searchResult.getObj()).getGeometry().get(0).getPoint();

            if (resultLocation != null) {

                Point startLoc = new Point(((MainActivity) context).loadDataFloat("lat"), ((MainActivity) context).loadDataFloat("lon"));
                Point destLoc = new Point(resultLocation.getLatitude(), resultLocation.getLongitude());
                submitRequest(startLoc, destLoc);

            }
            break;
        }
    }

    @Override
    public void onSearchError(@NonNull Error error) {

    }

    private void userLocation() {
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);

        UserLocationObjectListener listener = new UserLocationObjectListener() {
            @Override
            public void onObjectAdded(@NonNull UserLocationView userLocationView) {

                userLocationLayer.setAnchor(
                        new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
                        new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));

                userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                        context, R.drawable.user_arrow));

                userLocationView.getAccuracyCircle().setFillColor(Color.BLUE & 0x99ffffff);
            }

            @Override
            public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
            }

            @Override
            public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
            }
        };

        userLocationLayer.setObjectListener(listener);
    }


    @Override
    public void onPause() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        thread = false;
        super.onPause();

        if (myThread != null) {
            Thread dummy = myThread;
            myThread = null;
            dummy.interrupt();
        }
    }


    @Override
    public void onStop() {
        mapView.onStop();
        thread = false;
        MapKitFactory.getInstance().onStop();

        super.onStop();

        if (myThread != null) {
            Thread dummy = myThread;
            myThread = null;
            dummy.interrupt();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        thread = true;
        MapKitFactory.getInstance().onStart();

        mapView.onStart();
    }

    private void submitRequest(Point tripStart, Point tripEnd) {

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                tripStart,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(
                tripEnd,
                RequestPointType.WAYPOINT,
                null));
        DrivingSession.DrivingRouteListener drivingRouteListener = new DrivingSession.DrivingRouteListener() {
            @Override
            public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
                for (DrivingRoute route : list) {
                    mapObjects.addPlacemark(tripEnd).setIcon(ImageProvider.fromResource(context, R.drawable.search_result),
                            new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                                    .setRotationType(RotationType.ROTATE)
                                    .setZIndex(1f)
                                    .setScale(0.5f));
                    mapObjects.addPolyline(route.getGeometry());
                    break;
                }
            }

            @Override
            public void onDrivingRoutesError(@NonNull Error error) {

            }
        };
        drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, drivingRouteListener);

    }
}
