package com.banquito.formalizacion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.exception.ContratoNoFirmadoException;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.exception.PagaresPendientesException;
import com.banquito.formalizacion.model.ContratoCredito;
import com.banquito.formalizacion.repository.ContratoCreditoRepository;

@Service
@Transactional
public class ContratoCreditoService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoCreditoService.class);

    private final ContratoCreditoRepository repository;
    private final PagareService pagareService;

    public ContratoCreditoService(ContratoCreditoRepository repository, PagareService pagareService) {
        this.repository = repository;
        this.pagareService = pagareService;
    }

    @Transactional(readOnly = true)
    public Page<ContratoCredito> findAll(Pageable pageable) {
        logger.debug("Consultando contratos de crédito con paginación: página {}, tamaño {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<ContratoCredito> findAll() {
        logger.debug("Consultando todos los contratos de crédito - USAR CON PRECAUCIÓN");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ContratoCredito findById(Integer id) {
        logger.debug("Consultando contrato de crédito por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCredito"));
    }

    @Transactional(readOnly = true)
    public ContratoCredito findByIdSolicitud(Integer idSolicitud) {
        logger.debug("Consultando contrato de crédito por ID de solicitud: {}", idSolicitud);
        return repository.findByIdSolicitud(idSolicitud)
                .orElseThrow(() -> new NotFoundException(idSolicitud.toString(), "ContratoCredito por solicitud"));
    }

    @Transactional(readOnly = true)
    public ContratoCredito findByNumeroContratoCore(String numeroContratoCore) {
        logger.debug("Consultando contrato de crédito por número core: {}", numeroContratoCore);
        return repository.findByNumeroContratoCore(numeroContratoCore)
                .orElseThrow(() -> new NotFoundException(numeroContratoCore, "ContratoCredito por número core"));
    }

    @Transactional(readOnly = true)
    public List<ContratoCredito> findByEstado(ContratoCreditoEstado estado) {
        logger.debug("Consultando contratos de crédito por estado: {}", estado);
        return repository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Page<ContratoCredito> findByEstado(ContratoCreditoEstado estado, Pageable pageable) {
        logger.debug("Consultando contratos de crédito por estado: {} con paginación", estado);
        Page<ContratoCredito> contratos = repository.findByEstado(estado, pageable);
        logger.debug("Encontrados {} contratos en la página {}", 
                    contratos.getNumberOfElements(), pageable.getPageNumber());
        return contratos;
    }

    @Transactional(readOnly = true)
    public Page<ContratoCredito> findContratosConFiltros(
            ContratoCreditoEstado estado, 
            String numeroContratoCore,
            Integer idSolicitud,
            Pageable pageable) {
        logger.debug("Consultando contratos con filtros - Estado: {}, Número Core: {}, Solicitud: {}", 
                    estado, numeroContratoCore, idSolicitud);
        
        // Implementación de filtros combinados
        if (estado != null && numeroContratoCore != null && idSolicitud != null) {
            return repository.findByEstadoAndNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
                estado, numeroContratoCore, idSolicitud, pageable);
        } else if (estado != null && numeroContratoCore != null) {
            return repository.findByEstadoAndNumeroContratoCoreContainingIgnoreCase(
                estado, numeroContratoCore, pageable);
        } else if (estado != null && idSolicitud != null) {
            return repository.findByEstadoAndIdSolicitud(estado, idSolicitud, pageable);
        } else if (numeroContratoCore != null && idSolicitud != null) {
            return repository.findByNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
                numeroContratoCore, idSolicitud, pageable);
        } else if (estado != null) {
            return findByEstado(estado, pageable);
        } else if (numeroContratoCore != null) {
            return repository.findByNumeroContratoCoreContainingIgnoreCase(numeroContratoCore, pageable);
        } else if (idSolicitud != null) {
            return repository.findByIdSolicitud(idSolicitud, pageable);
        } else {
            return findAll(pageable);
        }
    }

    public ContratoCredito instrumentarCredito(ContratoCredito contrato) {
        logger.info("Instrumentando crédito para solicitud: {}", contrato.getIdSolicitud());
        
        if (repository.existsByIdSolicitud(contrato.getIdSolicitud())) {
            throw new ContratoYaExisteException(contrato.getIdSolicitud(), "contrato de crédito");
        }

        if (repository.existsByNumeroContratoCore(contrato.getNumeroContratoCore())) {
            throw new NumeroContratoYaExisteException(contrato.getNumeroContratoCore(), "contrato de crédito");
        }

        contrato.setFechaGeneracion(LocalDateTime.now());
        contrato.setEstado(ContratoCreditoEstado.PENDIENTE_FIRMA);
        contrato.setVersion(1L);

        ContratoCredito contratoGuardado = repository.save(contrato);
        
        // Generar pagarés automáticamente
        pagareService.generarPagares(contratoGuardado);
        
        logger.info("Crédito instrumentado exitosamente con ID: {}", contratoGuardado.getIdContratoCredito());
        
        return contratoGuardado;
    }

    public ContratoCredito registrarFirmaContrato(Integer id, String rutaArchivoFirmado) {
        logger.info("Registrando firma de contrato de crédito ID: {}", id);
        
        ContratoCredito contrato = findById(id);
        
        if (contrato.getEstado() != ContratoCreditoEstado.PENDIENTE_FIRMA) {
            throw new InvalidStateException(
                contrato.getEstado().toString(), 
                ContratoCreditoEstado.ACTIVO.toString(), 
                "ContratoCredito"
            );
        }

        contrato.setFechaFirma(LocalDateTime.now());
        contrato.setRutaArchivoFirmado(rutaArchivoFirmado);
        contrato.setEstado(ContratoCreditoEstado.ACTIVO);
        contrato.setVersion(contrato.getVersion() + 1);

        ContratoCredito contratoActualizado = repository.save(contrato);
        logger.info("Firma de contrato registrada y crédito activado para ID: {}", id);
        
        return contratoActualizado;
    }

    public ContratoCredito aprobarDesembolso(Integer id) {
        logger.info("Aprobando desembolso para contrato de crédito ID: {}", id);
        
        ContratoCredito contrato = findById(id);
        
        if (contrato.getEstado() != ContratoCreditoEstado.PENDIENTE_FIRMA) {
            throw new InvalidStateException(
                contrato.getEstado().toString(), 
                ContratoCreditoEstado.ACTIVO.toString(), 
                "ContratoCredito para desembolso"
            );
        }

        if (contrato.getRutaArchivoFirmado() == null || contrato.getRutaArchivoFirmado().trim().isEmpty()) {
            throw new ContratoNoFirmadoException(id, "contrato de crédito para desembolso");
        }

        contrato.setEstado(ContratoCreditoEstado.ACTIVO);
        contrato.setVersion(contrato.getVersion() + 1);

        ContratoCredito contratoActualizado = repository.save(contrato);
        logger.info("Desembolso aprobado para contrato ID: {}", id);
        
        return contratoActualizado;
    }

    public ContratoCredito marcarComoPagado(Integer id) {
        logger.info("Marcando como pagado el contrato de crédito ID: {}", id);
        
        ContratoCredito contrato = findById(id);
        
        if (contrato.getEstado() != ContratoCreditoEstado.ACTIVO) {
            throw new InvalidStateException(
                contrato.getEstado().toString(), 
                ContratoCreditoEstado.PAGADO.toString(), 
                "ContratoCredito"
            );
        }

        // Verificar que todos los pagarés estén pagados
        long pagaresPendientes = pagareService.contarPagaresPendientesPorContrato(id);
        if (pagaresPendientes > 0) {
            throw new PagaresPendientesException(id, pagaresPendientes);
        }

        contrato.setEstado(ContratoCreditoEstado.PAGADO);
        contrato.setVersion(contrato.getVersion() + 1);

        ContratoCredito contratoActualizado = repository.save(contrato);
        logger.info("Contrato marcado como pagado exitosamente ID: {}", id);
        
        return contratoActualizado;
    }

    public ContratoCredito cancelarContrato(Integer id, String motivo) {
        logger.info("Cancelando contrato de crédito ID: {} por motivo: {}", id, motivo);
        
        ContratoCredito contrato = findById(id);
        
        if (contrato.getEstado() == ContratoCreditoEstado.PAGADO || 
            contrato.getEstado() == ContratoCreditoEstado.CANCELADO) {
            throw new InvalidStateException(
                contrato.getEstado().toString(), 
                ContratoCreditoEstado.CANCELADO.toString(), 
                "ContratoCredito"
            );
        }

        contrato.setEstado(ContratoCreditoEstado.CANCELADO);
        contrato.setVersion(contrato.getVersion() + 1);

        ContratoCredito contratoActualizado = repository.save(contrato);
        logger.info("Contrato cancelado exitosamente ID: {}", id);
        
        return contratoActualizado;
    }

    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Integer idSolicitud) {
        return repository.existsByIdSolicitud(idSolicitud);
    }

    @Transactional(readOnly = true)
    public List<ContratoCredito> obtenerContratosParaDesembolso() {
        return repository.findByEstado(ContratoCreditoEstado.PENDIENTE_FIRMA);
    }

    @Transactional(readOnly = true)
    public List<ContratoCredito> obtenerContratosActivos() {
        return repository.findByEstado(ContratoCreditoEstado.ACTIVO);
    }
} 