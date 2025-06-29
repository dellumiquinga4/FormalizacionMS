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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.formalizacion.controller.dto.PageResponseDTO;
import com.banquito.formalizacion.controller.dto.PagareDTO;
import com.banquito.formalizacion.controller.mapper.PagareMapper;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.model.Pagare;
import com.banquito.formalizacion.service.PagareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1/pagares")
@Tag(name = "Pagarés", description = "Gestión de pagarés y cronograma de pagos")
public class PagareController {

    private final PagareService service;
    private final PagareMapper mapper;

    public PagareController(PagareService service, PagareMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pagaré por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagaré encontrado"),
        @ApiResponse(responseCode = "404", description = "Pagaré no encontrado")
    })
    public ResponseEntity<PagareDTO> obtenerPagarePorId(
            @Parameter(description = "ID del pagaré") @PathVariable Integer id) {
        Pagare pagare = service.findById(id);
        return ResponseEntity.ok(mapper.toDTO(pagare));
    }

    @GetMapping("/contrato/{idContratoCredito}")
    @Operation(summary = "Obtener pagarés por contrato de crédito con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de pagarés obtenida exitosamente")
    })
    public ResponseEntity<PageResponseDTO<PagareDTO>> obtenerPagaresPorContratoConPaginacion(
            @Parameter(description = "ID del contrato de crédito") @PathVariable Integer idContratoCredito,
            @Parameter(description = "Número de página (inicia en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "numeroCuota") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Pagare> pagaresPage = service.findByContratoCredito(idContratoCredito, pageable);
        
        List<PagareDTO> dtos = new ArrayList<>(pagaresPage.getContent().size());
        for (Pagare pagare : pagaresPage.getContent()) {
            dtos.add(mapper.toDTO(pagare));
        }
        
        PageResponseDTO<PagareDTO> response = new PageResponseDTO<>(
            dtos, 
            pagaresPage.getNumber(), 
            pagaresPage.getSize(), 
            pagaresPage.getTotalElements(), 
            pagaresPage.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contrato/{idContratoCredito}/all")
    @Operation(summary = "Obtener todos los pagarés por contrato sin paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pagarés obtenida exitosamente")
    })
    public ResponseEntity<List<PagareDTO>> obtenerTodosPagaresPorContrato(
            @Parameter(description = "ID del contrato de crédito") @PathVariable Integer idContratoCredito) {
        List<Pagare> pagares = service.findByContratoCredito(idContratoCredito);
        List<PagareDTO> dtos = new ArrayList<>(pagares.size());
        
        for (Pagare pagare : pagares) {
            dtos.add(mapper.toDTO(pagare));
        }
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/registrar-pago")
    @Operation(summary = "Registrar pago de pagaré")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago registrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pagaré no encontrado"),
        @ApiResponse(responseCode = "409", description = "Estado del pagaré no permite registro de pago")
    })
    public ResponseEntity<PagareDTO> registrarPago(
            @Parameter(description = "ID del pagaré") @PathVariable Integer id) {
        Pagare pagarePagado = service.registrarPago(id);
        return ResponseEntity.ok(mapper.toDTO(pagarePagado));
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