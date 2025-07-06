package com.banquito.formalizacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.client.SolicitudCreditoClient;
import com.banquito.formalizacion.controller.dto.*;
import com.banquito.formalizacion.controller.mapper.ContratoCreditoMapper;
import com.banquito.formalizacion.controller.mapper.PagareMapper;
import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.enums.PagareEstado;
import com.banquito.formalizacion.exception.ContratoCreditoGenerationException;
import com.banquito.formalizacion.exception.NumeroContratoYaExisteException;
import com.banquito.formalizacion.exception.PagareGenerationException;
import com.banquito.formalizacion.model.ContratoCredito;
import com.banquito.formalizacion.model.Pagare;
import com.banquito.formalizacion.repository.ContratoCreditoRepository;
import com.banquito.formalizacion.repository.PagareRepository;

@Service
public class ContratoCreditoService {

    private final ContratoCreditoRepository contratoCreditoRepository;
    private final PagareRepository pagareRepository;
    private final ContratoCreditoMapper contratoCreditoMapper;
    private final PagareMapper pagareMapper;
    private final SolicitudCreditoClient solicitudCreditoClient;

    public ContratoCreditoService(
        ContratoCreditoRepository contratoCreditoRepository,
        PagareRepository pagareRepository,
        ContratoCreditoMapper contratoCreditoMapper,
        PagareMapper pagareMapper,
        SolicitudCreditoClient solicitudCreditoClient
    ) {
        this.contratoCreditoRepository = contratoCreditoRepository;
        this.pagareRepository = pagareRepository;
        this.contratoCreditoMapper = contratoCreditoMapper;
        this.pagareMapper = pagareMapper;
        this.solicitudCreditoClient = solicitudCreditoClient;
    }

    // -------- CONTRATO CREDITO --------

    @Transactional
    public ContratoCreditoDTO getContratoCreditoById(Long id) {
        ContratoCredito contrato = contratoCreditoRepository.findById(id)
            .orElseThrow(() -> new ContratoCreditoGenerationException("Contrato no encontrado: " + id));
        return contratoCreditoMapper.toDto(contrato);
    }

    @Transactional
    public ContratoCreditoDTO createContratoCredito(ContratoCreditoCreateDTO dto) {
        // 1. Consumir el MS de originación para obtener la solicitud real
        SolicitudResumenDTO solicitud = solicitudCreditoClient.obtenerSolicitudPorId(dto.getIdSolicitud());

        // 3. Validaciones de unicidad
        if (contratoCreditoRepository.existsByIdSolicitud(solicitud.getIdSolicitud())) {
            throw new ContratoCreditoGenerationException("Ya existe un contrato para solicitud " + solicitud.getIdSolicitud());
        }
        if (contratoCreditoRepository.existsByNumeroContrato(dto.getNumeroContrato())) {
            throw new NumeroContratoYaExisteException(dto.getNumeroContrato(), "ContratoCredito");
        }

        // 4. Construir la entidad, pero SOBRESCRIBE los campos con los valores del MS originación
        ContratoCredito contrato = contratoCreditoMapper.toEntity(dto);
        contrato.setIdSolicitud(solicitud.getIdSolicitud());
        contrato.setMontoAprobado(solicitud.getMontoAprobado());
        contrato.setPlazoFinalMeses(solicitud.getPlazoFinalMeses() != null ? solicitud.getPlazoFinalMeses().longValue() : null);
        contrato.setTasaEfectivaAnual(solicitud.getTasaEfectivaAnual());
        contrato.setEstado(ContratoCreditoEstado.PENDIENTE_FIRMA);
        contrato.setVersion(1L);

        // 5. Guardar y retornar el DTO
        ContratoCredito saved = contratoCreditoRepository.save(contrato);
        return contratoCreditoMapper.toDto(saved);
    }

    @Transactional
    public ContratoCreditoDTO updateContratoCredito(Long id, ContratoCreditoUpdateDTO dto) {
        if (!id.equals(dto.getIdContratoCredito())) {
            throw new ContratoCreditoGenerationException("El ID del path no coincide con el del body");
        }
        ContratoCredito existing = contratoCreditoRepository.findById(id)
            .orElseThrow(() -> new ContratoCreditoGenerationException("Contrato no encontrado: " + id));

        contratoCreditoMapper.updateEntity(existing, dto);
        ContratoCredito updated = contratoCreditoRepository.save(existing);
        return contratoCreditoMapper.toDto(updated);
    }

