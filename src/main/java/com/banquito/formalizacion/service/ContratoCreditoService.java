package com.banquito.formalizacion.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.controller.dto.ContratoCreditoDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCreditoMapper;
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
    private final ContratoCreditoMapper mapper;
    private final PagareService pagareService;

    public ContratoCreditoService(ContratoCreditoRepository repository, 
                                 ContratoCreditoMapper mapper,
                                 PagareService pagareService) {
        this.repository = repository;
        this.mapper = mapper;
        this.pagareService = pagareService;
    }

    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> findAll(Pageable pageable) {
        try {
            logger.debug("Consultando contratos de crédito con paginación: página {}, tamaño {}", 
                        pageable.getPageNumber(), pageable.getPageSize());
            Page<ContratoCredito> contratos = repository.findAll(pageable);
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar contratos de crédito con paginación", e);
            throw new RuntimeException("Error al consultar contratos de crédito");
        }
    }

    @Transactional(readOnly = true)
    public ContratoCreditoDTO findById(Integer id) {
        try {
            logger.debug("Consultando contrato de crédito por ID: {}", id);
            ContratoCredito contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCredito"));
            return mapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar contrato de crédito por ID: {}", id, e);
            throw new RuntimeException("Error al consultar contrato de crédito");
        }
    }

    @Transactional(readOnly = true)
    public ContratoCreditoDTO findByIdSolicitud(Integer idSolicitud) {
        try {
            logger.debug("Consultando contrato de crédito por ID de solicitud: {}", idSolicitud);
            ContratoCredito contrato = repository.findByIdSolicitud(idSolicitud)
                    .orElseThrow(() -> new NotFoundException(idSolicitud.toString(), "ContratoCredito por solicitud"));
            return mapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar contrato de crédito por solicitud: {}", idSolicitud, e);
            throw new RuntimeException("Error al consultar contrato de crédito por solicitud");
        }
    }

    @Transactional(readOnly = true)
    public ContratoCreditoDTO findByNumeroContratoCore(String numeroContratoCore) {
        try {
            logger.debug("Consultando contrato de crédito por número core: {}", numeroContratoCore);
            ContratoCredito contrato = repository.findByNumeroContratoCore(numeroContratoCore)
                    .orElseThrow(() -> new NotFoundException(numeroContratoCore, "ContratoCredito por número core"));
            return mapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar contrato de crédito por número core: {}", numeroContratoCore, e);
            throw new RuntimeException("Error al consultar contrato de crédito por número core");
        }
    }

    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> findByEstado(ContratoCreditoEstado estado, Pageable pageable) {
        try {
            logger.debug("Consultando contratos de crédito por estado: {} con paginación", estado);
            Page<ContratoCredito> contratos = repository.findByEstado(estado, pageable);
            logger.debug("Encontrados {} contratos en la página {}", 
                        contratos.getNumberOfElements(), pageable.getPageNumber());
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar contratos por estado: {}", estado, e);
            throw new RuntimeException("Error al consultar contratos por estado");
        }
    }

    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> findContratosConFiltros(
            ContratoCreditoEstado estado, 
            String numeroContratoCore,
            Integer idSolicitud,
            Pageable pageable) {
        try {
            logger.debug("Consultando contratos con filtros - Estado: {}, Número Core: {}, Solicitud: {}", 
                        estado, numeroContratoCore, idSolicitud);
            
            Page<ContratoCredito> contratos;
            
            // Implementación de filtros combinados
            if (estado != null && numeroContratoCore != null && idSolicitud != null) {
                contratos = repository.findByEstadoAndNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
                    estado, numeroContratoCore, idSolicitud, pageable);
            } else if (estado != null && numeroContratoCore != null) {
                contratos = repository.findByEstadoAndNumeroContratoCoreContainingIgnoreCase(
                    estado, numeroContratoCore, pageable);
            } else if (estado != null && idSolicitud != null) {
                contratos = repository.findByEstadoAndIdSolicitud(estado, idSolicitud, pageable);
            } else if (numeroContratoCore != null && idSolicitud != null) {
                contratos = repository.findByNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
                    numeroContratoCore, idSolicitud, pageable);
            } else if (estado != null) {
                contratos = repository.findByEstado(estado, pageable);
            } else if (numeroContratoCore != null) {
                contratos = repository.findByNumeroContratoCoreContainingIgnoreCase(numeroContratoCore, pageable);
            } else if (idSolicitud != null) {
                contratos = repository.findByIdSolicitud(idSolicitud, pageable);
            } else {
                contratos = repository.findAll(pageable);
            }
            
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar contratos con filtros", e);
            throw new RuntimeException("Error al consultar contratos con filtros");
        }
    }

    public ContratoCreditoDTO instrumentarCredito(ContratoCreditoDTO contratoDto) {
        try {
            logger.info("Instrumentando crédito para solicitud: {}", contratoDto.getIdSolicitud());
            
            if (repository.existsByIdSolicitud(contratoDto.getIdSolicitud())) {
                throw new ContratoYaExisteException(contratoDto.getIdSolicitud(), "contrato de crédito");
            }

            if (repository.existsByNumeroContratoCore(contratoDto.getNumeroContratoCore())) {
                throw new NumeroContratoYaExisteException(contratoDto.getNumeroContratoCore(), "contrato de crédito");
            }

            ContratoCredito contrato = mapper.toModel(contratoDto);
            contrato.setFechaGeneracion(LocalDateTime.now());
            contrato.setEstado(ContratoCreditoEstado.PENDIENTE_FIRMA);
            contrato.setVersion(1L);

            ContratoCredito contratoGuardado = repository.save(contrato);
            
            // Generar pagarés automáticamente
            pagareService.generarPagares(contratoGuardado);
            
            logger.info("Crédito instrumentado exitosamente con ID: {}", contratoGuardado.getIdContratoCredito());
            
            return mapper.toDTO(contratoGuardado);
        } catch (ContratoYaExisteException | NumeroContratoYaExisteException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al instrumentar crédito", e);
            throw new RuntimeException("Error al instrumentar crédito");
        }
    }

    public ContratoCreditoDTO registrarFirmaContrato(Integer id, String rutaArchivoFirmado) {
        try {
            logger.info("Registrando firma de contrato de crédito ID: {}", id);
            
            ContratoCredito contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCredito"));
            
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
            
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException | InvalidStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al registrar firma de contrato ID: {}", id, e);
            throw new RuntimeException("Error al registrar firma de contrato");
        }
    }

    public ContratoCreditoDTO aprobarDesembolso(Integer id) {
        try {
            logger.info("Aprobando desembolso para contrato de crédito ID: {}", id);
            
            ContratoCredito contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCredito"));
            
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
            
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException | InvalidStateException | ContratoNoFirmadoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al aprobar desembolso para contrato ID: {}", id, e);
            throw new RuntimeException("Error al aprobar desembolso");
        }
    }

    public ContratoCreditoDTO marcarComoPagado(Integer id) {
        try {
            logger.info("Marcando como pagado el contrato de crédito ID: {}", id);
            
            ContratoCredito contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCredito"));
            
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
            
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException | InvalidStateException | PagaresPendientesException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al marcar contrato como pagado ID: {}", id, e);
            throw new RuntimeException("Error al marcar contrato como pagado");
        }
    }

    public ContratoCreditoDTO cancelarContrato(Integer id, String motivo) {
        try {
            logger.info("Cancelando contrato de crédito ID: {} por motivo: {}", id, motivo);
            
            ContratoCredito contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCredito"));
            
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
            
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException | InvalidStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al cancelar contrato ID: {}", id, e);
            throw new RuntimeException("Error al cancelar contrato");
        }
    }

    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Integer idSolicitud) {
        try {
            return repository.existsByIdSolicitud(idSolicitud);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de contrato por solicitud: {}", idSolicitud, e);
            throw new RuntimeException("Error al verificar existencia de contrato");
        }
    }

    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> obtenerContratosParaDesembolso(Pageable pageable) {
        try {
            Page<ContratoCredito> contratos = repository.findByEstado(ContratoCreditoEstado.PENDIENTE_FIRMA, pageable);
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener contratos para desembolso", e);
            throw new RuntimeException("Error al obtener contratos para desembolso");
        }
    }

    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> obtenerContratosActivos(Pageable pageable) {
        try {
            Page<ContratoCredito> contratos = repository.findByEstado(ContratoCreditoEstado.ACTIVO, pageable);
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener contratos activos", e);
            throw new RuntimeException("Error al obtener contratos activos");
        }
    }
} 