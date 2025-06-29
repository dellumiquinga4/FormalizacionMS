package com.banquito.formalizacion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.model.ContratoCompraVenta;
import com.banquito.formalizacion.repository.ContratoCompraVentaRepository;

@Service
@Transactional
public class ContratoCompraVentaService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoCompraVentaService.class);

    private final ContratoCompraVentaRepository repository;

    public ContratoCompraVentaService(ContratoCompraVentaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ContratoCompraVenta> findAll(Pageable pageable) {
        logger.debug("Consultando contratos de compra venta con paginación: página {}, tamaño {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<ContratoCompraVenta> findAll() {
        logger.debug("Consultando todos los contratos de compra venta - USAR CON PRECAUCIÓN");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ContratoCompraVenta findById(Integer id) {
        logger.debug("Consultando contrato de compra venta por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCompraVenta"));
    }

    @Transactional(readOnly = true)
    public ContratoCompraVenta findByIdSolicitud(Integer idSolicitud) {
        logger.debug("Consultando contrato de compra venta por ID de solicitud: {}", idSolicitud);
        return repository.findByIdSolicitud(idSolicitud)
                .orElseThrow(() -> new NotFoundException(idSolicitud.toString(), "ContratoCompraVenta por solicitud"));
    }

    @Transactional(readOnly = true)
    public ContratoCompraVenta findByNumeroContrato(String numeroContrato) {
        logger.debug("Consultando contrato de compra venta por número: {}", numeroContrato);
        return repository.findByNumeroContrato(numeroContrato)
                .orElseThrow(() -> new NotFoundException(numeroContrato, "ContratoCompraVenta por número"));
    }

    @Transactional(readOnly = true)
    public List<ContratoCompraVenta> findByEstado(ContratoVentaEstado estado) {
        logger.debug("Consultando contratos de compra venta por estado: {}", estado);
        return repository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Page<ContratoCompraVenta> findByEstado(ContratoVentaEstado estado, Pageable pageable) {
        logger.debug("Consultando contratos de compra venta por estado: {} con paginación", estado);
        Page<ContratoCompraVenta> contratos = repository.findByEstado(estado, pageable);
        logger.debug("Encontrados {} contratos en la página {}", 
                    contratos.getNumberOfElements(), pageable.getPageNumber());
        return contratos;
    }

    @Transactional(readOnly = true)
    public Page<ContratoCompraVenta> findContratosConFiltros(
            ContratoVentaEstado estado, 
            String numeroContrato,
            Integer idSolicitud,
            Pageable pageable) {
        logger.debug("Consultando contratos con filtros - Estado: {}, Número: {}, Solicitud: {}", 
                    estado, numeroContrato, idSolicitud);
        
        // Implementación de filtros combinados
        if (estado != null && numeroContrato != null && idSolicitud != null) {
            // Todos los filtros
            return repository.findByEstadoAndNumeroContratoContainingIgnoreCaseAndIdSolicitud(
                estado, numeroContrato, idSolicitud, pageable);
        } else if (estado != null && numeroContrato != null) {
            // Estado y número
            return repository.findByEstadoAndNumeroContratoContainingIgnoreCase(
                estado, numeroContrato, pageable);
        } else if (estado != null && idSolicitud != null) {
            // Estado y solicitud
            return repository.findByEstadoAndIdSolicitud(estado, idSolicitud, pageable);
        } else if (numeroContrato != null && idSolicitud != null) {
            // Número y solicitud
            return repository.findByNumeroContratoContainingIgnoreCaseAndIdSolicitud(
                numeroContrato, idSolicitud, pageable);
        } else if (estado != null) {
            // Solo estado
            return findByEstado(estado, pageable);
        } else if (numeroContrato != null) {
            // Solo número
            return repository.findByNumeroContratoContainingIgnoreCase(numeroContrato, pageable);
        } else if (idSolicitud != null) {
            // Solo solicitud
            return repository.findByIdSolicitud(idSolicitud, pageable);
        } else {
            // Sin filtros
            return findAll(pageable);
        }
    }

    public ContratoCompraVenta generarContratoVenta(ContratoCompraVenta contrato) {
        logger.info("Generando contrato de compra venta para solicitud: {}", contrato.getIdSolicitud());
        
        if (repository.existsByIdSolicitud(contrato.getIdSolicitud())) {
            throw new ContratoYaExisteException(contrato.getIdSolicitud(), "contrato de compra venta");
        }

        if (repository.existsByNumeroContrato(contrato.getNumeroContrato())) {
            throw new NumeroContratoYaExisteException(contrato.getNumeroContrato(), "contrato de compra venta");
        }

        contrato.setFechaGeneracion(LocalDateTime.now());
        contrato.setEstado(ContratoVentaEstado.PENDIENTE_FIRMA);
        contrato.setVersion(1L);

        ContratoCompraVenta contratoGuardado = repository.save(contrato);
        logger.info("Contrato de compra venta generado exitosamente con ID: {}", contratoGuardado.getIdContratoVenta());
        
        return contratoGuardado;
    }

    public ContratoCompraVenta registrarFirmaContrato(Integer id, String rutaArchivoFirmado) {
        logger.info("Registrando firma de contrato de compra venta ID: {}", id);
        
        ContratoCompraVenta contrato = findById(id);
        
        if (contrato.getEstado() != ContratoVentaEstado.PENDIENTE_FIRMA) {
            throw new InvalidStateException(
                contrato.getEstado().toString(), 
                ContratoVentaEstado.FIRMADO.toString(), 
                "ContratoCompraVenta"
            );
        }

        contrato.setFechaFirma(LocalDateTime.now());
        contrato.setRutaArchivoFirmado(rutaArchivoFirmado);
        contrato.setEstado(ContratoVentaEstado.FIRMADO);
        contrato.setVersion(contrato.getVersion() + 1);

        ContratoCompraVenta contratoActualizado = repository.save(contrato);
        logger.info("Firma de contrato registrada exitosamente para ID: {}", id);
        
        return contratoActualizado;
    }

    public ContratoCompraVenta actualizarContrato(ContratoCompraVenta contrato) {
        logger.info("Actualizando contrato de compra venta ID: {}", contrato.getIdContratoVenta());
        
        ContratoCompraVenta contratoExistente = findById(contrato.getIdContratoVenta());
        contrato.setVersion(contratoExistente.getVersion() + 1);
        
        ContratoCompraVenta contratoActualizado = repository.save(contrato);
        logger.info("Contrato de compra venta actualizado exitosamente");
        
        return contratoActualizado;
    }

    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Integer idSolicitud) {
        return repository.existsByIdSolicitud(idSolicitud);
    }
} 