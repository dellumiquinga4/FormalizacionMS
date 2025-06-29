package com.banquito.formalizacion.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.dto.PageResponseDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCompraVentaMapper;
import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.model.ContratoCompraVenta;
import com.banquito.formalizacion.service.ContratoCompraVentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/contratos-compra-venta")
@Tag(name = "Contratos de Compra Venta", description = "Gestión de contratos de compra venta de vehículos")
public class ContratoCompraVentaController {

    private final ContratoCompraVentaService service;
    private final ContratoCompraVentaMapper mapper;

    public ContratoCompraVentaController(ContratoCompraVentaService service, ContratoCompraVentaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Obtener contratos de compra venta con paginación y filtros")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de contratos obtenida exitosamente")
    })
    public ResponseEntity<PageResponseDTO<ContratoCompraVentaDTO>> obtenerContratosConPaginacion(
            @Parameter(description = "Número de página (inicia en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "fechaGeneracion") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Estado del contrato") @RequestParam(required = false) ContratoVentaEstado estado,
            @Parameter(description = "Número de contrato (búsqueda parcial)") @RequestParam(required = false) String numeroContrato,
            @Parameter(description = "ID de solicitud") @RequestParam(required = false) Integer idSolicitud) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContratoCompraVenta> contratosPage = service.findContratosConFiltros(
            estado, numeroContrato, idSolicitud, pageable);
        
        List<ContratoCompraVentaDTO> dtos = new ArrayList<>(contratosPage.getContent().size());
        for (ContratoCompraVenta contrato : contratosPage.getContent()) {
            dtos.add(mapper.toDTO(contrato));
        }
        
        PageResponseDTO<ContratoCompraVentaDTO> response = new PageResponseDTO<>(
            dtos, 
            contratosPage.getNumber(), 
            contratosPage.getSize(), 
            contratosPage.getTotalElements(), 
            contratosPage.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los contratos sin paginación - USAR CON PRECAUCIÓN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista completa de contratos obtenida exitosamente")
    })
    public ResponseEntity<List<ContratoCompraVentaDTO>> obtenerTodosLosContratos() {
        List<ContratoCompraVenta> contratos = service.findAll();
        List<ContratoCompraVentaDTO> dtos = new ArrayList<>(contratos.size());
        
        for (ContratoCompraVenta contrato : contratos) {
            dtos.add(mapper.toDTO(contrato));
        }
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contrato por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoCompraVentaDTO> obtenerContratoPorId(
            @Parameter(description = "ID del contrato") @PathVariable Integer id) {
        ContratoCompraVenta contrato = service.findById(id);
        return ResponseEntity.ok(mapper.toDTO(contrato));
    }

    @GetMapping("/solicitud/{idSolicitud}")
    @Operation(summary = "Obtener contrato por ID de solicitud")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoCompraVentaDTO> obtenerContratoPorSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Integer idSolicitud) {
        ContratoCompraVenta contrato = service.findByIdSolicitud(idSolicitud);
        return ResponseEntity.ok(mapper.toDTO(contrato));
    }

    @GetMapping("/numero/{numeroContrato}")
    @Operation(summary = "Obtener contrato por número")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoCompraVentaDTO> obtenerContratoPorNumero(
            @Parameter(description = "Número del contrato") @PathVariable String numeroContrato) {
        ContratoCompraVenta contrato = service.findByNumeroContrato(numeroContrato);
        return ResponseEntity.ok(mapper.toDTO(contrato));
    }

    @GetMapping("/estado")
    @Operation(summary = "Obtener contratos por estado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contratos obtenida exitosamente")
    })
    public ResponseEntity<List<ContratoCompraVentaDTO>> obtenerContratosPorEstado(
            @Parameter(description = "Estado del contrato") @RequestParam ContratoVentaEstado estado) {
        List<ContratoCompraVenta> contratos = service.findByEstado(estado);
        List<ContratoCompraVentaDTO> dtos = new ArrayList<>(contratos.size());
        
        for (ContratoCompraVenta contrato : contratos) {
            dtos.add(mapper.toDTO(contrato));
        }
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Generar nuevo contrato de compra venta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contrato generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflicto - Ya existe un contrato para la solicitud")
    })
    public ResponseEntity<ContratoCompraVentaDTO> generarContrato(
            @Parameter(description = "Datos del contrato") @Valid @RequestBody ContratoCompraVentaDTO contratoDTO) {
        ContratoCompraVenta contrato = mapper.toModel(contratoDTO);
        ContratoCompraVenta contratoGenerado = service.generarContratoVenta(contrato);
        return ResponseEntity.status(201).body(mapper.toDTO(contratoGenerado));
    }

    @PatchMapping("/{id}/firmar")
    @Operation(summary = "Registrar firma del contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Firma registrada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
        @ApiResponse(responseCode = "409", description = "Estado del contrato no permite firma")
    })
    public ResponseEntity<ContratoCompraVentaDTO> registrarFirma(
            @Parameter(description = "ID del contrato") @PathVariable Integer id,
            @Parameter(description = "Ruta del archivo firmado") @RequestParam String rutaArchivoFirmado) {
        ContratoCompraVenta contratoFirmado = service.registrarFirmaContrato(id, rutaArchivoFirmado);
        return ResponseEntity.ok(mapper.toDTO(contratoFirmado));
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