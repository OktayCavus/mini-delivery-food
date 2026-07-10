package com.cavus.delivery_food.outlet.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cavus.delivery_food.outlet.dto.OutletRequest;
import com.cavus.delivery_food.outlet.dto.OutletResponse;
import com.cavus.delivery_food.outlet.entity.Outlet;

@Mapper(componentModel = "spring")
public interface OutletMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "products", ignore = true)
    Outlet toEntity(OutletRequest request);



    OutletResponse toResponse(Outlet outlet);

    List<OutletResponse> toOutletResponseList(List<Outlet> outlets);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateOutletFromRequest(OutletRequest request, @MappingTarget Outlet outlet);
    
}
