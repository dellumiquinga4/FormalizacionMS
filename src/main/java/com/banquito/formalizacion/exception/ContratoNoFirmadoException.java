package com.banquito.formalizacion.exception;

public class ContratoNoFirmadoException extends RuntimeException {

    private final Integer idContrato;
    private final String tipoContrato;

    public ContratoNoFirmadoException(Integer idContrato, String tipoContrato) {
        super();
        this.idContrato = idContrato;
        this.tipoContrato = tipoContrato;
    }

    @Override
    public String getMessage() {
        return String.format("No se puede procesar %s ID: %d porque no está firmado", tipoContrato, idContrato);
    }

    public Integer getIdContrato() {
        return idContrato;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }
} 