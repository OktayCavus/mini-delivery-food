package com.cavus.delivery_food.product.mapper;

import com.cavus.delivery_food.product.dto.ProductRequest;
import com.cavus.delivery_food.product.dto.ProductResponse;
import com.cavus.delivery_food.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

// ! SERVICE KATMANINDA KULLANILIR
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /// Category'yi ignore ettik çünkü request içinde UUID olarak categoryId var
    /// Gerçek category nesnesini service katmanında repository ile bulmak gerekiyor
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest productRequest);

    List<Product> toProductList(List<ProductRequest> productRequests);

    /// ProductResponse'a eklediğimiz categoryId ve categoryName alanlarının
    /// nereden doldurulacağını söylüyoruz bu Mapping vasıtasıyla
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toProductResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);

    // @MappingTarget MapStruct'a şunu söyler Yeni object oluşturma, bu mevcut object’i modify et
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateProductFromRequest(ProductRequest request,
                                  @MappingTarget Product product);
}
