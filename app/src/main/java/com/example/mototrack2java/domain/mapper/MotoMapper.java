package com.example.mototrack2java.domain.mapper;

import com.example.mototrack2java.domain.Moto;

import org.json.JSONException;
import org.json.JSONObject;

public class MotoMapper {

    public static Moto motoFromJson(JSONObject jsonObject){
        Moto moto = null;
        try {
            moto = new Moto(
                    jsonObject.getLong("id"),
                    (float) jsonObject.getDouble("lat"),
                    (float) jsonObject.getDouble("lon")
            );
        } catch (JSONException e){
            e.printStackTrace();
        }
        return moto;
    }
}
