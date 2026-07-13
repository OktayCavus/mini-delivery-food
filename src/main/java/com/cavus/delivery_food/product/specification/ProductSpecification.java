package com.cavus.delivery_food.product.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.cavus.delivery_food.product.dto.ProductFilterRequest;
import com.cavus.delivery_food.product.entity.Product;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

/// ! Bu dosyanın amacı dinamik sorgular üretmek.
/// ! her bir filtre ifadesi arttıkça repository'de yeni bir method eklenir. bu istenmez
/// ! bu class sayesinde filtrelere göre dinamik sql sorguları üreteceğiz
public final class ProductSpecification {

    private ProductSpecification() {
    }
    
    public static Specification<Product> withFilter(ProductFilterRequest filter) {
        // ! Root<Product> root: Product tablosu
        // ! CriteriaQuery<Product> query: query'i oluşturmak için gerekli
        // ! CriteriaBuilder cb: query'i oluşturmak için gerekli builder
        // ! root.get('name') => Product tablosundaki name kolonu demek
        return (root, query, cb) -> {
            if (!isCountQuery(query)) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("outlet", JoinType.LEFT);
                query.distinct(true);
            }

            /// ! Where şartlarını predicates listesi tutar
            /// ! price >= 100 bir predicate'dir.
            List<Predicate> predicates = new ArrayList<>();

            // ! filter null ise predicates listesini döndür Select * from products yani
            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
            
            if (StringUtils.hasText(filter.getSearch())) {
                /// ! search filter'ı varsa name ve description'da arar
                /// ! şöyle bir sql sorgusu üretir: SELECT * FROM products WHERE name LIKE '%search%' OR description LIKE '%search%'
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }
            
             if (StringUtils.hasText(filter.getName())) {
                String pattern = "%" + filter.getName().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (StringUtils.hasText(filter.getDescription())) {
                String pattern = "%" + filter.getDescription().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getMinStock() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), filter.getMinStock()));
            }
            if (filter.getMaxStock() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("stock"), filter.getMaxStock()));
            }

            if (filter.getActive() != null) {
                /// ! Sql sorgusu: SELECT * FROM products WHERE active = true
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getCategoryId() != null) {
                /// ! Sql sorgusu: SELECT * FROM products WHERE category_id = ?
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getOutletId() != null) {
                predicates.add(cb.equal(root.get("outlet").get("id"), filter.getOutletId()));
            }

            /// ! belirlenenlerin listesini ve predicate'leri and ile birleştirir
            /* 
             predicates.add(cb.equal(root.get("active"), true));
             predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            predicates.add(cb.like(root.get("name"), "%pizza%")); 
            */
            // ! Where active = true AND category_id = ? AND name LIKE '%pizza%' 
           /// ! bu işe yarıyor
            return cb.and(predicates.toArray(new Predicate[0]));

        };

    }
    
     private static boolean isCountQuery(jakarta.persistence.criteria.CriteriaQuery<?> query) {
        return query.getResultType() == Long.class || query.getResultType() == long.class;
    }
    
}
