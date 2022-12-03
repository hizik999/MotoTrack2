package com.example.mototrack2java.rest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mototrack2java.MainActivity;
import com.example.mototrack2java.R;
import com.example.mototrack2java.database.DataBaseHelper;
import com.example.mototrack2java.domain.Moto;
import com.example.mototrack2java.domain.mapper.MotoMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MotoApiVolley implements MotoApiInterface {

    //localhost
    public static final String BASE_URL = "http://192.168.1.111:2023/";

    public static final String API_TEST = "API_TEST_VOLLEY";
    private Context context;
    private final Response.ErrorListener errorListener;

    private DataBaseHelper dataBaseHelper;
    private RequestQueue requestQueue;

    public MotoApiVolley(Context context) {
        this.context = context;

        errorListener = error -> {
            Log.d(API_TEST, error.toString());
            Toast.makeText(context.getApplicationContext(), ((MainActivity) context).getString(R.string.no_connection), 5).show();
        };
    }


    @Override
    public void fillMoto() {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        String url = BASE_URL + "moto";
        dataBaseHelper = new DataBaseHelper(context);
        JsonArrayRequest arrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    dataBaseHelper.cleanTableMoto();
                    try {
                        int c = 0;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            Moto moto = MotoMapper.motoFromJson(jsonObject);

                            if (calculateDistance(
                                    moto.getLat(),
                                    moto.getLon(),
                                    ((MainActivity) context).loadDataFloat("lat"),
                                    ((MainActivity) context).loadDataFloat("lon")
                            ) < 1000) {
                                c++;
                                boolean success = dataBaseHelper.addOne(moto);
                                Log.d("API_TEST_VOLLEY_FILL", String.valueOf(success));

                                if (((MainActivity) context).loadDataInt("moto_count") < c && ((MainActivity) context).loadDataBoolean("voiceOn")) {
                                    ((MainActivity) context).playSoundStart();
                                }


                            }

                        }
                        ((MainActivity) context).saveDataInt("moto_count", c);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                errorListener
        );
        List<Moto> list = new ArrayList<>();
        list = dataBaseHelper.getAllMoto();
        requestQueue.add(arrayRequest);
        Log.d("DB_MOTO", list.toString());
        dataBaseHelper.close();
    }

    @Override
    public void addMoto() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        String url = BASE_URL + "moto";

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    fillMoto();
                    try {
                        JSONObject respObj = new JSONObject(response);
                        long id = respObj.getLong("id");
                        ((MainActivity) context).saveDataLong("id", id);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.w("MOTO", response);
                },
                errorListener
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {

                JSONObject jsonObject = new JSONObject();
                String body = null;
                try {
                    Log.d("LATITUDE", String.valueOf(((MainActivity) context).loadDataFloat("lat")));
                    Log.d("LONGITUDE", String.valueOf(((MainActivity) context).loadDataFloat("lon")));
                    float lat = ((MainActivity) context).loadDataFloat("lat");
                    float lon = ((MainActivity) context).loadDataFloat("lon");
                    jsonObject.put("lat", lat);
                    jsonObject.put("lon", lon);
                    body = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    return body.getBytes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        };

        requestQueue.add(stringRequest);
    }


    @Override
    public void updateMoto(long id, float lat, float lon) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        String url = BASE_URL + "moto/" + id;
        StringRequest stringRequest = new StringRequest(
                Request.Method.PUT,
                url,
                response -> {
                    Log.d("UPDATE_MOTO", "success");
                },
                errorListener
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("lat", String.valueOf(lat));
                params.put("lon", String.valueOf(lon));

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    @Override
    public void deleteMoto(long id) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }

        String url = BASE_URL + "moto/" + id;

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    ((MainActivity) context).saveDataLong("id", -1);
                },
                errorListener
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private double calculateDistance(double Alat, double Alon, double Blat, double Blon) {

        final long EARTH_RADIUS = 6372795;
        double lat1 = Alat * Math.PI / 180;
        double lat2 = Blat * Math.PI / 180;
        double lon1 = Alon * Math.PI / 180;
        double lon2 = Blon * Math.PI / 180;

        double cl1 = Math.cos(lat1);
        double cl2 = Math.cos(lat2);
        double sl1 = Math.sin(lat1);
        double sl2 = Math.sin(lat2);

        double delta = lon1 - lon2;

        double cdelta = Math.cos(delta);
        double sdelta = Math.sin(delta);

        double y = Math.sqrt(Math.pow(cl2 * sdelta, 2) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
        double x = sl1 * sl2 + cl1 * cl2 * cdelta;
        double ad = Math.atan2(y, x);

        return ad * EARTH_RADIUS;
    }
}
