package com.cavus.delivery_food.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cavus.delivery_food.auth.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Permission findByName(String name);
}
