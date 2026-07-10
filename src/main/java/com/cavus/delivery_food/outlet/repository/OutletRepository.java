package com.cavus.delivery_food.outlet.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cavus.delivery_food.outlet.entity.Outlet;

@Repository
public interface OutletRepository  extends JpaRepository<Outlet, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<Outlet> findByActiveTrue();
    
}
