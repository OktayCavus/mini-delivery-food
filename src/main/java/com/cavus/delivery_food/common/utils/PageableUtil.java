package com.cavus.delivery_food.common.utils;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public final class PageableUtil {

    private PageableUtil() {
    }
     

    public static Pageable sanitize(Pageable pageable, Set<String> allowedSortFields, String defaultSortField) {

        /// ! /products?sort=price,desc böyle bir request gelsin
        /// ! sort = price Desc olur

        Sort sort = pageable.getSort();

        /// ! sıralama gönderilmemişse
        if (sort.isUnsorted()) {
            sort = Sort.by(defaultSortField).descending();
        } else {
            // ! boş bir sort oluşturalım
            Sort sanitized = Sort.unsorted();
            /// ! /products?sort=price,desc&sort=name,asc bu şekilde gelirse
            /// ! order dediklerimiz name Asc, price Desc olur
            for (Sort.Order order : sort) {
                /// ! burada bizim izin verdiğimiz alanları kontrol ediyoruz kullanıcı sort'ta her şeyi yollayamaz
                if (allowedSortFields.contains(order.getProperty())) {
                    /// ! order.getProperty() = price
                    /// ! order.getDirection() = DESC
                    sanitized = sanitized.and(Sort.by(order.getDirection(), order.getProperty()));
                }
            }
            /// ! allowedSortFields'a göre gelmediyse false döner oradan çıkar tekrar kontrol edilir
            /// ! sanitized.isUnsorted() = true ise defaultSortField'ı asc olarak setler
            if (sanitized.isUnsorted()) {
                sort = Sort.by(defaultSortField).ascending();
            } else {
                sort = sanitized;

            }
        }
        
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);


    }
    
}
