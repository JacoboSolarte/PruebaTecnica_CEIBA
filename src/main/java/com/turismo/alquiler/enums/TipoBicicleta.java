package com.turismo.alquiler.enums;

public enum TipoBicicleta {
    URBANA(3500),
    MONTAÑA(5000),
    ELÉCTRICA(7500);

    private final int tarifaPorHora;

    TipoBicicleta(int tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public int getTarifaPorHora() {
        return tarifaPorHora;
    }
}
