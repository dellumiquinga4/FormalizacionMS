package com.banquito.formalizacion.controller.dto;

import com.banquito.formalizacion.enums.PagareEstado;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PagareDTO{

    private Long idPagare;
    private Long idContratoCredito;
    private Long numeroCuota;
    private BigDecimal montoCuota;
    private LocalDate fechaVencimiento;
    private PagareEstado estado;
    private Long version;

}
