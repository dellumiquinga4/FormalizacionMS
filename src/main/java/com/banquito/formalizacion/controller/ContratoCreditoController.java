package com.banquito.formalizacion.controller;

import com.banquito.formalizacion.controller.dto.*;
import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.service.ContratoCreditoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/contratos-credito", produces = "application/json")
@Tag(name = "Contratos de Crédito", description = "API para gestionar Contratos de Crédito Automotriz y sus Pagarés")
@Validated
public class ContratoCreditoController {

    private static final Logger log = LoggerFactory.getLogger(ContratoCreditoController.class);
    private final ContratoCreditoService service;

    public ContratoCreditoController(ContratoCreditoService service) {
        this.service = service;
    }

    // === CONTRATO CREDITO ===

    @Operation(summary = "Obtiene un Contrato de Crédito por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato encontrado",
                     content = @Content(schema = @Schema(implementation = ContratoCreditoDTO.class))),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContratoCreditoDTO> getById(
        @Parameter(description = "ID del contrato", required = true)
        @PathVariable Long id) {
        log.debug("Solicitud recibida → Obtener ContratoCredito con ID={}", id);
        ContratoCreditoDTO dto = service.getContratoCreditoById(id);
        log.info("ContratoCredito ID={} recuperado correctamente.", id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Crea un nuevo Contrato de Crédito")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Contrato creado",
                     content = @Content(schema = @Schema(implementation = ContratoCreditoDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de contrato")
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ContratoCreditoDTO> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload para crear el Contrato de Crédito",
            required = true,
            content = @Content(schema = @Schema(implementation = ContratoCreditoCreateDTO.class))
        )
        @Valid @RequestBody ContratoCreditoCreateDTO createDto) {
        log.debug("Solicitud recibida → Crear ContratoCredito para solicitud={}", createDto.getIdSolicitud());
        ContratoCreditoDTO created = service.createContratoCredito(createDto);
        log.info("ContratoCredito creado correctamente con ID={}", created.getIdContratoCredito());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualiza un Contrato de Crédito existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato actualizado",
                     content = @Content(schema = @Schema(implementation = ContratoCreditoDTO.class))),
        @ApiResponse(responseCode = "400", description = "ID path/body no coinciden o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<ContratoCreditoDTO> update(
        @Parameter(description = "ID del contrato a actualizar", required = true)
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload para actualizar el Contrato de Crédito",
            required = true,
            content = @Content(schema = @Schema(implementation = ContratoCreditoUpdateDTO.class))
        )
        @Valid @RequestBody ContratoCreditoUpdateDTO updateDto) {
        log.debug("Solicitud recibida → Actualizar ContratoCredito ID={}", id);
        ContratoCreditoDTO updated = service.updateContratoCredito(id, updateDto);
        log.info("ContratoCredito ID={} actualizado correctamente.", id);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Elimina lógicamente un Contrato de Crédito (marca como CANCELADO)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato cancelado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ContratoCreditoDTO> logicalDelete(
        @Parameter(description = "ID del contrato a eliminar", required = true)
        @PathVariable Long id) {
        log.debug("Solicitud recibida → Eliminación lógica de ContratoCredito ID={}", id);
        ContratoCreditoDTO deleted = service.logicalDeleteContratoCredito(id);
        log.warn("ContratoCredito ID={} marcado como CANCELADO.", id);
        return ResponseEntity.ok(deleted);
    }

    @Operation(summary = "Lista contratos con filtros y paginación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de contratos")
    })
    @GetMapping
    public ResponseEntity<Page<ContratoCreditoDTO>> listWithFilters(
        @Parameter(description = "Estado del contrato") @RequestParam(required = false) ContratoCreditoEstado estado,
        @Parameter(description = "Número de contrato core (búsqueda parcial)") @RequestParam(required = false) String numeroContrato,
        @Parameter(description = "ID de solicitud") @RequestParam(required = false) Long idSolicitud,
        @Parameter(description = "Página", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ContratoCreditoDTO> result = service.findContratosConFiltros(estado, numeroContrato, idSolicitud, pageable);
        log.info("Consulta contratos: encontrados {} resultados.", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Verifica si existe un contrato para una solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Indicador de existencia")
    })
    @GetMapping("/existe/solicitud/{idSolicitud}")
    public ResponseEntity<Boolean> existsBySolicitud(
        @Parameter(description = "ID de la solicitud", required = true)
        @PathVariable Long idSolicitud) {
        log.debug("Verificando existencia de contrato para solicitud {}", idSolicitud);
        boolean existe = service.existePorSolicitud(idSolicitud);
        log.info("Existencia de contrato para solicitud {}: {}", idSolicitud, existe);
        return ResponseEntity.ok(existe);
    }

    // === PAGARE (Integrados) ===

    @GetMapping("/pagares/{id}")
    @Operation(summary = "Obtener pagaré por ID", description = "Obtiene un pagaré específico por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagaré encontrado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagareDTO.class))),
        @ApiResponse(responseCode = "404", description = "Pagaré no encontrado")
    })
    public ResponseEntity<PagareDTO> getPagareById(@PathVariable Long id) {
        log.info("Solicitando pagaré por ID: {}", id);
        PagareDTO pagare = service.getPagareById(id);
        return ResponseEntity.ok(pagare);
    }

