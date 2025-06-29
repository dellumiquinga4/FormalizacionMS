package com.banquito.formalizacion.enums;

public enum ContratoCreditoEstado {
    PENDIENTE_FIRMA("pendiente_firma"),
    ACTIVO("activo"),
    PAGADO("pagado"),
    CANCELADO("cancelado");

    private final String valor;

    ContratoCreditoEstado(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
} 