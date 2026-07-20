package com.cavus.delivery_food.product.specification;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.cavus.delivery_food.common.specification.SpecificationUtils;
import com.cavus.delivery_food.product.dto.ProductFilterRequest;
import com.cavus.delivery_food.product.entity.Product;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;

/// ! Bu dosyanın amacı dinamik sorgular üretmek.
/// ! her bir filtre ifadesi arttıkça repository'de yeni bir method eklenir. bu istenmez
/// ! bu class sayesinde filtrelere göre dinamik sql sorguları üreteceğiz
public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> withFilter(ProductFilterRequest filter) {
        if (filter == null) {
            return withFetches();
        }

        return withFetches()
                .and(search(filter.getSearch()))
                .and(nameLike(filter.getName()))
                .and(descriptionLike(filter.getDescription()))
                .and(minPrice(filter.getMinPrice()))
                .and(maxPrice(filter.getMaxPrice()))
                .and(minStock(filter.getMinStock()))
                .and(maxStock(filter.getMaxStock()))
                .and(activeEquals(filter.getActive()))
                .and(categoryIdEquals(filter.getCategoryId()))
                .and(outletIdEquals(filter.getOutletId()));
    }

    private static Specification<Product> withFetches() {
        return (root, query, cb) -> {
            if (!isCountQuery(query)) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("outlet", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    /// ! search filter'ı varsa name ve description'da arar
    /// ! şöyle bir sql sorgusu üretir: SELECT * FROM products WHERE name LIKE '%search%' OR description LIKE '%search%'
    private static Specification<Product> search(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> {
            var nameMatch = cb.like(cb.lower(root.get("name")), pattern);
            var descMatch = cb.like(cb.lower(root.get("description")), pattern);
            return cb.or(nameMatch, descMatch);
        };
    }

    private static Specification<Product> nameLike(String name) {
        return SpecificationUtils.likeIgnoreCase("name", name);
    }

    private static Specification<Product> descriptionLike(String description) {
        return SpecificationUtils.likeIgnoreCase("description", description);
    }

    private static Specification<Product> minPrice(BigDecimal minPrice) {
        return SpecificationUtils.greaterThanOrEqualIfPresent("price", minPrice);
    }

    private static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return SpecificationUtils.lessThanOrEqualIfPresent("price", maxPrice);
    }

    private static Specification<Product> minStock(Integer minStock) {
        return SpecificationUtils.greaterThanOrEqualIfPresent("stock", minStock);
    }

    private static Specification<Product> maxStock(Integer maxStock) {
        return SpecificationUtils.lessThanOrEqualIfPresent("stock", maxStock);
    }

    private static Specification<Product> activeEquals(Boolean active) {
        return SpecificationUtils.equalIfPresent("active", active);
    }

    private static Specification<Product> categoryIdEquals(UUID categoryId) {
        return SpecificationUtils.nestedEqualIfPresent("category", "id", categoryId);
    }

    private static Specification<Product> outletIdEquals(UUID outletId) {
        return SpecificationUtils.nestedEqualIfPresent("outlet", "id", outletId);
    }

    /// ! isCountQuery fetch join kullanan specification’lara özel bir koruma sağlar.
    private static boolean isCountQuery(CriteriaQuery<?> query) {
        return query.getResultType() == Long.class || query.getResultType() == long.class;
    }
}
