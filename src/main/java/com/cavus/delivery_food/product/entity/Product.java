package com.cavus.delivery_food.product.entity;



import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.common.entity.BaseEntity;
import com.cavus.delivery_food.outlet.entity.Outlet;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_outlet", columnList = "outlet_id"),
    @Index(name = "idx_product_category", columnList = "category_id")
})
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    private Boolean active = true;

    /// Category Product ilişkisinde FK'yı product tutar @JoinColumn ifadesi buraya yazılır. Ne demektir category tablosuyla product tablosunu product içindeki category_id ile ilişkilendirir.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id", nullable = false)
    private Outlet outlet;
}

