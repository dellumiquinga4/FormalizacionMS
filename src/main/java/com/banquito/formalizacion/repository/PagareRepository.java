package com.banquito.formalizacion.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.formalizacion.enums.PagareEstado;
import com.banquito.formalizacion.model.Pagare;

@Repository
public interface PagareRepository extends JpaRepository<Pagare, Long> {

    List<Pagare> findByIdContratoCredito(Long idContratoCredito);
    
    List<Pagare> findByIdContratoCreditoOrderByNumeroCuota(Long idContratoCredito);
    
    List<Pagare> findByEstado(PagareEstado estado);
    
    List<Pagare> findByFechaVencimientoBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Pagare> findByFechaVencimientoBeforeAndEstado(LocalDate fecha, PagareEstado estado);
    
    long countByIdContratoCreditoAndEstado(Long idContratoCredito, PagareEstado estado);

    Page<Pagare> findByIdContratoCreditoOrderByNumeroCuota(Long idContratoCredito, Pageable pageable);
    
    Page<Pagare> findByEstado(PagareEstado estado, Pageable pageable);
    
    Page<Pagare> findByFechaVencimientoBeforeAndEstado(LocalDate fecha, PagareEstado estado, Pageable pageable);
    
    Page<Pagare> findByFechaVencimientoBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    void deleteByIdContratoCredito(Long idContratoCredito);

    boolean existsByIdContratoCredito(Long idContratoCredito);

    java.util.Optional<Pagare> findByIdContratoCreditoAndNumeroCuota(Long idContratoCredito, Long numeroCuota);
}