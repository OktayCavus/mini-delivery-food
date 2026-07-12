package com.cavus.delivery_food.auth.entity;

import com.cavus.delivery_food.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
}
