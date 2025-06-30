package com.banquito.formalizacion.controller;

import com.banquito.formalizacion.controller.dto.PagareDTO;
import com.banquito.formalizacion.controller.dto.PagareCreateDTO;
import com.banquito.formalizacion.controller.dto.PagareUpdateDTO;
import com.banquito.formalizacion.service.PagareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pagares")
@Tag(name = "Pagarés", description = "API para gestión del cronograma de pagarés")
@Slf4j
public class PagareController {

    private final PagareService pagareService;

    public PagareController(PagareService pagareService) {
        this.pagareService = pagareService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pagaré por ID", description = "Obtiene un pagaré específico por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagaré encontrado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagareDTO.class))),
        @ApiResponse(responseCode = "404", description = "Pagaré no encontrado")
    })
    public ResponseEntity<PagareDTO> getPagareById(@PathVariable Long id) {
        log.info("Solicitando pagaré por ID: {}", id);
        PagareDTO pagare = pagareService.getPagareById(id);
        return ResponseEntity.ok(pagare);
    }

    @GetMapping("/contrato/{idContratoCredito}")
    @Operation(summary = "Obtener todos los pagarés de un contrato", description = "Obtiene la lista ordenada de pagarés de un contrato de crédito")
    public ResponseEntity<List<PagareDTO>> getPagaresByContrato(
            @PathVariable Long idContratoCredito) {
        log.info("Listando pagarés de contrato de crédito ID: {}", idContratoCredito);
        List<PagareDTO> pagares = pagareService.getPagaresByContratoCredito(idContratoCredito);
        return ResponseEntity.ok(pagares);
    }

    @GetMapping("/contrato/{idContratoCredito}/cuota/{numeroCuota}")
    @Operation(summary = "Obtener un pagaré de un contrato por número de cuota", description = "Obtiene el pagaré de un contrato para una cuota específica")
    public ResponseEntity<PagareDTO> getPagareByContratoAndCuota(
            @PathVariable Long idContratoCredito,
            @PathVariable Long numeroCuota) {
        log.info("Buscando pagaré por contrato {} y cuota {}", idContratoCredito, numeroCuota);
        PagareDTO pagare = pagareService.getPagareByContratoAndCuota(idContratoCredito, numeroCuota);
        return ResponseEntity.ok(pagare);
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo pagaré manual", description = "Permite crear un pagaré de forma manual (casos excepcionales)")
    public ResponseEntity<PagareDTO> createPagare(@Valid @RequestBody PagareCreateDTO dto) {
        log.info("Creando pagaré manual para contrato {}", dto.getIdContratoCredito());
        PagareDTO pagare = pagareService.createPagare(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagare);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pagaré", description = "Actualiza los datos de un pagaré existente")
    public ResponseEntity<PagareDTO> updatePagare(
            @PathVariable Long id,
            @Valid @RequestBody PagareUpdateDTO dto) {
        log.info("Actualizando pagaré ID: {}", id);
        PagareDTO pagareActualizado = pagareService.updatePagare(id, dto);
        return ResponseEntity.ok(pagareActualizado);
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar cronograma completo de pagarés", description = "Genera N pagarés automáticos para un contrato, uno por cada mes")
    public ResponseEntity<List<PagareDTO>> generarPagares(
            @Parameter(description = "ID del contrato de crédito") @RequestParam Long idContratoCredito,
            @Parameter(description = "Monto solicitado") @RequestParam BigDecimal montoSolicitado,
            @Parameter(description = "Tasa anual (%)") @RequestParam BigDecimal tasaAnual,
            @Parameter(description = "Plazo en meses") @RequestParam int plazoMeses,
            @Parameter(description = "Fecha inicial") @RequestParam LocalDate fechaInicio) {
        log.info("Generando {} pagarés para contrato {} (monto={}, tasa={}, inicio={})",
                plazoMeses, idContratoCredito, montoSolicitado, tasaAnual, fechaInicio);
        List<PagareDTO> pagares = pagareService.generarPagaresDesdeParams(
                idContratoCredito, montoSolicitado, tasaAnual, plazoMeses, fechaInicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagares);
    }

    @GetMapping("/contrato/{idContratoCredito}/existen")
    @Operation(summary = "Verificar si existen pagarés para un contrato")
    public ResponseEntity<Boolean> existenPagaresPorContrato(@PathVariable Long idContratoCredito) {
        log.info("Verificando existencia de pagarés para contrato {}", idContratoCredito);
        boolean existen = pagareService.existenPagaresPorContrato(idContratoCredito);
        return ResponseEntity.ok(existen);
    }
}
