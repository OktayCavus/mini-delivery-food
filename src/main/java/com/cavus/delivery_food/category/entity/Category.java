package com.cavus.delivery_food.category.entity;

import com.cavus.delivery_food.entity.BaseEntity;
import com.cavus.delivery_food.outlet.entity.Outlet;
import com.cavus.delivery_food.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    /// @OneToMany(mappedBy = "category")` demek,
    /// "Bu ilişkinin asıl foreign key tarafı Product içindeki `category` alanıdır" demektir.
    //  Veritabanında `categories` tablosunda `product_id` tutulmaz.
    /// Bu liste veritabanında ayrı bir kolon değildir.
    /// `categories` tablosunda `products` diye bir kolon oluşmaz. İlişki yine `products.category_id` üzerinden kurulur.
    /// Bu ilişkiyi ben yönetmiyorum. Karşı tarafta, yani `Product` sınıfında adı `category` olan field yönetiyor.
    /// Burada One ifadesi annotation'un yazıldığı sınıfı (Category) Many ifadesi ilişkili olduğu sınıfı (Product) temsil eder.
    /// Product.java dosyasında ManyToOne annotation'u ile Category.java dosyasında OneToMany annotation'u birbirini karşılıyor.
    /// JoinColumn annotation'u ise foreign key'i belirtir.
    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id", nullable = false)
    private Outlet outlet;


}
