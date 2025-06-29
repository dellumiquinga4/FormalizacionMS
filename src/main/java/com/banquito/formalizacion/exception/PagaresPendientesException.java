package com.banquito.formalizacion.exception;

public class PagaresPendientesException extends RuntimeException {

    private final Integer idContrato;
    private final long cantidadPendientes;

    public PagaresPendientesException(Integer idContrato, long cantidadPendientes) {
        super();
        this.idContrato = idContrato;
        this.cantidadPendientes = cantidadPendientes;
    }

    @Override
    public String getMessage() {
        return String.format("No se puede cerrar el contrato ID: %d, existen %d pagarés pendientes de pago", 
                           idContrato, cantidadPendientes);
    }

    public Integer getIdContrato() {
        return idContrato;
    }

    public long getCantidadPendientes() {
        return cantidadPendientes;
    }
} 