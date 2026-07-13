package com.cavus.delivery_food.outlet.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.cavus.delivery_food.outlet.dto.OutletFilterRequest;
import com.cavus.delivery_food.outlet.entity.Outlet;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class OutletSpecification {

    private OutletSpecification() {
    }

    public static Specification<Outlet> withFilter(OutletFilterRequest filter) {
        return (r, q , cb) -> {
       

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        
            if (StringUtils.hasText(filter.getName())) {
                String pattern = "%" + filter.getName().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(r.get("name")), pattern));
            }

            if (StringUtils.hasText(filter.getAddress())) {
                String pattern = "%" + filter.getAddress().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(r.get("address")), pattern));
            }

            if (StringUtils.hasText(filter.getPhone())) {
                predicates.add(cb.equal(r.get("phone"), filter.getPhone()));
            }

            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(cb.equal(cb.lower(r.get("email")), filter.getEmail().toLowerCase()));
            }

            if (filter.getActive() != null) {
                predicates.add(cb.equal(r.get("active"), filter.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}