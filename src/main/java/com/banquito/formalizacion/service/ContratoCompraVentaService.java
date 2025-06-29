package com.banquito.formalizacion.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCompraVentaMapper;
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
    private final ContratoCompraVentaMapper mapper;

    public ContratoCompraVentaService(ContratoCompraVentaRepository repository, 
                                     ContratoCompraVentaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ContratoCompraVentaDTO> findAll(Pageable pageable) {
        try {
            logger.debug("Consultando contratos de compra venta con paginación: página {}, tamaño {}", 
                        pageable.getPageNumber(), pageable.getPageSize());
            Page<ContratoCompraVenta> contratos = repository.findAll(pageable);
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar contratos de compra venta con paginación", e);
            throw new RuntimeException("Error al consultar contratos de compra venta");
        }
    }

    @Transactional(readOnly = true)
    public ContratoCompraVentaDTO findById(Integer id) {
        try {
            logger.debug("Consultando contrato de compra venta por ID: {}", id);
            ContratoCompraVenta contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCompraVenta"));
            return mapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar contrato de compra venta por ID: {}", id, e);
            throw new RuntimeException("Error al consultar contrato de compra venta");
        }
    }

    @Transactional(readOnly = true)
    public ContratoCompraVentaDTO findByIdSolicitud(Integer idSolicitud) {
        try {
            logger.debug("Consultando contrato de compra venta por ID de solicitud: {}", idSolicitud);
            ContratoCompraVenta contrato = repository.findByIdSolicitud(idSolicitud)
                    .orElseThrow(() -> new NotFoundException(idSolicitud.toString(), "ContratoCompraVenta por solicitud"));
            return mapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar contrato de compra venta por solicitud: {}", idSolicitud, e);
            throw new RuntimeException("Error al consultar contrato de compra venta por solicitud");
        }
    }

    @Transactional(readOnly = true)
    public ContratoCompraVentaDTO findByNumeroContrato(String numeroContrato) {
        try {
            logger.debug("Consultando contrato de compra venta por número: {}", numeroContrato);
            ContratoCompraVenta contrato = repository.findByNumeroContrato(numeroContrato)
                    .orElseThrow(() -> new NotFoundException(numeroContrato, "ContratoCompraVenta por número"));
            return mapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar contrato de compra venta por número: {}", numeroContrato, e);
            throw new RuntimeException("Error al consultar contrato de compra venta por número");
        }
    }

    @Transactional(readOnly = true)
    public Page<ContratoCompraVentaDTO> findByEstado(ContratoVentaEstado estado, Pageable pageable) {
        try {
            logger.debug("Consultando contratos de compra venta por estado: {} con paginación", estado);
            Page<ContratoCompraVenta> contratos = repository.findByEstado(estado, pageable);
            logger.debug("Encontrados {} contratos en la página {}", 
                        contratos.getNumberOfElements(), pageable.getPageNumber());
            return contratos.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar contratos por estado: {}", estado, e);
            throw new RuntimeException("Error al consultar contratos por estado");
        }
    }

    @Transactional(readOnly = true)
    public Page<ContratoCompraVentaDTO> findContratosConFiltros(
            ContratoVentaEstado estado, 
            String numeroContrato,
            Integer idSolicitud,
            Pageable pageable) {
        try {
            logger.debug("Consultando contratos con filtros - Estado: {}, Número: {}, Solicitud: {}", 
                        estado, numeroContrato, idSolicitud);
            
            Page<ContratoCompraVenta> contratos;
            
            // Implementación de filtros combinados
            if (estado != null && numeroContrato != null && idSolicitud != null) {
                contratos = repository.findByEstadoAndNumeroContratoContainingIgnoreCaseAndIdSolicitud(
                    estado, numeroContrato, idSolicitud, pageable);
            } else if (estado != null && numeroContrato != null) {
                contratos = repository.findByEstadoAndNumeroContratoContainingIgnoreCase(
                    estado, numeroContrato, pageable);
            } else if (estado != null && idSolicitud != null) {
                contratos = repository.findByEstadoAndIdSolicitud(estado, idSolicitud, pageable);
            } else if (numeroContrato != null && idSolicitud != null) {
                contratos = repository.findByNumeroContratoContainingIgnoreCaseAndIdSolicitud(
                    numeroContrato, idSolicitud, pageable);
            } else if (estado != null) {
                contratos = repository.findByEstado(estado, pageable);
            } else if (numeroContrato != null) {
                contratos = repository.findByNumeroContratoContainingIgnoreCase(numeroContrato, pageable);
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

    public ContratoCompraVentaDTO generarContratoVenta(ContratoCompraVentaDTO contratoDto) {
        try {
            logger.info("Generando contrato de compra venta para solicitud: {}", contratoDto.getIdSolicitud());
            
            if (repository.existsByIdSolicitud(contratoDto.getIdSolicitud())) {
                throw new ContratoYaExisteException(contratoDto.getIdSolicitud(), "contrato de compra venta");
            }

            if (repository.existsByNumeroContrato(contratoDto.getNumeroContrato())) {
                throw new NumeroContratoYaExisteException(contratoDto.getNumeroContrato(), "contrato de compra venta");
            }

            ContratoCompraVenta contrato = mapper.toModel(contratoDto);
            contrato.setFechaGeneracion(LocalDateTime.now());
            contrato.setEstado(ContratoVentaEstado.PENDIENTE_FIRMA);
            contrato.setVersion(1L);

            ContratoCompraVenta contratoGuardado = repository.save(contrato);
            logger.info("Contrato de compra venta generado exitosamente con ID: {}", contratoGuardado.getIdContratoVenta());
            
            return mapper.toDTO(contratoGuardado);
        } catch (ContratoYaExisteException | NumeroContratoYaExisteException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al generar contrato de compra venta", e);
            throw new RuntimeException("Error al generar contrato de compra venta");
        }
    }

    public ContratoCompraVentaDTO registrarFirmaContrato(Integer id, String rutaArchivoFirmado) {
        try {
            logger.info("Registrando firma de contrato de compra venta ID: {}", id);
            
            ContratoCompraVenta contrato = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCompraVenta"));
            
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
            
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException | InvalidStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al registrar firma de contrato ID: {}", id, e);
            throw new RuntimeException("Error al registrar firma de contrato");
        }
    }

    public ContratoCompraVentaDTO actualizarContrato(ContratoCompraVentaDTO contratoDto) {
        try {
            logger.info("Actualizando contrato de compra venta ID: {}", contratoDto.getIdContratoVenta());
            
            ContratoCompraVenta contratoExistente = repository.findById(contratoDto.getIdContratoVenta())
                    .orElseThrow(() -> new NotFoundException(contratoDto.getIdContratoVenta().toString(), "ContratoCompraVenta"));
            
            ContratoCompraVenta contrato = mapper.toModel(contratoDto);
            contrato.setVersion(contratoExistente.getVersion() + 1);
            
            ContratoCompraVenta contratoActualizado = repository.save(contrato);
            logger.info("Contrato de compra venta actualizado exitosamente");
            
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar contrato de compra venta", e);
            throw new RuntimeException("Error al actualizar contrato de compra venta");
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
} 