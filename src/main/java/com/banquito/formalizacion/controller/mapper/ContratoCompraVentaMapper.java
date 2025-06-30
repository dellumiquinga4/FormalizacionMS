package com.banquito.formalizacion.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaCreateDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.controller.dto.ContratoCompraVentaUpdateDTO;
import com.banquito.formalizacion.model.ContratoCompraVenta;

import java.util.List;

@Mapper(
        componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContratoCompraVentaMapper {

    // Mapea de modelo a DTO de creación
    @Mapping(target = "idContratoVenta", ignore = true) // Ignora la ID ya que se genera automáticamente
    @Mapping(target = "fechaGeneracion", expression = "java(java.time.LocalDateTime.now())") // Establece fecha actual por defecto
    @Mapping(target = "idSolicitud", expression = "java(12345L)")
    ContratoCompraVenta toEntity(ContratoCompraVentaCreateDTO dto);

    // Mapea de DTO de actualización a modelo, modificando solo los campos necesarios
    @Mapping(target = "idContratoVenta", ignore = true) // La ID no se actualiza
    @Mapping(target = "fechaGeneracion", ignore = true) // La fecha de generación no se actualiza
    void updateEntity(@MappingTarget ContratoCompraVenta entity, ContratoCompraVentaUpdateDTO dto);

    // Mapea de entidad a DTO de respuesta
    ContratoCompraVentaDTO toDTO(ContratoCompraVenta entity);

    // Mapea una lista de entidades a una lista de DTOs
    List<ContratoCompraVentaDTO> toDTOList(List<ContratoCompraVenta> entities);
}
