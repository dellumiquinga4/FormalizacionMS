package com.banquito.formalizacion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaCreateDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaUpdateDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCompraVentaMapper;
import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
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

    //Obtiene un contrato de compra-venta por su ID.
    @Transactional(readOnly = true)
    public ContratoCompraVentaDTO findById(Long id) {
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

    // Crea un nuevo contrato de compra-venta
    @Transactional
    public ContratoCompraVentaDTO generarContratoVenta(ContratoCompraVentaCreateDTO contratoDto) {
        try {
            logger.info("Generando contrato de compra venta para solicitud: {}", contratoDto.getIdSolicitud());

            // Verifica si ya existe un contrato con la solicitud o número de contrato
            if (repository.existsByIdSolicitud(contratoDto.getIdSolicitud())) {
                throw new ContratoYaExisteException(contratoDto.getIdSolicitud(), "contrato de compra venta");
            }

            if (repository.existsByNumeroContrato(contratoDto.getNumeroContrato())) {
                throw new NumeroContratoYaExisteException(contratoDto.getNumeroContrato(), "contrato de compra venta");
            }

            // Convertir DTO a entidad
            ContratoCompraVenta contrato = mapper.toEntity(contratoDto);
            contrato.setFechaGeneracion(LocalDateTime.now());
            contrato.setEstado(ContratoVentaEstado.PENDIENTE_FIRMA);
            contrato.setVersion(1L);

            // Guardar la entidad en la base de datos
            ContratoCompraVenta contratoGuardado = repository.save(contrato);
            logger.info("Contrato de compra venta generado exitosamente con ID: {}", contratoGuardado.getIdContratoVenta());

            // Devolver DTO
            return mapper.toDTO(contratoGuardado);
        } catch (ContratoYaExisteException | NumeroContratoYaExisteException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al generar contrato de compra venta", e);
            throw new RuntimeException("Error al generar contrato de compra venta");
        }
    }

    // Actualiza un contrato existente
    @Transactional
    public ContratoCompraVentaDTO actualizarContrato(Long id, ContratoCompraVentaUpdateDTO dto) {
        try {
            logger.info("Actualizando contrato de compra venta ID: {}", id);

            // Verificar si el ID del path coincide con el ID en el DTO
            if (!id.equals(dto.getIdContratoVenta())) {
                throw new ContratoYaExisteException(dto.getIdSolicitud(), "contrato de compra venta");
            }

            // Buscar contrato existente en la base de datos
            ContratoCompraVenta contratoExistente = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCompraVenta"));

            // Mapear los datos del DTO de actualización a la entidad existente
            contratoExistente.setNumeroContrato(dto.getNumeroContrato());
            contratoExistente.setFechaFirma(dto.getFechaFirma());
            contratoExistente.setPrecioFinalVehiculo(dto.getPrecioFinalVehiculo());
            contratoExistente.setRutaArchivoFirmado(dto.getRutaArchivoFirmado());
            contratoExistente.setEstado(dto.getEstado());

            // Incrementar la versión del contrato antes de guardar
            contratoExistente.setVersion(contratoExistente.getVersion() + 1);

            // Guardar los cambios en la base de datos
            ContratoCompraVenta contratoActualizado = repository.save(contratoExistente);
            logger.info("Contrato de compra venta actualizado exitosamente");

            // Devolver el DTO de la entidad actualizada
            return mapper.toDTO(contratoActualizado);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar contrato de compra venta", e);
            throw new RuntimeException("Error al actualizar contrato de compra venta");
        }
    }

    // Método para verificar la existencia del contrato por ID de solicitud
    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Long idSolicitud) {
        try {
            return repository.existsByIdSolicitud(idSolicitud);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de contrato por solicitud: {}", idSolicitud, e);
            throw new RuntimeException("Error al verificar existencia de contrato");
        }
    }

    // Método para listar contratos por estado
    @Transactional(readOnly = true)
    public List<ContratoCompraVentaDTO> listarContratosPorEstado(ContratoVentaEstado estado) {
        try {
            logger.info("Consultando contratos de compra venta con estado: {}", estado);

            // Obtener los contratos filtrados por estado
            List<ContratoCompraVenta> contratos = repository.findByEstado(estado);

            // Convertir la lista de contratos a DTOs
            return contratos.stream()
                    .map(mapper::toDTO)  // Mapear la entidad a DTO
                    .toList();  // Retornar la lista
        } catch (Exception e) {
            logger.error("Error al consultar contratos de compra venta por estado: {}", estado, e);
            throw new RuntimeException("Error al consultar contratos de compra venta por estado");
        }
    }

}
