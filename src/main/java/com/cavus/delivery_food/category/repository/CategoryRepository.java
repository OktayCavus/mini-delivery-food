package com.cavus.delivery_food.category.repository;

import com.cavus.delivery_food.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOutletIdAndActiveTrue(UUID outletId);

    boolean existsByNameIgnoreCaseAndOutletId(String name, UUID outletId);

}
