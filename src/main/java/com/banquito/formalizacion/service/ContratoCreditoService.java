package com.banquito.formalizacion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.controller.dto.ContratoCreditoCreateDTO;
import com.banquito.formalizacion.controller.dto.ContratoCreditoDTO;
import com.banquito.formalizacion.controller.dto.ContratoCreditoUpdateDTO;
import com.banquito.formalizacion.controller.mapper.ContratoCreditoMapper;
import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.exception.ContratoCreditoGenerationException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.model.ContratoCredito;
import com.banquito.formalizacion.repository.ContratoCreditoRepository;

@Service
public class ContratoCreditoService {

    private final ContratoCreditoRepository contratoCreditoRepository;
    private final ContratoCreditoMapper contratoCreditoMapper;

    public ContratoCreditoService(ContratoCreditoRepository contratoCreditoRepository,
                                  ContratoCreditoMapper contratoCreditoMapper) {
        this.contratoCreditoRepository = contratoCreditoRepository;
        this.contratoCreditoMapper = contratoCreditoMapper;
    }

    // Obtener contrato por ID
    @Transactional
    public ContratoCreditoDTO getContratoCreditoById(Long id) {
        try {
            ContratoCredito contrato = contratoCreditoRepository.findById(id)
                .orElseThrow(() -> new ContratoCreditoGenerationException("Contrato no encontrado: " + id));
            return contratoCreditoMapper.toDto(contrato);
        } catch (ContratoCreditoGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCreditoGenerationException("Error al obtener el Contrato: " + id);
        }
    }

    // Crear nuevo contrato
    @Transactional
    public ContratoCreditoDTO createContratoCredito(ContratoCreditoCreateDTO dto) {
        try {
            if (contratoCreditoRepository.existsByIdSolicitud(dto.getIdSolicitud())) {
                throw new ContratoCreditoGenerationException("Ya existe un contrato para solicitud " + dto.getIdSolicitud());
            }
            if (contratoCreditoRepository.existsByNumeroContratoCore(dto.getNumeroContratoCore())) {
                throw new NumeroContratoYaExisteException(dto.getNumeroContratoCore(), "ContratoCredito");
            }
            ContratoCredito contrato = contratoCreditoMapper.toEntity(dto);
            contrato.setEstado(ContratoCreditoEstado.PENDIENTE_FIRMA);
            contrato.setVersion(1L);

            ContratoCredito saved = contratoCreditoRepository.save(contrato);
            return contratoCreditoMapper.toDto(saved);
        } catch (ContratoCreditoGenerationException | NumeroContratoYaExisteException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCreditoGenerationException("Error al crear el Contrato de Crédito");
        }
    }

    // Actualizar contrato por ID
    @Transactional
    public ContratoCreditoDTO updateContratoCredito(Long id, ContratoCreditoUpdateDTO dto) {
        try {
            if (!id.equals(dto.getIdContratoCredito())) {
                throw new ContratoCreditoGenerationException("El ID del path no coincide con el del body");
            }
            ContratoCredito existing = contratoCreditoRepository.findById(id)
                .orElseThrow(() -> new ContratoCreditoGenerationException("Contrato no encontrado: " + id));

            contratoCreditoMapper.updateEntity(existing, dto);
            ContratoCredito updated = contratoCreditoRepository.save(existing);
            return contratoCreditoMapper.toDto(updated);

        } catch (ContratoCreditoGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCreditoGenerationException("Error al actualizar el Contrato: " + id);
        }
    }

    // Eliminación lógica: marca estado como CANCELADO
    @Transactional
    public ContratoCreditoDTO logicalDeleteContratoCredito(Long id) {
        try {
            ContratoCredito existing = contratoCreditoRepository.findById(id)
                .orElseThrow(() -> new ContratoCreditoGenerationException("Contrato no encontrado: " + id));

            if (ContratoCreditoEstado.CANCELADO.equals(existing.getEstado())) {
                throw new ContratoCreditoGenerationException("El contrato ya está cancelado: " + id);
            }
            existing.setEstado(ContratoCreditoEstado.CANCELADO);
            ContratoCredito saved = contratoCreditoRepository.save(existing);
            return contratoCreditoMapper.toDto(saved);

        } catch (ContratoCreditoGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new ContratoCreditoGenerationException("Error al eliminar lógicamente el Contrato: " + id);
        }
    }

    // Obtener contratos con filtros combinados
    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> findContratosConFiltros(
            ContratoCreditoEstado estado,
            String numeroContratoCore,
            Long idSolicitud,
            Pageable pageable) {
        try {
            Page<ContratoCredito> contratos;
            if (estado != null && numeroContratoCore != null && idSolicitud != null) {
                contratos = contratoCreditoRepository.findByEstadoAndNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
                        estado, numeroContratoCore, idSolicitud, pageable);
            } else if (estado != null && numeroContratoCore != null) {
                contratos = contratoCreditoRepository.findByEstadoAndNumeroContratoCoreContainingIgnoreCase(
                        estado, numeroContratoCore, pageable);
            } else if (estado != null && idSolicitud != null) {
                contratos = contratoCreditoRepository.findByEstadoAndIdSolicitud(estado, idSolicitud, pageable);
            } else if (numeroContratoCore != null && idSolicitud != null) {
                contratos = contratoCreditoRepository.findByNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
                        numeroContratoCore, idSolicitud, pageable);
            } else if (estado != null) {
                contratos = contratoCreditoRepository.findByEstado(estado, pageable);
            } else if (numeroContratoCore != null) {
                contratos = contratoCreditoRepository.findByNumeroContratoCoreContainingIgnoreCase(numeroContratoCore, pageable);
            } else if (idSolicitud != null) {
                contratos = contratoCreditoRepository.findByIdSolicitud(idSolicitud, pageable);
            } else {
                contratos = contratoCreditoRepository.findAll(pageable);
            }
            return contratos.map(contratoCreditoMapper::toDto);
        } catch (Exception e) {
            throw new ContratoCreditoGenerationException("Error al consultar contratos con filtros");
        }
    }

    // Verifica existencia por solicitud
    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Long idSolicitud) {
        try {
            return contratoCreditoRepository.existsByIdSolicitud(idSolicitud);
        } catch (Exception e) {
            throw new ContratoCreditoGenerationException("Error al verificar existencia de contrato para la solicitud: " + idSolicitud);
        }
    }
}
