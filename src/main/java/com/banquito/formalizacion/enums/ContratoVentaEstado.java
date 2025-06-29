package com.banquito.formalizacion.enums;

public enum ContratoVentaEstado {
    PENDIENTE_FIRMA("pendiente_firma"),
    FIRMADO("firmado");

    private final String valor;

    ContratoVentaEstado(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
} 