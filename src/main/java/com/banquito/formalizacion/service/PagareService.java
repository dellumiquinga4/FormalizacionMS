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

    public PagareService(PagareRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Pagare> findAll(Pageable pageable) {
        logger.debug("Consultando pagarés con paginación: página {}, tamaño {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Pagare> findAll() {
        logger.debug("Consultando todos los pagarés - USAR CON PRECAUCIÓN");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Pagare findById(Integer id) {
        logger.debug("Consultando pagaré por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "Pagare"));
    }

    @Transactional(readOnly = true)
    public List<Pagare> findByContratoCredito(Integer idContratoCredito) {
        logger.debug("Consultando pagarés por contrato de crédito: {}", idContratoCredito);
        return repository.findByIdContratoCreditoOrderByNumeroCuota(idContratoCredito);
    }

    @Transactional(readOnly = true)
    public List<Pagare> findByEstado(PagareEstado estado) {
        logger.debug("Consultando pagarés por estado: {}", estado);
        return repository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Page<Pagare> findByEstado(PagareEstado estado, Pageable pageable) {
        logger.debug("Consultando pagarés por estado: {} con paginación", estado);
        return repository.findByEstado(estado, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pagare> findByContratoCredito(Integer idContratoCredito, Pageable pageable) {
        logger.debug("Consultando pagarés por contrato de crédito: {} con paginación", idContratoCredito);
        return repository.findByIdContratoCreditoOrderByNumeroCuota(idContratoCredito, pageable);
    }

    @Transactional(readOnly = true)
    public List<Pagare> findPagaresVencidos() {
        LocalDate fechaActual = LocalDate.now();
        return repository.findByFechaVencimientoBeforeAndEstado(fechaActual, PagareEstado.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<Pagare> findPagaresPorVencer(int diasAntes) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(diasAntes);
        return repository.findByFechaVencimientoBetween(fechaInicio, fechaFin);
    }

    public List<Pagare> generarPagares(ContratoCredito contrato) {
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
        
        return pagaresGuardados;
    }

    public Pagare registrarPago(Integer idPagare) {
        logger.info("Registrando pago de pagaré ID: {}", idPagare);
        
        Pagare pagare = findById(idPagare);
        
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
        
        return pagareActualizado;
    }

    public List<Pagare> marcarComoVencidos() {
        logger.info("Marcando pagarés vencidos");
        
        List<Pagare> pagaresVencidos = findPagaresVencidos();
        
        for (Pagare pagare : pagaresVencidos) {
            pagare.setEstado(PagareEstado.VENCIDO);
            pagare.setVersion(pagare.getVersion() + 1);
        }

        List<Pagare> pagaresActualizados = repository.saveAll(pagaresVencidos);
        logger.info("Se marcaron {} pagarés como vencidos", pagaresActualizados.size());
        
        return pagaresActualizados;
    }

    @Transactional(readOnly = true)
    public long contarPagaresPendientesPorContrato(Integer idContratoCredito) {
        return repository.countByIdContratoCreditoAndEstado(idContratoCredito, PagareEstado.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public long contarPagaresVencidosPorContrato(Integer idContratoCredito) {
        return repository.countByIdContratoCreditoAndEstado(idContratoCredito, PagareEstado.VENCIDO);
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