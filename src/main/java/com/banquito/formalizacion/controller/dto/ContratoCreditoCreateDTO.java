package com.banquito.formalizacion.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banquito.formalizacion.enums.ContratoCreditoEstado;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoCreditoCreateDTO {

    @NotNull(message = "El ID de solicitud no puede ser nulo")
    private Long idSolicitud;

    @NotBlank(message = "El número de contrato core no puede estar vacío")
    @Size(max = 50, message = "El número de contrato core no debe exceder 50 caracteres")
    private String numeroContrato;

    @NotNull(message = "La fecha de generación es requerida")
    private LocalDateTime fechaGeneracion;

    private LocalDateTime fechaFirma;

    @NotNull(message = "El monto aprobado es requerido")
    @DecimalMin(value = "0.01", message = "El monto aprobado debe ser mayor a 0")
    @DecimalMax(value = "999999999999.99", message = "El monto aprobado excede el límite permitido")
    private BigDecimal montoAprobado;

    @NotNull(message = "El plazo final en meses es requerido")
    @Min(value = 1, message = "El plazo debe ser mínimo 1 mes")
    @Max(value = 120, message = "El plazo no puede exceder 120 meses")
    private Long plazoFinalMeses;

    @NotNull(message = "La tasa efectiva anual es requerida")
    @DecimalMin(value = "0.00", message = "La tasa efectiva anual no puede ser negativa")
    @DecimalMax(value = "99.99", message = "La tasa efectiva anual no puede exceder 99.99%")
    private BigDecimal tasaEfectivaAnual;

    @Size(max = 255, message = "La ruta del archivo no debe exceder 255 caracteres")
    private String rutaArchivoFirmado;

    @NotNull(message = "El estado es requerido")
    private ContratoCreditoEstado estado;

}
