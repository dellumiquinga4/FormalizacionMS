package com.banquito.formalizacion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;

import com.banquito.formalizacion.enums.ContratoCreditoEstado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contrato_credito")
@Getter
@Setter
@NoArgsConstructor
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_credito", nullable = false)
    private Long idContratoCredito;

    @Column(name = "id_solicitud", nullable = false, unique = true)
    private Long idSolicitud;

    @Column(name = "numero_contrato", nullable = false, unique = true, length = 50)
    private String numeroContrato;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_firma")
    private LocalDateTime fechaFirma;

    @Column(name = "monto_aprobado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAprobado;

    @Column(name = "plazo_final_meses", nullable = false)
    private Long plazoFinalMeses;

    @Column(name = "tasa_efectiva_anual", nullable = false, precision = 5, scale = 2)
    private BigDecimal tasaEfectivaAnual;

    @Column(name = "ruta_archivo_firmado", length = 255)
    private String rutaArchivoFirmado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private ContratoCreditoEstado estado;

    @Version
    private Long version;

    @OneToMany(mappedBy = "idContratoCredito")
    private List<Pagare> pagares;

    public ContratoCredito(Long idContratoCredito) {
        this.idContratoCredito = idContratoCredito;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContratoCredito that = (ContratoCredito) obj;
        return Objects.equals(idContratoCredito, that.idContratoCredito);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idContratoCredito);
    }

    @Override
    public String toString() {
        return "ContratoCredito{" +
                "idContratoCredito=" + idContratoCredito +
                ", idSolicitud=" + idSolicitud +
                ", numeroContrato='" + numeroContrato + '\'' +
                ", fechaGeneracion=" + fechaGeneracion +
                ", fechaFirma=" + fechaFirma +
                ", montoAprobado=" + montoAprobado +
                ", plazoFinalMeses=" + plazoFinalMeses +
                ", tasaEfectivaAnual=" + tasaEfectivaAnual +
                ", rutaArchivoFirmado='" + rutaArchivoFirmado + '\'' +
                ", estado=" + estado +
                ", version=" + version +
                '}';
    }
} 