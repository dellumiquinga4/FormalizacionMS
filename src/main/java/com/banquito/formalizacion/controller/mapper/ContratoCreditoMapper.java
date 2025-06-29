package com.banquito.formalizacion.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.formalizacion.controller.dto.ContratoCreditoDTO;
import com.banquito.formalizacion.model.ContratoCredito;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContratoCreditoMapper {

    ContratoCreditoDTO toDTO(ContratoCredito model);
    
    ContratoCredito toModel(ContratoCreditoDTO dto);
} 