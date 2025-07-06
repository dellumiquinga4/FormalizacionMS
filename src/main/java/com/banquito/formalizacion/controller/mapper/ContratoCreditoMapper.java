package com.banquito.formalizacion.controller.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.banquito.formalizacion.controller.dto.ContratoCreditoCreateDTO;
import com.banquito.formalizacion.controller.dto.ContratoCreditoDTO;
import com.banquito.formalizacion.controller.dto.ContratoCreditoUpdateDTO;
import com.banquito.formalizacion.model.ContratoCredito;

@Mapper(
        componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContratoCreditoMapper {

    // Mapea de modelo a DTO para creación (dto a entidad)
    @Mapping(target = "idContratoCredito", ignore = true)  // No mapeamos el ID, ya que es autogenerado
    @Mapping(target = "fechaGeneracion", expression = "java(java.time.LocalDateTime.now())") // Establece la fecha actual por defecto
    @Mapping(target = "fechaFirma", ignore = true) 
    @Mapping(target = "version", ignore = true) // No mapeamos la versión, la dejamos como está en la base de datos
    ContratoCredito toEntity(ContratoCreditoCreateDTO dto);

    // Mapea la entidad a DTO para actualización
    @Mapping(target = "idContratoCredito", ignore = true)
    @Mapping(target = "idSolicitud", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget ContratoCredito entity, ContratoCreditoUpdateDTO dto);

    // Mapea la entidad a DTO de respuesta
    ContratoCreditoDTO toDto(ContratoCredito entity);

    // Mapea una lista de entidades a una lista de DTOs
    List<ContratoCreditoDTO> toDtoList(List<ContratoCredito> entities);

}