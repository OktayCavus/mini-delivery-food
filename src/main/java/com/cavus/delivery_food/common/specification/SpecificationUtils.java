package com.cavus.delivery_food.common.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    public static <T> Specification<T> unrestricted() {
        return Specification.unrestricted();
    }

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern);
    }

    public static <T> Specification<T> equalIfPresent(String field, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> equalIfPresent(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> equalIgnoreCaseIfPresent(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.toLowerCase();
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), normalized);
    }

    public static <T, Y extends Comparable<? super Y>> Specification<T> greaterThanOrEqualIfPresent(String field, Y value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
    }

    public static <T, Y extends Comparable<? super Y>> Specification<T> lessThanOrEqualIfPresent(String field, Y value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
    }

    public static <T> Specification<T> nestedEqualIfPresent(String joinField, String field, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(joinField).get(field), value);
    }
}
