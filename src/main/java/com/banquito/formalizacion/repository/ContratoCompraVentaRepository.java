package com.banquito.formalizacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.formalizacion.enums.ContratoVentaEstado;
import com.banquito.formalizacion.model.ContratoCompraVenta;

@Repository
public interface ContratoCompraVentaRepository extends JpaRepository<ContratoCompraVenta, Integer> {

    Optional<ContratoCompraVenta> findByIdSolicitud(Integer idSolicitud);
    
    Optional<ContratoCompraVenta> findByNumeroContrato(String numeroContrato);
    
    List<ContratoCompraVenta> findByEstado(ContratoVentaEstado estado);
    
    boolean existsByIdSolicitud(Integer idSolicitud);
    
    boolean existsByNumeroContrato(String numeroContrato);

    Page<ContratoCompraVenta> findByEstado(ContratoVentaEstado estado, Pageable pageable);
    
    Page<ContratoCompraVenta> findByIdSolicitud(Integer idSolicitud, Pageable pageable);
    
    Page<ContratoCompraVenta> findByNumeroContratoContainingIgnoreCase(String numeroContrato, Pageable pageable);

    Page<ContratoCompraVenta> findByEstadoAndNumeroContratoContainingIgnoreCase(
        ContratoVentaEstado estado, String numeroContrato, Pageable pageable);
    
    Page<ContratoCompraVenta> findByEstadoAndIdSolicitud(
        ContratoVentaEstado estado, Integer idSolicitud, Pageable pageable);
    
    Page<ContratoCompraVenta> findByNumeroContratoContainingIgnoreCaseAndIdSolicitud(
        String numeroContrato, Integer idSolicitud, Pageable pageable);
    
    Page<ContratoCompraVenta> findByEstadoAndNumeroContratoContainingIgnoreCaseAndIdSolicitud(
        ContratoVentaEstado estado, String numeroContrato, Integer idSolicitud, Pageable pageable);
} 