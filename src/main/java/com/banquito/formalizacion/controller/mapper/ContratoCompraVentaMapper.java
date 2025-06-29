package com.banquito.formalizacion.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.formalizacion.controller.dto.ContratoCompraVentaDTO;
import com.banquito.formalizacion.model.ContratoCompraVenta;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContratoCompraVentaMapper {

    ContratoCompraVentaDTO toDTO(ContratoCompraVenta model);
    
    ContratoCompraVenta toModel(ContratoCompraVentaDTO dto);
} 