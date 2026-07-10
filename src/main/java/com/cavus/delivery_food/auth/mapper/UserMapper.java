package com.cavus.delivery_food.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cavus.delivery_food.auth.dto.LoginResponse;
import com.cavus.delivery_food.auth.dto.RegisterRequest;
import com.cavus.delivery_food.auth.dto.RegisterResponse;
import com.cavus.delivery_food.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toEntity(RegisterRequest request);

    RegisterResponse toRegisterResponse(User entity);


    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    LoginResponse toLoginResponse(User entity);

    
}
