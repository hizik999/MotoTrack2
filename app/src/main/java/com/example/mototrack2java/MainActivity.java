package com.example.mototrack2java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mototrack2java.database.DataBaseHelper;
import com.example.mototrack2java.domain.Moto;
import com.example.mototrack2java.fragment.SettingsFragment;
import com.example.mototrack2java.fragment.YandexMapFragment;
import com.example.mototrack2java.rest.MotoApiVolley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fm = getSupportFragmentManager();
    private Fragment fragment_map;
    private Fragment fragment_settings;
    private Fragment current_fragment;

    private AppCompatImageButton btn_findLocation;

    private AppCompatEditText et_FindLocation;

    private SharedPreferences sharedPreferences;

    private MediaPlayer sound;

    private boolean threadStatus;

    private DataBaseHelper dataBaseHelper;

    private Context context;

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 100;

    private static final String CHANNEL_NAME = "mototrack";
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        builder = new NotificationCompat.Builder(this, "123");
        createNotificationChannel();


        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("mototrack2.db", MODE_PRIVATE, null);
        new DataBaseHelper(this).onCreate(db);
        threadStatus = true;

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        setNavigation();
        et_FindLocation = findViewById(R.id.et_FindLocation);
        et_FindLocation.setHint("Введите адрес");

        et_FindLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveDataString("loc", String.valueOf(et_FindLocation.getText()));
            }
        });
        et_FindLocation.setText(loadDataString("loc"));
        Thread thread = new MyTread();
        thread.setDaemon(true);
        thread.start();
    }

    void setNavigation() {
        fragment_map = new YandexMapFragment();
        fragment_settings = new SettingsFragment();
        current_fragment = fragment_map;
        try {
            loadFragment(current_fragment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_findLocation = findViewById(R.id.btn_findLocation);
        btn_findLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ///////////////
                //      TEST

                //new MotoApiVolley(MainActivity.this).addMoto();

                ///////////////

                if (current_fragment == fragment_map) {
                    current_fragment = fragment_settings;
                    btn_findLocation.setImageResource(R.drawable.ic_baseline_arrow_back_24);
                    loadFragment(current_fragment);
                } else if (current_fragment == fragment_settings) {
                    current_fragment = fragment_map;
                    btn_findLocation.setImageResource(R.drawable.ic_round_arrow_forward_24);
                    loadFragment(current_fragment);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadStatus = false;

    }

    public void loadMapFragment(){
        current_fragment = fragment_map;
        btn_findLocation.setImageResource(R.drawable.ic_round_arrow_forward_24);
        fm.beginTransaction().replace(R.id.frame_layout, current_fragment).addToBackStack(null).commit();
    }

    public void loadFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.frame_layout, fragment).addToBackStack(null).commit();
    }


    public void saveDataBoolean(String key, boolean b) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, b);
        editor.apply();
    }

    public boolean loadDataBoolean(String key) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public void saveDataInt(String key, int value) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void saveDataLong(String key, long value) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public int loadDataInt(String key) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getInt(key, -1);
    }

    public long loadDataLong(String key) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getLong(key, -1);
    }

    public void saveDataFloat(String key, float value) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public float loadDataFloat(String key) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getFloat(key, 0.0F);
    }

    public void saveDataString(String key, String value) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String loadDataString(String key) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }


    private void updateUIValues(Location location) {
        try {
            saveDataFloat("lat", (float) location.getLatitude());
            saveDataFloat("lon", (float) location.getLongitude());
            Log.d("LAT", String.valueOf(loadDataFloat("lat")));
            Log.d("LON", String.valueOf(loadDataFloat("lon")));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(MainActivity.this, "no GPS!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void updateGPS() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, MainActivity.this::updateUIValues);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }


    private class MyTread extends Thread implements Runnable {

        private MotoApiVolley motoApiVolley;

        @Override
        public void run() {

            while (threadStatus) {
                try {
                    updateGPS();
                    if (loadDataBoolean("status")) {
                        switch (loadDataInt("car_status")) {
                            case 0:
                                fillMoto();
                                break;
                            case 1:
                                updateMoto();
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("THREAD", "running");
                try {
                    sleep(2 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.gc();
            }
        }

        private void updateMoto() {
            if (motoApiVolley == null) {
                Log.w("MOTO_API_VOLLEY", "create");
                motoApiVolley = new MotoApiVolley(MainActivity.this);
            }
            try {
                motoApiVolley.updateMoto(loadDataLong("id"), loadDataFloat("lat"), loadDataFloat("lon"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void fillMoto() {
            if (motoApiVolley == null) {
                Log.w("MOTO_API_VOLLEY", "create");
                motoApiVolley = new MotoApiVolley(MainActivity.this);
            }
            try {
                motoApiVolley.fillMoto();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void cancelTripEditText() {
        saveDataString("loc", "");
        et_FindLocation.setText("");
    }

    public void playSoundStart() {
        sound = MediaPlayer.create(this, R.raw.start);
        if (sound.isPlaying()) {
            sound.stop();
        }
        sound.setLooping(false);
        sound.start();
    }

    public void sendNotificationStatus(){

        Intent intentMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pIntentMainActivity = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pIntentMainActivity = PendingIntent.getActivity(this, 0, intentMainActivity, PendingIntent.FLAG_MUTABLE);
        }

        String title = "";
        String text = "";
        switch (loadDataInt("car_status")){
            case 0:
                title = getString(R.string.notification_title_car);
                text = getString(R.string.notification_desc_car);
                break;
            case 1:
                title = getString(R.string.notification_title_moto);
                text = getString(R.string.notification_desc_moto);
                break;
        }
        builder.setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getText(R.string.notification_big_text)))
                .setOngoing(true)
                .setContentIntent(pIntentMainActivity);

        notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
        notificationManagerCompat.notify(1, builder.build());
    }

    public void cancelNotification(){
        try{
            notificationManagerCompat.cancel(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("123", CHANNEL_NAME, importance);
            channel.setDescription("description");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}