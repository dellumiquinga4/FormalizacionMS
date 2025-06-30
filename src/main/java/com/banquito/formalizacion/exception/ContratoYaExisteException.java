package com.banquito.formalizacion.exception;

public class ContratoYaExisteException extends RuntimeException {

    private final Long idSolicitud;
    private final String tipoContrato;

    public ContratoYaExisteException(Long idSolicitud, String tipoContrato) {
        super();
        this.idSolicitud = idSolicitud;
        this.tipoContrato = tipoContrato;
    }

    @Override
    public String getMessage() {
        return String.format("Ya existe un %s para la solicitud ID: %d", tipoContrato, idSolicitud);
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }
} 