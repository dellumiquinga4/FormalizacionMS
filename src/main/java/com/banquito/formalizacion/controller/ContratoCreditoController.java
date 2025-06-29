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

import com.banquito.formalizacion.controller.dto.ContratoCreditoDTO;
import com.banquito.formalizacion.controller.dto.PageResponseDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCreditoMapper;
import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.exception.ContratoNoFirmadoException;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.exception.PagaresPendientesException;
import com.banquito.formalizacion.model.ContratoCredito;
import com.banquito.formalizacion.service.ContratoCreditoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/contratos-credito")
@Tag(name = "Contratos de Crédito", description = "Gestión del ciclo de vida de contratos de crédito automotriz")
public class ContratoCreditoController {

    private final ContratoCreditoService service;
    private final ContratoCreditoMapper mapper;

    public ContratoCreditoController(ContratoCreditoService service, ContratoCreditoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Obtener contratos de crédito con paginación y filtros")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de contratos obtenida exitosamente")
    })
    public ResponseEntity<PageResponseDTO<ContratoCreditoDTO>> obtenerContratosConPaginacion(
            @Parameter(description = "Número de página (inicia en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "fechaGeneracion") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Estado del contrato") @RequestParam(required = false) ContratoCreditoEstado estado,
            @Parameter(description = "Número de contrato core (búsqueda parcial)") @RequestParam(required = false) String numeroContratoCore,
            @Parameter(description = "ID de solicitud") @RequestParam(required = false) Integer idSolicitud) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContratoCredito> contratosPage = service.findContratosConFiltros(
            estado, numeroContratoCore, idSolicitud, pageable);
        
        List<ContratoCreditoDTO> dtos = new ArrayList<>(contratosPage.getContent().size());
        for (ContratoCredito contrato : contratosPage.getContent()) {
            dtos.add(mapper.toDTO(contrato));
        }
        
        PageResponseDTO<ContratoCreditoDTO> response = new PageResponseDTO<>(
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
    public ResponseEntity<List<ContratoCreditoDTO>> obtenerTodosLosContratos() {
        List<ContratoCredito> contratos = service.findAll();
        List<ContratoCreditoDTO> dtos = new ArrayList<>(contratos.size());
        
        for (ContratoCredito contrato : contratos) {
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
    public ResponseEntity<ContratoCreditoDTO> obtenerContratoPorId(
            @Parameter(description = "ID del contrato") @PathVariable Integer id) {
        ContratoCredito contrato = service.findById(id);
        return ResponseEntity.ok(mapper.toDTO(contrato));
    }

    @GetMapping("/solicitud/{idSolicitud}")
    @Operation(summary = "Obtener contrato por ID de solicitud")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoCreditoDTO> obtenerContratoPorSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Integer idSolicitud) {
        ContratoCredito contrato = service.findByIdSolicitud(idSolicitud);
        return ResponseEntity.ok(mapper.toDTO(contrato));
    }

    @GetMapping("/numero-core/{numeroContratoCore}")
    @Operation(summary = "Obtener contrato por número core")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoCreditoDTO> obtenerContratoPorNumeroCore(
            @Parameter(description = "Número del contrato core") @PathVariable String numeroContratoCore) {
        ContratoCredito contrato = service.findByNumeroContratoCore(numeroContratoCore);
        return ResponseEntity.ok(mapper.toDTO(contrato));
    }

    @GetMapping("/estado")
    @Operation(summary = "Obtener contratos por estado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contratos obtenida exitosamente")
    })
    public ResponseEntity<List<ContratoCreditoDTO>> obtenerContratosPorEstado(
            @Parameter(description = "Estado del contrato") @RequestParam ContratoCreditoEstado estado) {
        List<ContratoCredito> contratos = service.findByEstado(estado);
        List<ContratoCreditoDTO> dtos = new ArrayList<>(contratos.size());
        
        for (ContratoCredito contrato : contratos) {
            dtos.add(mapper.toDTO(contrato));
        }
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/para-desembolso")
    @Operation(summary = "Obtener contratos listos para desembolso")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contratos obtenida exitosamente")
    })
    public ResponseEntity<List<ContratoCreditoDTO>> obtenerContratosParaDesembolso() {
        List<ContratoCredito> contratos = service.obtenerContratosParaDesembolso();
        List<ContratoCreditoDTO> dtos = new ArrayList<>(contratos.size());
        
        for (ContratoCredito contrato : contratos) {
            dtos.add(mapper.toDTO(contrato));
        }
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener contratos activos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contratos obtenida exitosamente")
    })
    public ResponseEntity<List<ContratoCreditoDTO>> obtenerContratosActivos() {
        List<ContratoCredito> contratos = service.obtenerContratosActivos();
        List<ContratoCreditoDTO> dtos = new ArrayList<>(contratos.size());
        
        for (ContratoCredito contrato : contratos) {
            dtos.add(mapper.toDTO(contrato));
        }
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/instrumentar")
    @Operation(summary = "Instrumentar crédito (FR-09)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Crédito instrumentado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflicto - Ya existe un contrato para la solicitud")
    })
    public ResponseEntity<ContratoCreditoDTO> instrumentarCredito(
            @Parameter(description = "Datos del contrato de crédito") @Valid @RequestBody ContratoCreditoDTO contratoDTO) {
        ContratoCredito contrato = mapper.toModel(contratoDTO);
        ContratoCredito contratoInstrumentado = service.instrumentarCredito(contrato);
        return ResponseEntity.status(201).body(mapper.toDTO(contratoInstrumentado));
    }

    @PatchMapping("/{id}/firmar")
    @Operation(summary = "Registrar firma del contrato (FR-11)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Firma registrada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
        @ApiResponse(responseCode = "409", description = "Estado del contrato no permite firma")
    })
    public ResponseEntity<ContratoCreditoDTO> registrarFirmaContrato(
            @Parameter(description = "ID del contrato") @PathVariable Integer id,
            @Parameter(description = "Ruta del archivo firmado") @RequestParam String rutaArchivoFirmado) {
        ContratoCredito contratoFirmado = service.registrarFirmaContrato(id, rutaArchivoFirmado);
        return ResponseEntity.ok(mapper.toDTO(contratoFirmado));
    }

    @PatchMapping("/{id}/aprobar-desembolso")
    @Operation(summary = "Aprobar desembolso (FR-12/FR-13)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Desembolso aprobado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
        @ApiResponse(responseCode = "409", description = "Estado del contrato no permite desembolso")
    })
    public ResponseEntity<ContratoCreditoDTO> aprobarDesembolso(
            @Parameter(description = "ID del contrato") @PathVariable Integer id) {
        ContratoCredito contratoDesembolsado = service.aprobarDesembolso(id);
        return ResponseEntity.ok(mapper.toDTO(contratoDesembolsado));
    }

    @PatchMapping("/{id}/marcar-pagado")
    @Operation(summary = "Marcar contrato como pagado (FR-16)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato marcado como pagado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
        @ApiResponse(responseCode = "409", description = "Estado del contrato no permite marcar como pagado")
    })
    public ResponseEntity<ContratoCreditoDTO> marcarComoPagado(
            @Parameter(description = "ID del contrato") @PathVariable Integer id) {
        ContratoCredito contratoPagado = service.marcarComoPagado(id);
        return ResponseEntity.ok(mapper.toDTO(contratoPagado));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato cancelado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
        @ApiResponse(responseCode = "409", description = "Estado del contrato no permite cancelación")
    })
    public ResponseEntity<ContratoCreditoDTO> cancelarContrato(
            @Parameter(description = "ID del contrato") @PathVariable Integer id,
            @Parameter(description = "Motivo de la cancelación") @RequestParam String motivo) {
        ContratoCredito contratoCancelado = service.cancelarContrato(id, motivo);
        return ResponseEntity.ok(mapper.toDTO(contratoCancelado));
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> manejarNoEncontrado(NotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({ContratoYaExisteException.class, NumeroContratoYaExisteException.class, 
                      ContratoNoFirmadoException.class, PagaresPendientesException.class})
    public ResponseEntity<String> manejarErrorLogicaNegocio(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler({InvalidStateException.class})
    public ResponseEntity<String> manejarEstadoInvalido(InvalidStateException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }
} 