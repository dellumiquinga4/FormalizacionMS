package com.banquito.formalizacion.exception;

public class NumeroContratoYaExisteException extends RuntimeException {

    private final String numeroContrato;
    private final String tipoContrato;

    public NumeroContratoYaExisteException(String numeroContrato, String tipoContrato) {
        super();
        this.numeroContrato = numeroContrato;
        this.tipoContrato = tipoContrato;
    }

    @Override
    public String getMessage() {
        return String.format("Ya existe un %s con el número: %s", tipoContrato, numeroContrato);
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }
} 