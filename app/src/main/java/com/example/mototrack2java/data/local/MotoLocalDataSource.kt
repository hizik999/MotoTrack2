package com.example.mototrack2java.data.local

import com.example.mototrack2java.domain.model.Moto

interface MotoLocalDataSource {
    fun replaceAll(motos: List<Moto>)
    fun getAll(): List<Moto>
    fun clear()
}
