package com.banquito.formalizacion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaCreateDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaUpdateDTO;
import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.service.ContratoCompraVentaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/contratos-compra-venta", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Contratos de Compra Venta", description = "API para gestionar contratos de compra-venta de vehículos")
@Validated
public class ContratoCompraVentaController {

    private static final Logger log = LoggerFactory.getLogger(ContratoCompraVentaController.class);
    private final ContratoCompraVentaService service;

    public ContratoCompraVentaController(ContratoCompraVentaService service) {
        this.service = service;
    }

    @Operation(summary = "Obtiene un contrato de compra-venta por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato encontrado",
                     content = @Content(schema = @Schema(implementation = ContratoCompraVentaDTO.class))),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContratoCompraVentaDTO> getById(
        @Parameter(description = "ID del contrato", required = true)
        @PathVariable Long id) {

        log.debug("Solicitud recibida → Obtener contrato con ID={}", id);
        ContratoCompraVentaDTO dto = service.findById(id);
        log.info("Contrato con ID={} recuperado correctamente", id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Lista todos los contratos de compra-venta por estado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de contratos",
                     content = @Content(schema = @Schema(implementation = ContratoCompraVentaDTO.class))),
        @ApiResponse(responseCode = "404", description = "No existen contratos para ese estado")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ContratoCompraVentaDTO>> getByEstado(
        @Parameter(description = "Estado del contrato", required = true)
        @PathVariable ContratoVentaEstado estado) {

        log.debug("ENTER GET /api/contratos-compra-venta/estado/{} → listar contratos", estado);
        List<ContratoCompraVentaDTO> dtos = service.listarContratosPorEstado(estado);
        if (dtos.isEmpty()) {
            log.warn("No se encontraron contratos para el estado {}", estado);
            return ResponseEntity.notFound().build();
        }
        log.info("{} contratos listados para el estado {}", dtos.size(), estado);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Genera un nuevo contrato de compra-venta")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Contrato creado",
                     content = @Content(schema = @Schema(implementation = ContratoCompraVentaDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContratoCompraVentaDTO> create(
        @Parameter(description = "Payload para crear el contrato", required = true)
        @Valid @RequestBody ContratoCompraVentaCreateDTO createDto) {

        log.debug("Solicitud recibida → Crear contrato de compra-venta para solicitud {}", createDto.getIdSolicitud());
        ContratoCompraVentaDTO created = service.generarContratoVenta(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualiza un contrato de compra-venta existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato actualizado",
                     content = @Content(schema = @Schema(implementation = ContratoCompraVentaDTO.class))),
        @ApiResponse(responseCode = "400", description = "ID path/body no coinciden o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContratoCompraVentaDTO> update(
        @Parameter(description = "ID del contrato a actualizar", required = true)
        @PathVariable Long id,
        @Parameter(description = "Payload para actualizar el contrato", required = true)
        @Valid @RequestBody ContratoCompraVentaUpdateDTO updateDto) {

        log.debug("Solicitud recibida → Actualizar contrato ID={} con estado='{}'", id, updateDto.getEstado());
        ContratoCompraVentaDTO updated = service.actualizarContrato(id, updateDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Verifica si existe contrato para una solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Existencia de contrato"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    @GetMapping("/existe-solicitud/{idSolicitud}")
    public ResponseEntity<Boolean> existsBySolicitud(
        @Parameter(description = "ID de la solicitud", required = true)
        @PathVariable Long idSolicitud) {

        log.debug("ENTER GET /api/contratos-compra-venta/existe-solicitud/{} → verificar existencia de contrato", idSolicitud);
        boolean existe = service.existePorSolicitud(idSolicitud);
        log.info("Existencia de contrato para solicitud {}: {}", idSolicitud, existe);
        return ResponseEntity.ok(existe);
    }

}
