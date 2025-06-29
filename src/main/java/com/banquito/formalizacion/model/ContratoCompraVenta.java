package com.banquito.formalizacion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.banquito.formalizacion.enums.ContratoVentaEstado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contrato_compra_venta")
@Getter
@Setter
@NoArgsConstructor
public class ContratoCompraVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_venta", nullable = false)
    private Integer idContratoVenta;

    @Column(name = "id_solicitud", nullable = false, unique = true)
    private Integer idSolicitud;

    @Column(name = "numero_contrato", nullable = false, unique = true, length = 50)
    private String numeroContrato;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_firma")
    private LocalDateTime fechaFirma;

    @Column(name = "precio_final_vehiculo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioFinalVehiculo;

    @Column(name = "ruta_archivo_firmado", length = 255)
    private String rutaArchivoFirmado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private ContratoVentaEstado estado;

    @Column(name = "version", nullable = false)
    private Long version;

    public ContratoCompraVenta(Integer idContratoVenta) {
        this.idContratoVenta = idContratoVenta;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContratoCompraVenta that = (ContratoCompraVenta) obj;
        return Objects.equals(idContratoVenta, that.idContratoVenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idContratoVenta);
    }

    @Override
    public String toString() {
        return "ContratoCompraVenta{" +
                "idContratoVenta=" + idContratoVenta +
                ", idSolicitud=" + idSolicitud +
                ", numeroContrato='" + numeroContrato + '\'' +
                ", fechaGeneracion=" + fechaGeneracion +
                ", fechaFirma=" + fechaFirma +
                ", precioFinalVehiculo=" + precioFinalVehiculo +
                ", rutaArchivoFirmado='" + rutaArchivoFirmado + '\'' +
                ", estado=" + estado +
                ", version=" + version +
                '}';
    }
} 