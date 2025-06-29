package com.banquito.formalizacion.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.formalizacion.controller.dto.PagareDTO;
import com.banquito.formalizacion.model.Pagare;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PagareMapper {

    PagareDTO toDTO(Pagare model);
    
    Pagare toModel(PagareDTO dto);
} 