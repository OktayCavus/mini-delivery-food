package com.cavus.delivery_food.auth.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.cavus.delivery_food.auth.dto.LoginResponse;
import com.cavus.delivery_food.auth.dto.RegisterRequest;
import com.cavus.delivery_food.auth.dto.RegisterResponse;
import com.cavus.delivery_food.auth.entity.Role;
import com.cavus.delivery_food.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "roles", source = "roles" , qualifiedByName = "roleNames")
    RegisterResponse toRegisterResponse(User entity);


    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    LoginResponse toLoginResponse(User entity);


    // Set<Role> → List<String> dönüşümü
    @Named("roleNames")
    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(role -> role.getName())
                .sorted()
                .collect(Collectors.toList());
    }

    
}
