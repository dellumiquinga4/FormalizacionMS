package com.banquito.formalizacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.formalizacion.controller.dto.PagareDTO;
import com.banquito.formalizacion.controller.mapper.PagareMapper;
import com.banquito.formalizacion.enums.PagareEstado;
import com.banquito.formalizacion.exception.InvalidStateException;
import com.banquito.formalizacion.exception.NotFoundException;
import com.banquito.formalizacion.model.ContratoCredito;
import com.banquito.formalizacion.model.Pagare;
import com.banquito.formalizacion.repository.PagareRepository;

@Service
@Transactional
public class PagareService {

    private static final Logger logger = LoggerFactory.getLogger(PagareService.class);

    private final PagareRepository repository;
    private final PagareMapper mapper;

    public PagareService(PagareRepository repository, PagareMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<PagareDTO> findAll(Pageable pageable) {
        try {
            logger.debug("Consultando pagarés con paginación: página {}, tamaño {}", 
                        pageable.getPageNumber(), pageable.getPageSize());
            Page<Pagare> pagares = repository.findAll(pageable);
            return pagares.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar pagarés con paginación", e);
            throw new RuntimeException("Error al consultar pagarés");
        }
    }

    @Transactional(readOnly = true)
    public PagareDTO findById(Integer id) {
        try {
            logger.debug("Consultando pagaré por ID: {}", id);
            Pagare pagare = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id.toString(), "Pagare"));
            return mapper.toDTO(pagare);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al consultar pagaré por ID: {}", id, e);
            throw new RuntimeException("Error al consultar pagaré");
        }
    }

    @Transactional(readOnly = true)
    public List<PagareDTO> findByContratoCredito(Integer idContratoCredito) {
        try {
            logger.debug("Consultando pagarés por contrato de crédito: {}", idContratoCredito);
            List<Pagare> pagares = repository.findByIdContratoCreditoOrderByNumeroCuota(idContratoCredito);
            return pagares.stream().map(mapper::toDTO).toList();
        } catch (Exception e) {
            logger.error("Error al consultar pagarés por contrato: {}", idContratoCredito, e);
            throw new RuntimeException("Error al consultar pagarés por contrato");
        }
    }

    @Transactional(readOnly = true)
    public Page<PagareDTO> findByEstado(PagareEstado estado, Pageable pageable) {
        try {
            logger.debug("Consultando pagarés por estado: {} con paginación", estado);
            Page<Pagare> pagares = repository.findByEstado(estado, pageable);
            return pagares.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar pagarés por estado: {}", estado, e);
            throw new RuntimeException("Error al consultar pagarés por estado");
        }
    }

    @Transactional(readOnly = true)
    public Page<PagareDTO> findByContratoCredito(Integer idContratoCredito, Pageable pageable) {
        try {
            logger.debug("Consultando pagarés por contrato de crédito: {} con paginación", idContratoCredito);
            Page<Pagare> pagares = repository.findByIdContratoCreditoOrderByNumeroCuota(idContratoCredito, pageable);
            return pagares.map(mapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al consultar pagarés por contrato con paginación: {}", idContratoCredito, e);
            throw new RuntimeException("Error al consultar pagarés por contrato");
        }
    }

    @Transactional(readOnly = true)
    public List<PagareDTO> findPagaresVencidos() {
        try {
            LocalDate fechaActual = LocalDate.now();
            List<Pagare> pagares = repository.findByFechaVencimientoBeforeAndEstado(fechaActual, PagareEstado.PENDIENTE);
            return pagares.stream().map(mapper::toDTO).toList();
        } catch (Exception e) {
            logger.error("Error al consultar pagarés vencidos", e);
            throw new RuntimeException("Error al consultar pagarés vencidos");
        }
    }

    @Transactional(readOnly = true)
    public List<PagareDTO> findPagaresPorVencer(int diasAntes) {
        try {
            LocalDate fechaInicio = LocalDate.now();
            LocalDate fechaFin = fechaInicio.plusDays(diasAntes);
            List<Pagare> pagares = repository.findByFechaVencimientoBetween(fechaInicio, fechaFin);
            return pagares.stream().map(mapper::toDTO).toList();
        } catch (Exception e) {
            logger.error("Error al consultar pagarés por vencer", e);
            throw new RuntimeException("Error al consultar pagarés por vencer");
        }
    }

    public List<PagareDTO> generarPagares(ContratoCredito contrato) {
        try {
            logger.info("Generando pagarés para contrato de crédito ID: {}", contrato.getIdContratoCredito());
            
            List<Pagare> pagares = new ArrayList<>();
            BigDecimal montoCuota = calcularCuotaMensual(
                contrato.getMontoAprobado(), 
                contrato.getTasaEfectivaAnual(), 
                contrato.getPlazoFinalMeses()
            );

            LocalDate fechaBase = contrato.getFechaGeneracion().toLocalDate().plusMonths(1);
            
            for (int i = 1; i <= contrato.getPlazoFinalMeses(); i++) {
                Pagare pagare = new Pagare();
                pagare.setIdContratoCredito(contrato.getIdContratoCredito());
                pagare.setNumeroCuota(i);
                pagare.setMontoCuota(montoCuota);
                pagare.setFechaVencimiento(ajustarADiaLaborable(fechaBase.plusMonths(i - 1)));
                pagare.setEstado(PagareEstado.PENDIENTE);
                pagare.setVersion(1L);
                
                pagares.add(pagare);
            }

            List<Pagare> pagaresGuardados = repository.saveAll(pagares);
            logger.info("Se generaron {} pagarés para el contrato ID: {}", 
                       pagaresGuardados.size(), contrato.getIdContratoCredito());
            
            return pagaresGuardados.stream().map(mapper::toDTO).toList();
        } catch (Exception e) {
            logger.error("Error al generar pagarés para contrato: {}", contrato.getIdContratoCredito(), e);
            throw new RuntimeException("Error al generar pagarés");
        }
    }

    public PagareDTO registrarPago(Integer idPagare) {
        try {
            logger.info("Registrando pago de pagaré ID: {}", idPagare);
            
            Pagare pagare = repository.findById(idPagare)
                    .orElseThrow(() -> new NotFoundException(idPagare.toString(), "Pagare"));
            
            if (pagare.getEstado() != PagareEstado.PENDIENTE) {
                throw new InvalidStateException(
                    pagare.getEstado().toString(), 
                    PagareEstado.PAGADO.toString(), 
                    "Pagare"
                );
            }

            pagare.setEstado(PagareEstado.PAGADO);
            pagare.setVersion(pagare.getVersion() + 1);

            Pagare pagareActualizado = repository.save(pagare);
            logger.info("Pago registrado exitosamente para pagaré ID: {}", idPagare);
            
            return mapper.toDTO(pagareActualizado);
        } catch (NotFoundException | InvalidStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al registrar pago de pagaré ID: {}", idPagare, e);
            throw new RuntimeException("Error al registrar pago de pagaré");
        }
    }

    public List<PagareDTO> marcarComoVencidos() {
        try {
            logger.info("Marcando pagarés vencidos");
            
            LocalDate fechaActual = LocalDate.now();
            List<Pagare> pagaresVencidos = repository.findByFechaVencimientoBeforeAndEstado(fechaActual, PagareEstado.PENDIENTE);
            
            for (Pagare pagare : pagaresVencidos) {
                pagare.setEstado(PagareEstado.VENCIDO);
                pagare.setVersion(pagare.getVersion() + 1);
            }

            List<Pagare> pagaresActualizados = repository.saveAll(pagaresVencidos);
            logger.info("Se marcaron {} pagarés como vencidos", pagaresActualizados.size());
            
            return pagaresActualizados.stream().map(mapper::toDTO).toList();
        } catch (Exception e) {
            logger.error("Error al marcar pagarés como vencidos", e);
            throw new RuntimeException("Error al marcar pagarés como vencidos");
        }
    }

    @Transactional(readOnly = true)
    public long contarPagaresPendientesPorContrato(Integer idContratoCredito) {
        try {
            return repository.countByIdContratoCreditoAndEstado(idContratoCredito, PagareEstado.PENDIENTE);
        } catch (Exception e) {
            logger.error("Error al contar pagarés pendientes por contrato: {}", idContratoCredito, e);
            throw new RuntimeException("Error al contar pagarés pendientes");
        }
    }

    @Transactional(readOnly = true)
    public long contarPagaresVencidosPorContrato(Integer idContratoCredito) {
        try {
            return repository.countByIdContratoCreditoAndEstado(idContratoCredito, PagareEstado.VENCIDO);
        } catch (Exception e) {
            logger.error("Error al contar pagarés vencidos por contrato: {}", idContratoCredito, e);
            throw new RuntimeException("Error al contar pagarés vencidos");
        }
    }

    private BigDecimal calcularCuotaMensual(BigDecimal monto, BigDecimal tasaAnual, Integer plazoMeses) {
        if (tasaAnual.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(tasaMensual).pow(plazoMeses);
        BigDecimal numerador = monto.multiply(tasaMensual).multiply(factor);
        BigDecimal denominador = factor.subtract(BigDecimal.ONE);
        
        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private LocalDate ajustarADiaLaborable(LocalDate fecha) {
        // Si cae en sábado, mover al viernes anterior
        if (fecha.getDayOfWeek().getValue() == 6) {
            return fecha.minusDays(1);
        }
        // Si cae en domingo, mover al lunes siguiente
        if (fecha.getDayOfWeek().getValue() == 7) {
            return fecha.plusDays(1);
        }
        return fecha;
    }
} 