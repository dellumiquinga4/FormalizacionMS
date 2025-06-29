package com.banquito.formalizacion.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banquito.formalizacion.enums.ContratoVentaEstado;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContratoCompraVentaDTO {

    private Integer idContratoVenta;

    @NotNull(message = "El ID de solicitud es requerido")
    private Integer idSolicitud;

    @NotBlank(message = "El número de contrato es requerido")
    @Size(max = 50, message = "El número de contrato no debe exceder 50 caracteres")
    private String numeroContrato;

    @NotNull(message = "La fecha de generación es requerida")
    private LocalDateTime fechaGeneracion;

    private LocalDateTime fechaFirma;

    @NotNull(message = "El precio final del vehículo es requerido")
    @DecimalMin(value = "0.01", message = "El precio final del vehículo debe ser mayor a 0")
    @DecimalMax(value = "999999999999.99", message = "El precio final del vehículo excede el límite permitido")
    private BigDecimal precioFinalVehiculo;

    @Size(max = 255, message = "La ruta del archivo no debe exceder 255 caracteres")
    private String rutaArchivoFirmado;

    @NotNull(message = "El estado es requerido")
    private ContratoVentaEstado estado;

    @NotNull(message = "La versión es requerida")
    private Long version;
} 