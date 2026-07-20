package com.cavus.delivery_food.outlet.specification;

import org.springframework.data.jpa.domain.Specification;

import com.cavus.delivery_food.common.specification.SpecificationUtils;
import com.cavus.delivery_food.outlet.dto.OutletFilterRequest;
import com.cavus.delivery_food.outlet.entity.Outlet;

public final class OutletSpecification {

    private OutletSpecification() {
    }

    public static Specification<Outlet> withFilter(OutletFilterRequest filter) {
        if (filter == null) {
            return SpecificationUtils.unrestricted();
        }

        // ! unrestricted() = where() gibi düşünülebilir
        return SpecificationUtils.<Outlet>unrestricted()
                .and(nameLike(filter.getName()))
                .and(addressLike(filter.getAddress()))
                .and(phoneEquals(filter.getPhone()))
                .and(emailEquals(filter.getEmail()))
                .and(activeEquals(filter.getActive()));
    }

    private static Specification<Outlet> nameLike(String name) {
        return SpecificationUtils.likeIgnoreCase("name", name);
    }

    private static Specification<Outlet> addressLike(String address) {
        return SpecificationUtils.likeIgnoreCase("address", address);
    }

    private static Specification<Outlet> phoneEquals(String phone) {
        return SpecificationUtils.equalIfPresent("phone", phone);
    }

    private static Specification<Outlet> emailEquals(String email) {
        return SpecificationUtils.equalIgnoreCaseIfPresent("email", email);
    }

    private static Specification<Outlet> activeEquals(Boolean active) {
        return SpecificationUtils.equalIfPresent("active", active);
    }
}
