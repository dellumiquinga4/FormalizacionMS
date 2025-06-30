package com.banquito.formalizacion.controller.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.banquito.formalizacion.enums.ContratoVentaEstado;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoCompraVentaCreateDTO {

    @NotNull(message = "El ID de solicitud no puede ser nulo")
    private Long idSolicitud;

    @NotBlank(message = "El número de contrato no puede estar vacío")
    @Size(max = 50, message = "El número de contrato no debe exceder 50 caracteres")
    private String numeroContrato;

    @NotNull(message = "La fecha de generación no puede ser nula")
    private LocalDateTime fechaGeneracion;

    private LocalDateTime fechaFirma;

    @NotNull(message = "El precio final del vehículo no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio final del vehículo debe ser mayor a 0")
    @DecimalMax(value = "999999999999.99", message = "El precio final del vehículo excede el límite permitido")
    private BigDecimal precioFinalVehiculo;

    @Size(max = 255, message = "La ruta del archivo no debe exceder 255 caracteres")
    private String rutaArchivoFirmado;

    @NotNull(message = "El estado del contrato no puede ser nulo")
    private ContratoVentaEstado estado;
}
