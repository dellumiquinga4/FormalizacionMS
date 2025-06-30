package com.banquito.formalizacion.exception;

public class ContratoCreditoGenerationException extends RuntimeException {

    private final Long idContratoCredito;
    private final String detalle;

    // Constructor con ID de contrato y mensaje detallado
    public ContratoCreditoGenerationException(Long idContratoCredito, String detalle) {
        super();
        this.idContratoCredito = idContratoCredito;
        this.detalle = detalle;
    }

    // Constructor solo con mensaje detallado (opcional, para otros usos)
    public ContratoCreditoGenerationException(String detalle) {
        super();
        this.idContratoCredito = null;
        this.detalle = detalle;
    }

    @Override
    public String getMessage() {
        if (idContratoCredito != null) {
            return String.format("Error con el contrato de crédito ID: %d - %s", idContratoCredito, detalle);
        } else {
            return String.format("Error en la operación de contrato de crédito: %s", detalle);
        }
    }

    public Long getIdContratoCredito() {
        return idContratoCredito;
    }

    public String getDetalle() {
        return detalle;
    }
}
