package com.banquito.formalizacion.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.formalizacion.controller.dto.ContratoCreditoDTO;
import com.banquito.formalizacion.controller.dto.PageResponseDTO;
import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.exception.ContratoNoFirmadoException;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.exception.PagaresPendientesException;
import com.banquito.formalizacion.service.ContratoCreditoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/contratos-credito")
@Validated
@Tag(name = "Contratos de Crédito", description = "Gestión de contratos de crédito automotriz")
public class ContratoCreditoController {

    private final ContratoCreditoService service;

    public ContratoCreditoController(ContratoCreditoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener contratos con paginación y filtros",
               description = "Obtiene contratos de crédito con soporte para paginación, ordenamiento y filtros múltiples")
    public ResponseEntity<PageResponseDTO<ContratoCreditoDTO>> getContratos(
            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento")
            @RequestParam(defaultValue = "idContratoCredito") String sortBy,
            @Parameter(description = "Dirección de ordenamiento")
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filtro por estado")
            @RequestParam(required = false) ContratoCreditoEstado estado,
            @Parameter(description = "Filtro por número de contrato core (búsqueda parcial)")
            @RequestParam(required = false) String numeroContratoCore,
            @Parameter(description = "Filtro por ID de solicitud")
            @RequestParam(required = false) Integer idSolicitud) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ContratoCreditoDTO> contratos = service.findContratosConFiltros(
            estado, numeroContratoCore, idSolicitud, pageable);

        PageResponseDTO<ContratoCreditoDTO> response = new PageResponseDTO<>(
            contratos.getContent(),
            contratos.getNumber(),
            contratos.getSize(),
            contratos.getTotalElements(),
            contratos.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los contratos sin filtros - USAR CON PRECAUCIÓN",
               description = "Endpoint legacy - usar con precaución en producción")
    public ResponseEntity<PageResponseDTO<ContratoCreditoDTO>> getAllContratos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ContratoCreditoDTO> contratos = service.findAll(pageable);

        PageResponseDTO<ContratoCreditoDTO> response = new PageResponseDTO<>(
            contratos.getContent(),
            contratos.getNumber(),
            contratos.getSize(),
            contratos.getTotalElements(),
            contratos.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contrato por ID")
    public ResponseEntity<ContratoCreditoDTO> getContratoById(@PathVariable Integer id) {
        ContratoCreditoDTO contrato = service.findById(id);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/solicitud/{idSolicitud}")
    @Operation(summary = "Obtener contrato por ID de solicitud")
    public ResponseEntity<ContratoCreditoDTO> getContratoBySolicitud(@PathVariable Integer idSolicitud) {
        ContratoCreditoDTO contrato = service.findByIdSolicitud(idSolicitud);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/numero-core/{numeroContratoCore}")
    @Operation(summary = "Obtener contrato por número core")
    public ResponseEntity<ContratoCreditoDTO> getContratoByNumeroCore(@PathVariable String numeroContratoCore) {
        ContratoCreditoDTO contrato = service.findByNumeroContratoCore(numeroContratoCore);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener contratos por estado con paginación")
    public ResponseEntity<PageResponseDTO<ContratoCreditoDTO>> getContratosByEstado(
            @PathVariable ContratoCreditoEstado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idContratoCredito") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratoCreditoDTO> contratos = service.findByEstado(estado, pageable);

        PageResponseDTO<ContratoCreditoDTO> response = new PageResponseDTO<>(
            contratos.getContent(),
            contratos.getNumber(),
            contratos.getSize(),
            contratos.getTotalElements(),
            contratos.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/desembolso")
    @Operation(summary = "Obtener contratos pendientes de desembolso")
    public ResponseEntity<PageResponseDTO<ContratoCreditoDTO>> getContratosParaDesembolso(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ContratoCreditoDTO> contratos = service.obtenerContratosParaDesembolso(pageable);

        PageResponseDTO<ContratoCreditoDTO> response = new PageResponseDTO<>(
            contratos.getContent(),
            contratos.getNumber(),
            contratos.getSize(),
            contratos.getTotalElements(),
            contratos.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener contratos activos")
    public ResponseEntity<PageResponseDTO<ContratoCreditoDTO>> getContratosActivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ContratoCreditoDTO> contratos = service.obtenerContratosActivos(pageable);

        PageResponseDTO<ContratoCreditoDTO> response = new PageResponseDTO<>(
            contratos.getContent(),
            contratos.getNumber(),
            contratos.getSize(),
            contratos.getTotalElements(),
            contratos.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Instrumentar nuevo crédito")
    public ResponseEntity<ContratoCreditoDTO> instrumentarCredito(@Valid @RequestBody ContratoCreditoDTO contratoDto) {
        ContratoCreditoDTO contratoGenerado = service.instrumentarCredito(contratoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoGenerado);
    }

    @PutMapping("/{id}/firmar")
    @Operation(summary = "Registrar firma de contrato")
    public ResponseEntity<ContratoCreditoDTO> firmarContrato(
            @PathVariable Integer id,
            @RequestParam String rutaArchivoFirmado) {
        ContratoCreditoDTO contratoFirmado = service.registrarFirmaContrato(id, rutaArchivoFirmado);
        return ResponseEntity.ok(contratoFirmado);
    }

    @PutMapping("/{id}/aprobar-desembolso")
    @Operation(summary = "Aprobar desembolso del crédito")
    public ResponseEntity<ContratoCreditoDTO> aprobarDesembolso(@PathVariable Integer id) {
        ContratoCreditoDTO contratoAprobado = service.aprobarDesembolso(id);
        return ResponseEntity.ok(contratoAprobado);
    }

    @PutMapping("/{id}/marcar-pagado")
    @Operation(summary = "Marcar contrato como pagado")
    public ResponseEntity<ContratoCreditoDTO> marcarComoPagado(@PathVariable Integer id) {
        ContratoCreditoDTO contratoPagado = service.marcarComoPagado(id);
        return ResponseEntity.ok(contratoPagado);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar contrato de crédito")
    public ResponseEntity<ContratoCreditoDTO> cancelarContrato(
            @PathVariable Integer id,
            @RequestParam String motivo) {
        ContratoCreditoDTO contratoCancelado = service.cancelarContrato(id, motivo);
        return ResponseEntity.ok(contratoCancelado);
    }

    @GetMapping("/existe-solicitud/{idSolicitud}")
    @Operation(summary = "Verificar si existe contrato para solicitud")
    public ResponseEntity<Boolean> existeContratoPorSolicitud(@PathVariable Integer idSolicitud) {
        boolean existe = service.existePorSolicitud(idSolicitud);
        return ResponseEntity.ok(existe);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> manejarNoEncontrado(NotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({ContratoYaExisteException.class, NumeroContratoYaExisteException.class})
    public ResponseEntity<String> manejarErrorLogicaNegocio(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler({InvalidStateException.class, ContratoNoFirmadoException.class, PagaresPendientesException.class})
    public ResponseEntity<String> manejarEstadoInvalido(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
} 