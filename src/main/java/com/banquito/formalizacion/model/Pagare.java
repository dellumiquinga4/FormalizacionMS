package com.banquito.formalizacion.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.banquito.formalizacion.enums.PagareEstado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pagares")
@Getter
@Setter
@NoArgsConstructor
public class Pagare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagare", nullable = false)
    private Long idPagare;

    @Column(name = "id_contrato_credito", nullable = false)
    private Long idContratoCredito;

    @Column(name = "numero_cuota", nullable = false)
    private Long numeroCuota;

    @Column(name = "monto_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private PagareEstado estado;

    @Version
    private Long version;

    public Pagare(Long idPagare) {
        this.idPagare = idPagare;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pagare that = (Pagare) obj;
        return Objects.equals(idPagare, that.idPagare);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPagare);
    }

    @Override
    public String toString() {
        return "Pagare{" +
                "idPagare=" + idPagare +
                ", idContratoCredito=" + idContratoCredito +
                ", numeroCuota=" + numeroCuota +
                ", montoCuota=" + montoCuota +
                ", fechaVencimiento=" + fechaVencimiento +
                ", estado=" + estado +
                ", version=" + version +
                '}';
    }
} 