    @GetMapping("/pagares/contrato/{idContratoCredito}")
    @Operation(summary = "Obtener todos los pagarés de un contrato", description = "Obtiene la lista ordenada de pagarés de un contrato de crédito")
    public ResponseEntity<List<PagareDTO>> getPagaresByContrato(
            @PathVariable Long idContratoCredito) {
        log.info("Listando pagarés de contrato de crédito ID: {}", idContratoCredito);
        List<PagareDTO> pagares = service.getPagaresByContratoCredito(idContratoCredito);
        return ResponseEntity.ok(pagares);
    }

    @GetMapping("/pagares/contrato/{idContratoCredito}/cuota/{numeroCuota}")
    @Operation(summary = "Obtener un pagaré de un contrato por número de cuota", description = "Obtiene el pagaré de un contrato para una cuota específica")
    public ResponseEntity<PagareDTO> getPagareByContratoAndCuota(
            @PathVariable Long idContratoCredito,
            @PathVariable Long numeroCuota) {
        log.info("Buscando pagaré por contrato {} y cuota {}", idContratoCredito, numeroCuota);
        PagareDTO pagare = service.getPagareByContratoAndCuota(idContratoCredito, numeroCuota);
        return ResponseEntity.ok(pagare);
    }

    @PostMapping("/pagares")
    @Operation(summary = "Crear un nuevo pagaré manual", description = "Permite crear un pagaré de forma manual (casos excepcionales)")
    public ResponseEntity<PagareDTO> createPagare(@Valid @RequestBody PagareCreateDTO dto) {
        log.info("Creando pagaré manual para contrato {}", dto.getIdContratoCredito());
        PagareDTO pagare = service.createPagare(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagare);
    }

    @PutMapping("/pagares/{id}")
    @Operation(summary = "Actualizar pagaré", description = "Actualiza los datos de un pagaré existente")
    public ResponseEntity<PagareDTO> updatePagare(
            @PathVariable Long id,
            @Valid @RequestBody PagareUpdateDTO dto) {
        log.info("Actualizando pagaré ID: {}", id);
        PagareDTO pagareActualizado = service.updatePagare(id, dto);
        return ResponseEntity.ok(pagareActualizado);
    }

    @PostMapping("/pagares/generar")
    @Operation(summary = "Generar cronograma completo de pagarés", description = "Genera N pagarés automáticos para un contrato, uno por cada mes")
    public ResponseEntity<List<PagareDTO>> generarPagares(
            @Parameter(description = "ID del contrato de crédito") @RequestParam Long idContratoCredito) {
        List<PagareDTO> pagares = service.generarPagaresDesdeContrato(idContratoCredito);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagares);
    }


    @GetMapping("/pagares/contrato/{idContratoCredito}/existen")
    @Operation(summary = "Verificar si existen pagarés para un contrato")
    public ResponseEntity<Boolean> existenPagaresPorContrato(@PathVariable Long idContratoCredito) {
        log.info("Verificando existencia de pagarés para contrato {}", idContratoCredito);
        boolean existen = service.existenPagaresPorContrato(idContratoCredito);
        return ResponseEntity.ok(existen);
    }
}
