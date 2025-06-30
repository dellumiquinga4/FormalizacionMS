package com.banquito.formalizacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.formalizacion.enums.ContratoCreditoEstado;
import com.banquito.formalizacion.model.ContratoCredito;

@Repository
public interface ContratoCreditoRepository extends JpaRepository<ContratoCredito, Long> {

    Optional<ContratoCredito> findByIdSolicitud(Long idSolicitud);
    
    Optional<ContratoCredito> findByNumeroContrato(String numeroContrato);
    
    List<ContratoCredito> findByEstado(ContratoCreditoEstado estado);
    
    boolean existsByIdSolicitud(Long idSolicitud);
    
    boolean existsByNumeroContrato(String numeroContrato);
  
    Page<ContratoCredito> findByEstado(ContratoCreditoEstado estado, Pageable pageable);
    
    Page<ContratoCredito> findByIdSolicitud(Long idSolicitud, Pageable pageable);
    
    Page<ContratoCredito> findByNumeroContratoContainingIgnoreCase(String numeroContrato, Pageable pageable);

    Page<ContratoCredito> findByEstadoAndNumeroContratoContainingIgnoreCase(
        ContratoCreditoEstado estado, String numeroContrato, Pageable pageable);
    
    Page<ContratoCredito> findByEstadoAndIdSolicitud(
        ContratoCreditoEstado estado, Long idSolicitud, Pageable pageable);
    
    Page<ContratoCredito> findByNumeroContratoContainingIgnoreCaseAndIdSolicitud(
        String numeroContrato, Long idSolicitud, Pageable pageable);
    
    Page<ContratoCredito> findByEstadoAndNumeroContratoContainingIgnoreCaseAndIdSolicitud(
        ContratoCreditoEstado estado, String numeroContrato, Long idSolicitud, Pageable pageable);
} 