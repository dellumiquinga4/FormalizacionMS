package com.banquito.formalizacion.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.formalizacion.controller.dto.PageResponseDTO;
import com.banquito.formalizacion.controller.dto.PagareDTO;
import com.banquito.formalizacion.enums.PagareEstado;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.service.PagareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/pagares")
@Validated
@Tag(name = "Pagarés", description = "Gestión de pagarés del cronograma de pagos")
public class PagareController {

    private final PagareService service;

    public PagareController(PagareService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener pagarés con paginación",
               description = "Obtiene pagarés con soporte para paginación y ordenamiento")
    public ResponseEntity<PageResponseDTO<PagareDTO>> getPagares(
            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento")
            @RequestParam(defaultValue = "fechaVencimiento") String sortBy,
            @Parameter(description = "Dirección de ordenamiento")
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PagareDTO> pagares = service.findAll(pageable);

        PageResponseDTO<PagareDTO> response = new PageResponseDTO<>(
            pagares.getContent(),
            pagares.getNumber(),
            pagares.getSize(),
            pagares.getTotalElements(),
            pagares.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pagaré por ID")
    public ResponseEntity<PagareDTO> getPagareById(@PathVariable Integer id) {
        PagareDTO pagare = service.findById(id);
        return ResponseEntity.ok(pagare);
    }

    @GetMapping("/contrato/{idContratoCredito}")
    @Operation(summary = "Obtener pagarés por contrato de crédito")
    public ResponseEntity<List<PagareDTO>> getPagaresByContrato(@PathVariable Integer idContratoCredito) {
        List<PagareDTO> pagares = service.findByContratoCredito(idContratoCredito);
        return ResponseEntity.ok(pagares);
    }

    @GetMapping("/contrato/{idContratoCredito}/paginated")
    @Operation(summary = "Obtener pagarés por contrato con paginación")
    public ResponseEntity<PageResponseDTO<PagareDTO>> getPagaresByContratoPaginated(
            @PathVariable Integer idContratoCredito,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PagareDTO> pagares = service.findByContratoCredito(idContratoCredito, pageable);

        PageResponseDTO<PagareDTO> response = new PageResponseDTO<>(
            pagares.getContent(),
            pagares.getNumber(),
            pagares.getSize(),
            pagares.getTotalElements(),
            pagares.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener pagarés por estado con paginación")
    public ResponseEntity<PageResponseDTO<PagareDTO>> getPagaresByEstado(
            @PathVariable PagareEstado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaVencimiento") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PagareDTO> pagares = service.findByEstado(estado, pageable);

        PageResponseDTO<PagareDTO> response = new PageResponseDTO<>(
            pagares.getContent(),
            pagares.getNumber(),
            pagares.getSize(),
            pagares.getTotalElements(),
            pagares.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vencidos")
    @Operation(summary = "Obtener pagarés vencidos")
    public ResponseEntity<List<PagareDTO>> getPagaresVencidos() {
        List<PagareDTO> pagares = service.findPagaresVencidos();
        return ResponseEntity.ok(pagares);
    }

    @GetMapping("/por-vencer")
    @Operation(summary = "Obtener pagarés próximos a vencer")
    public ResponseEntity<List<PagareDTO>> getPagaresPorVencer(
            @Parameter(description = "Días de anticipación")
            @RequestParam(defaultValue = "30") int diasAntes) {
        List<PagareDTO> pagares = service.findPagaresPorVencer(diasAntes);
        return ResponseEntity.ok(pagares);
    }

    @PutMapping("/{id}/registrar-pago")
    @Operation(summary = "Registrar pago de pagaré")
    public ResponseEntity<PagareDTO> registrarPago(@PathVariable Integer id) {
        PagareDTO pagarePagado = service.registrarPago(id);
        return ResponseEntity.ok(pagarePagado);
    }

    @PutMapping("/marcar-vencidos")
    @Operation(summary = "Marcar pagarés como vencidos")
    public ResponseEntity<List<PagareDTO>> marcarComoVencidos() {
        List<PagareDTO> pagaresVencidos = service.marcarComoVencidos();
        return ResponseEntity.ok(pagaresVencidos);
    }

    @GetMapping("/contrato/{idContratoCredito}/pendientes/count")
    @Operation(summary = "Contar pagarés pendientes por contrato")
    public ResponseEntity<Long> contarPagaresPendientes(@PathVariable Integer idContratoCredito) {
        long count = service.contarPagaresPendientesPorContrato(idContratoCredito);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/contrato/{idContratoCredito}/vencidos/count")
    @Operation(summary = "Contar pagarés vencidos por contrato")
    public ResponseEntity<Long> contarPagaresVencidos(@PathVariable Integer idContratoCredito) {
        long count = service.contarPagaresVencidosPorContrato(idContratoCredito);
        return ResponseEntity.ok(count);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> manejarNoEncontrado(NotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({InvalidStateException.class})
    public ResponseEntity<String> manejarEstadoInvalido(InvalidStateException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }
} 