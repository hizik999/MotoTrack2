package com.example.mototrack2java.database;

import com.example.mototrack2java.domain.Moto;

import java.util.List;

public interface DataBaseHelperInterface {

    boolean addOne(Moto moto);

    List<Moto> getAllMoto();

    void dropTableMoto();

    void cleanTableMoto();
}
