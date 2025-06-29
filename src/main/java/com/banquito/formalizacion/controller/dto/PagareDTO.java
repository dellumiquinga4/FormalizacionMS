package com.banquito.formalizacion.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.banquito.formalizacion.enums.PagareEstado;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PagareDTO {

    private Integer idPagare;

    @NotNull(message = "El ID de contrato de crédito es requerido")
    private Integer idContratoCredito;

    @NotNull(message = "El número de cuota es requerido")
    @Min(value = 1, message = "El número de cuota debe ser mínimo 1")
    private Integer numeroCuota;

    @NotNull(message = "El monto de la cuota es requerido")
    @DecimalMin(value = "0.01", message = "El monto de la cuota debe ser mayor a 0")
    @DecimalMax(value = "9999999999.99", message = "El monto de la cuota excede el límite permitido")
    private BigDecimal montoCuota;

    @NotNull(message = "La fecha de vencimiento es requerida")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El estado es requerido")
    private PagareEstado estado;

    @NotNull(message = "La versión es requerida")
    private Long version;
} 