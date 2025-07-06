package com.banquito.formalizacion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.client.SolicitudCreditoClient;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaCreateDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaUpdateDTO;
import com.banquito.formalizacion.controller.dto.SolicitudResumenDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCompraVentaMapper;
import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.exception.ContratoCompraVentaGenerationException;
import com.banquito.formalizacion.exception.ContratoYaExisteException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.model.ContratoCompraVenta;
import com.banquito.formalizacion.repository.ContratoCompraVentaRepository;

@Service
public class ContratoCompraVentaService {

    private final ContratoCompraVentaRepository contratoCompraVentaRepository;
    private final ContratoCompraVentaMapper contratoCompraVentaMapper;
    private final SolicitudCreditoClient solicitudCreditoClient;

    public ContratoCompraVentaService(ContratoCompraVentaRepository contratoCompraVentaRepository,
                                      ContratoCompraVentaMapper contratoCompraVentaMapper,
                                      SolicitudCreditoClient solicitudCreditoClient) {
        this.contratoCompraVentaRepository = contratoCompraVentaRepository;
        this.contratoCompraVentaMapper = contratoCompraVentaMapper;
        this.solicitudCreditoClient = solicitudCreditoClient;
    }

    // Obtiene un contrato de compra-venta por su ID.
    @Transactional
    public ContratoCompraVentaDTO getContratoCompraVentaById(Long id) {
        try {
            ContratoCompraVenta contrato = contratoCompraVentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCompraVenta"));
            return contratoCompraVentaMapper.toDTO(contrato);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCompraVentaGenerationException("Error al obtener el contrato de compra-venta: " + id);
        }
    }

    // Crea un nuevo contrato de compra-venta
    @Transactional
    public ContratoCompraVentaDTO createContratoCompraVenta(ContratoCompraVentaCreateDTO dto) {
        try {
            // 1. Trae la solicitud del microservicio de originación (usa el idSolicitud recibido en el DTO)
            SolicitudResumenDTO resumen = solicitudCreditoClient.obtenerSolicitudPorId(dto.getIdSolicitud());

            // 3. Validaciones de unicidad (NO cambian)
            if (contratoCompraVentaRepository.existsByIdSolicitud(resumen.getIdSolicitud())) {
                throw new ContratoYaExisteException(resumen.getIdSolicitud(), "ContratoCompraVenta");
            }
            if (contratoCompraVentaRepository.existsByNumeroContrato(dto.getNumeroContrato())) {
                throw new NumeroContratoYaExisteException(dto.getNumeroContrato(), "ContratoCompraVenta");
            }

            // 4. Construye la entidad desde el DTO
            ContratoCompraVenta contrato = contratoCompraVentaMapper.toEntity(dto);

            // 5. SOBRESCRIBE los valores sensibles con lo que trae originación
            contrato.setIdSolicitud(resumen.getIdSolicitud());
            contrato.setPrecioFinalVehiculo(resumen.getPrecioFinalVehiculo());// Siempre lo del MS originación
            contrato.setFechaGeneracion(LocalDateTime.now());
            contrato.setEstado(ContratoVentaEstado.PENDIENTE_FIRMA);
            contrato.setVersion(1L);

            // 6. Guarda y retorna el DTO
            ContratoCompraVenta saved = contratoCompraVentaRepository.save(contrato);
            return contratoCompraVentaMapper.toDTO(saved);
        } catch (ContratoYaExisteException | NumeroContratoYaExisteException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCompraVentaGenerationException("Error al crear el contrato de compra-venta", e);
        }
    }

    // Actualiza un contrato existente por su ID
    @Transactional
    public ContratoCompraVentaDTO updateContratoCompraVenta(Long id, ContratoCompraVentaUpdateDTO dto) {
        try {
            if (!id.equals(dto.getIdContratoVenta())) {
                throw new ContratoCompraVentaGenerationException("El ID del path no coincide con el del body");
            }
            ContratoCompraVenta existing = contratoCompraVentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "ContratoCompraVenta"));
            contratoCompraVentaMapper.updateEntity(existing, dto);
            existing.setVersion(existing.getVersion() + 1);
            ContratoCompraVenta updated = contratoCompraVentaRepository.save(existing);
            return contratoCompraVentaMapper.toDTO(updated);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCompraVentaGenerationException("Error al actualizar el contrato de compra-venta: " + id);
        }
    }

    // Listar todos los contratos por estado
    @Transactional
    public List<ContratoCompraVentaDTO> getContratosByEstado(ContratoVentaEstado estado) {
        try {
            var contratos = contratoCompraVentaRepository.findByEstado(estado);
            return contratoCompraVentaMapper.toDTOList(contratos);
        } catch (Exception e) {
            throw new ContratoCompraVentaGenerationException("Error al obtener contratos de compra-venta por estado");
        }
    }

    // Verificar existencia de contrato por solicitud
    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Long idSolicitud) {
        try {
            return contratoCompraVentaRepository.existsByIdSolicitud(idSolicitud);
        } catch (Exception e) {
            throw new ContratoCompraVentaGenerationException("Error al verificar existencia de contrato de compra-venta para la solicitud: " + idSolicitud);
        }
    }
}