    @Transactional
    public ContratoCreditoDTO logicalDeleteContratoCredito(Long id) {
        ContratoCredito existing = contratoCreditoRepository.findById(id)
            .orElseThrow(() -> new ContratoCreditoGenerationException("Contrato no encontrado: " + id));

        if (ContratoCreditoEstado.ACTIVO.equals(existing.getEstado())) {
            throw new ContratoCreditoGenerationException("El contrato ya está cancelado: " + id);
        }
        existing.setEstado(ContratoCreditoEstado.ACTIVO);
        ContratoCredito saved = contratoCreditoRepository.save(existing);
        return contratoCreditoMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContratoCreditoDTO> findContratosConFiltros(
        ContratoCreditoEstado estado,
        String numeroContrato,
        Long idSolicitud,
        Pageable pageable
    ) {
        Page<ContratoCredito> contratos;
        if (estado != null && numeroContrato != null && idSolicitud != null) {
            contratos = contratoCreditoRepository.findByEstadoAndNumeroContratoContainingIgnoreCaseAndIdSolicitud(
                estado, numeroContrato, idSolicitud, pageable);
        } else if (estado != null && numeroContrato != null) {
            contratos = contratoCreditoRepository.findByEstadoAndNumeroContratoContainingIgnoreCase(
                estado, numeroContrato, pageable);
        } else if (estado != null && idSolicitud != null) {
            contratos = contratoCreditoRepository.findByEstadoAndIdSolicitud(estado, idSolicitud, pageable);
        } else if (numeroContrato != null && idSolicitud != null) {
            contratos = contratoCreditoRepository.findByNumeroContratoContainingIgnoreCaseAndIdSolicitud(
                numeroContrato, idSolicitud, pageable);
        } else if (estado != null) {
            contratos = contratoCreditoRepository.findByEstado(estado, pageable);
        } else if (numeroContrato != null) {
            contratos = contratoCreditoRepository.findByNumeroContratoContainingIgnoreCase(numeroContrato, pageable);
        } else if (idSolicitud != null) {
            contratos = contratoCreditoRepository.findByIdSolicitud(idSolicitud, pageable);
        } else {
            contratos = contratoCreditoRepository.findAll(pageable);
        }
        return contratos.map(contratoCreditoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public boolean existePorSolicitud(Long idSolicitud) {
        return contratoCreditoRepository.existsByIdSolicitud(idSolicitud);
    }

    // -------- PAGARE (Integrado) --------

    @Transactional
    public PagareDTO getPagareById(Long id) {
        Pagare pagare = pagareRepository.findById(id)
            .orElseThrow(() -> new PagareGenerationException("Pagaré no encontrado: " + id));
        return pagareMapper.toDto(pagare);
    }

    @Transactional
    public PagareDTO createPagare(PagareCreateDTO dto) {
        Pagare pagare = pagareMapper.toEntity(dto);
        Pagare saved = pagareRepository.save(pagare);
        return pagareMapper.toDto(saved);
    }

    @Transactional
    public List<PagareDTO> getPagaresByContratoCredito(Long idContratoCredito) {
        var pagares = pagareRepository.findByIdContratoCreditoOrderByNumeroCuota(idContratoCredito);
        return pagareMapper.toDtoList(pagares);
    }

    @Transactional
    public PagareDTO getPagareByContratoAndCuota(Long idContratoCredito, Long numeroCuota) {
        return pagareRepository
            .findByIdContratoCreditoAndNumeroCuota(idContratoCredito, numeroCuota)
            .map(pagareMapper::toDto)
            .orElseThrow(() ->
                new PagareGenerationException(
                    "No se encontró el pagaré para contrato "
                    + idContratoCredito + " y cuota " + numeroCuota
                )
            );
    }

    @Transactional
    public PagareDTO updatePagare(Long id, PagareUpdateDTO dto) {
        if (!id.equals(dto.getIdPagare())) {
            throw new PagareGenerationException("El ID del path no coincide con el del body");
        }
        Pagare existing = pagareRepository.findById(id)
            .orElseThrow(() -> new PagareGenerationException("Pagaré no encontrado: " + id));
        pagareMapper.updateEntity(existing, dto);
        Pagare updated = pagareRepository.save(existing);
        return pagareMapper.toDto(updated);
    }

    @Transactional
    public List<PagareDTO> generarPagaresDesdeParams(
        Long idContratoCredito,
        BigDecimal montoSolicitado,
        BigDecimal tasaAnual,
        int plazoMeses,
        LocalDate fechaInicio
    ) {
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
    }

    @Transactional
    public List<PagareDTO> generarPagaresDesdeContrato(Long idContratoCredito) {
        ContratoCredito contrato = contratoCreditoRepository.findById(idContratoCredito)
            .orElseThrow(() -> new PagareGenerationException("Contrato de crédito no encontrado: " + idContratoCredito));

        BigDecimal montoSolicitado = contrato.getMontoAprobado();
        BigDecimal tasaAnual = contrato.getTasaEfectivaAnual();
        int plazoMeses = contrato.getPlazoFinalMeses().intValue();
        LocalDate fechaInicio = contrato.getFechaGeneracion().toLocalDate(); // Ajusta si tienes un campo específico para fecha de inicio

        return this.generarPagaresDesdeParams(idContratoCredito, montoSolicitado, tasaAnual, plazoMeses, fechaInicio);
    }


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
        return pagareRepository.existsByIdContratoCredito(idContratoCredito);
    }
}
