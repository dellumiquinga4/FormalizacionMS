package com.banquito.formalizacion.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banquito.formalizacion.enums.ContratoCreditoEstado;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContratoCreditoDTO {

    private Long idContratoCredito;
    private Long idSolicitud;
    private String numeroContrato;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaFirma;
    private BigDecimal montoAprobado;
    private Long plazoFinalMeses;
    private BigDecimal tasaEfectivaAnual;
    private String rutaArchivoFirmado;
    private ContratoCreditoEstado estado;
    private Long version;

}
