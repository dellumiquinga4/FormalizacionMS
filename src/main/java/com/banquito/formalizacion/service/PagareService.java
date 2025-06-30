package com.banquito.formalizacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.controller.dto.PagareDTO;
import com.banquito.formalizacion.controller.dto.PagareCreateDTO;
import com.banquito.formalizacion.controller.dto.PagareUpdateDTO;
import com.banquito.formalizacion.controller.mapper.PagareMapper;
import com.banquito.formalizacion.enums.PagareEstado;
import com.banquito.formalizacion.exception.PagareGenerationException;
import com.banquito.formalizacion.model.Pagare;
import com.banquito.formalizacion.repository.PagareRepository;

@Service
public class PagareService {

    private final PagareRepository pagareRepository;
    private final PagareMapper pagareMapper;

    public PagareService(PagareRepository pagareRepository, PagareMapper pagareMapper) {
        this.pagareRepository = pagareRepository;
        this.pagareMapper = pagareMapper;
    }

    // Obtener un pagaré por su ID
    @Transactional
    public PagareDTO getPagareById(Long id) {
        try {
            Pagare pagare = pagareRepository.findById(id)
                .orElseThrow(() -> new PagareGenerationException("Pagaré no encontrado: " + id));
            return pagareMapper.toDto(pagare);
        } catch (PagareGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PagareGenerationException("Error al obtener el Pagaré: " + id);
        }
    }

    // Crear un nuevo pagaré
    @Transactional
    public PagareDTO createPagare(PagareCreateDTO dto) {
        try {
            Pagare pagare = pagareMapper.toEntity(dto);
            Pagare saved = pagareRepository.save(pagare);
            return pagareMapper.toDto(saved);
        } catch (Exception e) {
            throw new PagareGenerationException("Error al crear el pagaré");
        }
    }

    // Obtener todos los pagarés de un contrato de crédito (ordenados por cuota)
    @Transactional
    public List<PagareDTO> getPagaresByContratoCredito(Long idContratoCredito) {
        try {
            var pagares = pagareRepository.findByIdContratoCreditoOrderByNumeroCuota(idContratoCredito);
            return pagareMapper.toDtoList(pagares);
        } catch (Exception e) {
            throw new PagareGenerationException("Error al obtener pagarés del contrato de crédito");
        }
    }

    // Obtener un pagaré concreto de un contrato y número de cuota
    @Transactional
    public PagareDTO getPagareByContratoAndCuota(Long idContratoCredito, Long numeroCuota) {
        try {
            return pagareRepository
                .findByIdContratoCreditoAndNumeroCuota(idContratoCredito, numeroCuota)
                .map(pagareMapper::toDto)
                .orElseThrow(() ->
                    new PagareGenerationException(
                        "No se encontró el pagaré para contrato "
                        + idContratoCredito + " y cuota " + numeroCuota
                    )
                );
        } catch (PagareGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PagareGenerationException("Error al obtener el pagaré específico");
        }
    }

    // Actualizar un pagaré existente
    @Transactional
    public PagareDTO updatePagare(Long id, PagareUpdateDTO dto) {
        try {
            if (!id.equals(dto.getIdPagare())) {
                throw new PagareGenerationException("El ID del path no coincide con el del body");
            }
            Pagare existing = pagareRepository.findById(id)
                .orElseThrow(() -> new PagareGenerationException("Pagaré no encontrado: " + id));

            pagareMapper.updateEntity(existing, dto);
            Pagare updated = pagareRepository.save(existing);
            return pagareMapper.toDto(updated);
        } catch (PagareGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PagareGenerationException("Error al actualizar el pagaré: " + id);
        }
    }

    // Genera pagarés a partir de los parámetros (stub temporal)
    @Transactional
    public List<PagareDTO> generarPagaresDesdeParams(
            Long idContratoCredito,
            BigDecimal montoSolicitado,
            BigDecimal tasaAnual,
            int plazoMeses,
            LocalDate fechaInicio) {
        try {
            if (pagareRepository.existsByIdContratoCredito(idContratoCredito)) {
                throw new PagareGenerationException("Ya existen pagarés para contrato " + idContratoCredito);
            }
            List<Pagare> pagares = new ArrayList<>();
            BigDecimal cuotaMensual = calcularCuotaMensual(montoSolicitado, tasaAnual, plazoMeses);

            for (int i = 1; i <= plazoMeses; i++) {
                Pagare p = new Pagare();
                p.setIdContratoCredito(idContratoCredito);
                p.setNumeroCuota((long) i);
                p.setMontoCuota(cuotaMensual);
                p.setFechaVencimiento(fechaInicio.plusMonths(i - 1));
                p.setEstado(PagareEstado.PENDIENTE);
                p.setVersion(1L);
                pagares.add(pagareRepository.save(p));
            }
            return pagareMapper.toDtoList(pagares);
        } catch (PagareGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PagareGenerationException("Error al generar cronograma de pagarés");
        }
    }

    // Helpers

    private BigDecimal calcularCuotaMensual(BigDecimal monto, BigDecimal tasaAnual, int plazoMeses) {
        if (tasaAnual == null || tasaAnual.compareTo(BigDecimal.ZERO) <= 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(tasaMensual).pow(plazoMeses);
        BigDecimal numerador = monto.multiply(tasaMensual).multiply(factor);
        BigDecimal denominador = factor.subtract(BigDecimal.ONE);
        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    public boolean existenPagaresPorContrato(Long idContratoCredito) {
        try {
            return pagareRepository.existsByIdContratoCredito(idContratoCredito);
        } catch (Exception e) {
            throw new PagareGenerationException("Error al verificar existencia de pagarés");
        }
    }
}
