package com.cavus.delivery_food.outlet.entity;


import java.util.ArrayList;
import java.util.List;

import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.entity.BaseEntity;
import com.cavus.delivery_food.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "outlets")
@Getter
@Setter
@AllArgsConstructor
public class Outlet extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 15)
    private String phone;
    
    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany
    private List<Category> categories = new ArrayList<>();

    @OneToMany
    private List<Product> products = new ArrayList<>();
    
    
}
