package com.example.mototrack2java.rest;

import com.example.mototrack2java.domain.Moto;

public interface MotoApiInterface {

    void fillMoto();

    void addMoto();

    void updateMoto(
            long id,
            float lat,
            float lon
    );

    void deleteMoto(long id);
}
