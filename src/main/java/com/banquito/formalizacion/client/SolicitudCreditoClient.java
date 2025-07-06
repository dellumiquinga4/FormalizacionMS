package com.banquito.formalizacion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// DTO sencillo para ejemplo, puedes personalizarlo según la respuesta real
import com.banquito.formalizacion.controller.dto.SolicitudResumenDTO;

@FeignClient(name = "originacion", url = "${originacion.url}")
public interface SolicitudCreditoClient {

    @GetMapping("/api/v1/solicitudes/{id}/resumen")
    SolicitudResumenDTO obtenerSolicitudPorId(@PathVariable("id") Long id);

}
