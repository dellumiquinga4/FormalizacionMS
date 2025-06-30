package com.banquito.formalizacion.controller.dto;

import com.banquito.formalizacion.enums.PagareEstado;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagareCreateDTO {

    @NotNull(message = "El ID del contrato de crédito no puede ser nulo")
    private Long idContratoCredito;

    @NotNull(message = "El número de cuota no puede ser nulo")
    @Min(value = 1, message = "El número de cuota debe ser al menos 1")
    private Long numeroCuota;

    @NotNull(message = "El monto de la cuota es obligatorio")
    @Positive(message = "El monto de la cuota debe ser positivo")
    private BigDecimal montoCuota;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El estado del pagaré es obligatorio")
    private PagareEstado estado;

}
