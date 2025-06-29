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
public interface ContratoCreditoRepository extends JpaRepository<ContratoCredito, Integer> {

    Optional<ContratoCredito> findByIdSolicitud(Integer idSolicitud);
    
    Optional<ContratoCredito> findByNumeroContratoCore(String numeroContratoCore);
    
    List<ContratoCredito> findByEstado(ContratoCreditoEstado estado);
    
    boolean existsByIdSolicitud(Integer idSolicitud);
    
    boolean existsByNumeroContratoCore(String numeroContratoCore);
  
    Page<ContratoCredito> findByEstado(ContratoCreditoEstado estado, Pageable pageable);
    
    Page<ContratoCredito> findByIdSolicitud(Integer idSolicitud, Pageable pageable);
    
    Page<ContratoCredito> findByNumeroContratoCoreContainingIgnoreCase(String numeroContratoCore, Pageable pageable);

    Page<ContratoCredito> findByEstadoAndNumeroContratoCoreContainingIgnoreCase(
        ContratoCreditoEstado estado, String numeroContratoCore, Pageable pageable);
    
    Page<ContratoCredito> findByEstadoAndIdSolicitud(
        ContratoCreditoEstado estado, Integer idSolicitud, Pageable pageable);
    
    Page<ContratoCredito> findByNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
        String numeroContratoCore, Integer idSolicitud, Pageable pageable);
    
    Page<ContratoCredito> findByEstadoAndNumeroContratoCoreContainingIgnoreCaseAndIdSolicitud(
        ContratoCreditoEstado estado, String numeroContratoCore, Integer idSolicitud, Pageable pageable);
} 