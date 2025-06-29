package com.banquito.formalizacion.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.dto.PageResponseDTO;
import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.service.ContratoCompraVentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/contratos-compra-venta")
@Tag(name = "Contratos de Compra Venta", description = "Gestión de contratos de compra venta de vehículos")
public class ContratoCompraVentaController {

    private final ContratoCompraVentaService service;

    public ContratoCompraVentaController(ContratoCompraVentaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener contratos con paginación y filtros", 
               description = "Obtiene contratos de compra venta con soporte para paginación, ordenamiento y filtros múltiples")
    public ResponseEntity<PageResponseDTO<ContratoCompraVentaDTO>> getContratos(
            @Parameter(description = "Número de página (inicia en 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") 
            @RequestParam(defaultValue = "idContratoVenta") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") 
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filtro por estado") 
            @RequestParam(required = false) ContratoVentaEstado estado,
            @Parameter(description = "Filtro por número de contrato (búsqueda parcial)") 
            @RequestParam(required = false) String numeroContrato,
            @Parameter(description = "Filtro por ID de solicitud") 
            @RequestParam(required = false) Integer idSolicitud) {

        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContratoCompraVentaDTO> contratos = service.findContratosConFiltros(
            estado, numeroContrato, idSolicitud, pageable);
        
        PageResponseDTO<ContratoCompraVentaDTO> response = new PageResponseDTO<>(
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
    public ResponseEntity<PageResponseDTO<ContratoCompraVentaDTO>> getAllContratos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ContratoCompraVentaDTO> contratos = service.findAll(pageable);
        
        PageResponseDTO<ContratoCompraVentaDTO> response = new PageResponseDTO<>(
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
    public ResponseEntity<ContratoCompraVentaDTO> getContratoById(@PathVariable Integer id) {
        ContratoCompraVentaDTO contrato = service.findById(id);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/solicitud/{idSolicitud}")
    @Operation(summary = "Obtener contrato por ID de solicitud")
    public ResponseEntity<ContratoCompraVentaDTO> getContratoBySolicitud(@PathVariable Integer idSolicitud) {
        ContratoCompraVentaDTO contrato = service.findByIdSolicitud(idSolicitud);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/numero/{numeroContrato}")
    @Operation(summary = "Obtener contrato por número")
    public ResponseEntity<ContratoCompraVentaDTO> getContratoByNumero(@PathVariable String numeroContrato) {
        ContratoCompraVentaDTO contrato = service.findByNumeroContrato(numeroContrato);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener contratos por estado con paginación")
    public ResponseEntity<PageResponseDTO<ContratoCompraVentaDTO>> getContratosByEstado(
            @PathVariable ContratoVentaEstado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idContratoVenta") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratoCompraVentaDTO> contratos = service.findByEstado(estado, pageable);
        
        PageResponseDTO<ContratoCompraVentaDTO> response = new PageResponseDTO<>(
            contratos.getContent(),
            contratos.getNumber(),
            contratos.getSize(),
            contratos.getTotalElements(),
            contratos.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Generar nuevo contrato de compra venta")
    public ResponseEntity<ContratoCompraVentaDTO> generarContrato(@Valid @RequestBody ContratoCompraVentaDTO contratoDto) {
        ContratoCompraVentaDTO contratoGenerado = service.generarContratoVenta(contratoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoGenerado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar contrato de compra venta")
    public ResponseEntity<ContratoCompraVentaDTO> actualizarContrato(
            @PathVariable Integer id, 
            @Valid @RequestBody ContratoCompraVentaDTO contratoDto) {
        contratoDto.setIdContratoVenta(id);
        ContratoCompraVentaDTO contratoActualizado = service.actualizarContrato(contratoDto);
        return ResponseEntity.ok(contratoActualizado);
    }

    @PutMapping("/{id}/firmar")
    @Operation(summary = "Registrar firma de contrato")
    public ResponseEntity<ContratoCompraVentaDTO> firmarContrato(
            @PathVariable Integer id,
            @RequestParam String rutaArchivoFirmado) {
        ContratoCompraVentaDTO contratoFirmado = service.registrarFirmaContrato(id, rutaArchivoFirmado);
        return ResponseEntity.ok(contratoFirmado);
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

    @ExceptionHandler({InvalidStateException.class})
    public ResponseEntity<String> manejarEstadoInvalido(InvalidStateException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }
} 