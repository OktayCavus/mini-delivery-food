# Faz 3: Basket ve Order Sistemi

**Proje:** Mini Food Delivery Backend  
**Faz:** N fazdan 3.  
**Odak:** Basket, BasketItem, Order, OrderItem ve Product ilişkileri  
**Bu fazda:** Kullanıcı sepete ürün ekleyebilir, sepetini görüntüleyebilir, ürünü sepetten çıkarabilir ve sepetini siparişe dönüştürebilir.  
**Henüz tam yok:** Ödeme, restoran, kurye, sipariş durumu operasyonları

> Bu rehber, Faz 1'de kurulan Product CRUD yapısının ve Faz 2'de eklenen Category ilişkilerinin üzerine inşa edilir. Artık sistem sadece ürün listeleyen bir backend olmaktan çıkıp, gerçek bir yemek sipariş uygulamasının çekirdeğine yaklaşır.

> **Auth sistemi hazır:** Projede `User`, register/login, JWT üretimi, `JwtAuthenticationFilter` ve `SecurityConfig` zaten uygulanmış durumda. Bu fazda sepet ve sipariş endpoint'leri doğrudan giriş yapmış kullanıcıya bağlanır; `userIdentifier` query param yaklaşımı kullanılmaz. Auth detayları için bkz. [spring-security-learning-guide.md](./spring-security-learning-guide.md).

> **Dokümantasyon kaynağı:** Bu rehberdeki repository query method, transaction ve JPA yaklaşımı Context7 üzerinden çekilen güncel **Spring Data JPA** dokümantasyonuna dayandırılmıştır: `/spring-projects/spring-data-jpa`. Auth entegrasyonundaki Spring Security önerileri Context7 üzerinden çekilen **Spring Security 6.5** dokümantasyonuna dayandırılmıştır: `/websites/spring_io_spring-security_reference_6_5`.

> **Bu fazı nasıl okumalısınız?** Sepet ve sipariş sistemi sadece "iki tablo daha ekleyelim" konusu değildir. Bu fazda asıl öğrenmeniz gereken şey, değişken veriyi kalıcı iş kaydına dönüştürmektir. Sepet değişebilir; sipariş ise geçmiş kaydıdır.

---

## İçindekiler

1. [Bu Fazın Amacı](#1-bu-fazın-amacı)
2. [Gerçek Dünyada Basket ve Order Akışı](#2-gerçek-dünyada-basket-ve-order-akışı)
3. [Bu Fazda İnşa Edilecek Yapı](#3-bu-fazda-inşa-edilecek-yapı)
4. [Entity Tasarımı ve İlişkiler](#4-entity-tasarımı-ve-ilişkiler)
5. [Veritabanı Şeması](#5-veritabanı-şeması)
6. [Adım Adım Uygulama](#6-adım-adım-uygulama)
7. [API Endpoint'leri](#7-api-endpointleri)
8. [Business Logic Açıklaması](#8-business-logic-açıklaması)
9. [Service Layer Tasarımı](#9-service-layer-tasarımı)
10. [DTO ve Mapper Tasarımı](#10-dto-ve-mapper-tasarımı)
11. [Exception Handling Tasarımı](#11-exception-handling-tasarımı)
12. [Mevcut Auth Sistemi ile Entegrasyon](#12-mevcut-auth-sistemi-ile-entegrasyon)
13. [Projeyi Gerçek Dünya Projesi Gibi Geliştirme Önerileri](#13-projeyi-gerçek-dünya-projesi-gibi-geliştirme-önerileri)
14. [Yaygın Hatalar](#14-yaygın-hatalar)
15. [Bu Fazda Neler Öğreneceksiniz](#15-bu-fazda-neler-öğreneceksiniz)
16. [Uygulama Checklist'i](#16-uygulama-checklisti)

---

## 1. Bu Fazın Amacı

Faz 1'de `Product` CRUD yapısını kurdunuz. Faz 2'de `Category` ekleyerek ürünleri kategorilere bağladınız. Faz 3'te artık kullanıcı davranışını modellemeye başlıyorsunuz.

Bu fazın ana hedefleri:

- `Basket` entity'si ile kullanıcının geçici alışveriş alanını oluşturmak
- `BasketItem` entity'si ile sepetteki ürünleri ve adetlerini tutmak
- `Order` entity'si ile tamamlanmış sipariş kaydı oluşturmak
- `OrderItem` entity'si ile sipariş anındaki ürün bilgilerini sabitlemek
- `Basket -> BasketItem` ilişkisinde `@OneToMany` mantığını öğrenmek
- `Order -> OrderItem` ilişkisinde kalıcı geçmiş kaydı mantığını öğrenmek
- Sepet toplamını hesaplamak
- Sepeti siparişe dönüştürürken transaction yönetimini anlamak
- Mevcut JWT auth sistemine bağlı sepet ve sipariş akışı kurmak

> **Bu fazın kuralı:** Sepet değişebilir, sipariş değişmemelidir. Kullanıcı sepete ürün ekleyip çıkarabilir. Fakat sipariş oluşturulduktan sonra o sipariş, oluşturulduğu andaki fiyat ve ürün bilgilerini saklamalıdır.

---

## 2. Gerçek Dünyada Basket ve Order Akışı

Bir yemek sipariş uygulamasında kullanıcı genelde şu adımları izler:

1. Ürünleri listeler.
2. Bir ürünü sepete ekler.
3. Aynı üründen birkaç adet ekleyebilir.
4. Sepette ürün adedini artırabilir veya azaltabilir.
5. Sepet toplamını görür.
6. Sipariş oluşturur.
7. Sipariş oluşturulduktan sonra sepet boşaltılır.
8. Kullanıcı geçmiş siparişlerini görüntüler.

Bu akışta iki farklı kavram vardır:

| Kavram     | Anlamı                       | Değişebilir mi? |
| ---------- | ---------------------------- | --------------- |
| Basket     | Kullanıcının aktif sepeti    | Evet            |
| BasketItem | Sepetteki ürün satırı        | Evet            |
| Order      | Tamamlanmış sipariş          | Normalde hayır  |
| OrderItem  | Sipariş anındaki ürün satırı | Normalde hayır  |

Örnek gerçek dünya senaryosu:

| İşlem                          | Basket durumu              | Order durumu        |
| ------------------------------ | -------------------------- | ------------------- |
| Kullanıcı pizza ekledi         | Sepette 1 pizza var        | Sipariş yok         |
| Kullanıcı kola ekledi          | Sepette pizza + kola var   | Sipariş yok         |
| Kullanıcı pizzayı 2 adet yaptı | Sepette 2 pizza + kola var | Sipariş yok         |
| Kullanıcı sipariş oluşturdu    | Sepet boşaltıldı           | Yeni sipariş oluştu |

Önemli ayrım şudur:

- Sepette ürün fiyatı güncel `Product.price` üzerinden hesaplanabilir.
- Siparişte ürün fiyatı sipariş anında kopyalanmalıdır.

Neden?

Bugün pizza 250 TL iken kullanıcı sipariş verdi. Yarın pizza 300 TL olursa geçmiş siparişin toplamı değişmemelidir. Bu yüzden `OrderItem` içinde `unitPrice` ve `lineTotal` tutulur.

---

## 3. Bu Fazda İnşa Edilecek Yapı

Mevcut projede `product` ve `category` paketleri domain bazlı ayrılmış durumda. Aynı stili korumak için yeni paketleri de domain bazlı ekleyin:

```text
com.cavus.delivery_food
├── auth          ← mevcut (register, login, JWT)
├── basket
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
├── order
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
├── product
├── category
├── common
├── config
└── entity
```

Bu fazda yeni domain'ler:

| Domain    | Sorumluluk                                           |
| --------- | ---------------------------------------------------- |
| `basket`  | Aktif sepeti ve sepet ürünlerini yönetir             |
| `order`   | Sipariş oluşturma ve sipariş geçmişini yönetir       |
| `product` | Ürün bilgisini sağlar                                |
| `auth`    | Kullanıcı kaydı, login ve JWT doğrulama (mevcut)    |

Genel akış:

```mermaid
flowchart TD
    A[Client] --> B[BasketController]
    B --> C[BasketService]
    C --> D[BasketRepository]
    C --> E[ProductService veya ProductRepository]
    C --> F[BasketItemRepository]

    A --> G[OrderController]
    G --> H[OrderService]
    H --> C
    H --> I[OrderRepository]
    H --> J[OrderItemRepository]
```

---

## 4. Entity Tasarımı ve İlişkiler

Bu fazda dört yeni entity eklenir:

- `Basket`
- `BasketItem`
- `Order`
- `OrderItem`

`Product` zaten mevcut olduğu için tekrar oluşturulmaz. Sadece yeni entity'ler `Product` ile ilişki kurar.

### 4.1 Genel ilişki diyagramı

```mermaid
erDiagram
    USER ||--o{ BASKET : owns
    USER ||--o{ ORDERS : places
    BASKET ||--o{ BASKET_ITEM : contains
    PRODUCT ||--o{ BASKET_ITEM : added_as

    ORDERS ||--o{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : ordered_as

    USER {
        uuid id
        string email
        string firstName
        string lastName
        string role
    }

    BASKET {
        uuid id
        uuid user_id
        decimal totalPrice
        boolean active
        datetime createdAt
        datetime updatedAt
    }

    BASKET_ITEM {
        uuid id
        uuid basket_id
        uuid product_id
        int quantity
        decimal unitPrice
        decimal lineTotal
    }

    ORDERS {
        uuid id
        uuid user_id
        decimal totalPrice
        string status
        datetime orderedAt
    }

    ORDER_ITEM {
        uuid id
        uuid order_id
        uuid product_id
        string productName
        int quantity
        decimal unitPrice
        decimal lineTotal
    }
```

> `Order` bazı veritabanlarında reserved keyword olabilir. Bu yüzden tablo adı olarak `orders` kullanmak daha güvenlidir.

### 4.2 Basket entity

`Basket`, kullanıcının aktif sepetini temsil eder.

Önerilen alanlar:

| Field        | Type               | Açıklama                                               |
| ------------ | ------------------ | ------------------------------------------------------ |
| `id`         | `UUID`             | `BaseEntity` üzerinden gelir                           |
| `user`       | `User`             | Sepetin sahibi (`auth.entity.User`)                    |
| `items`      | `List<BasketItem>` | Sepetteki ürün satırları                               |
| `totalPrice` | `BigDecimal`       | Sepet toplamı                                          |
| `active`     | `Boolean`          | Sepet aktif mi                                         |

Örnek entity:

```java
@Getter
@Setter
@Entity
@Table(name = "baskets")
public class Basket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BasketItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;
}
```

Buradaki önemli noktalar:

- `mappedBy = "basket"` ilişkiyi `BasketItem.basket` alanının yönettiğini söyler.
- `cascade = CascadeType.ALL` basket kaydedildiğinde item'ların da kaydedilmesini sağlar.
- `orphanRemoval = true` listeden çıkarılan item'ın veritabanından da silinmesini sağlar.

### 4.3 BasketItem entity

`BasketItem`, sepetteki tek bir ürün satırını temsil eder.

Önerilen alanlar:

| Field       | Type         | Açıklama                          |
| ----------- | ------------ | --------------------------------- |
| `id`        | `UUID`       | `BaseEntity` üzerinden gelir      |
| `basket`    | `Basket`     | Hangi sepete ait                  |
| `product`   | `Product`    | Hangi ürün sepete eklendi         |
| `quantity`  | `Integer`    | Kaç adet                          |
| `unitPrice` | `BigDecimal` | Sepete eklendiği anda ürün fiyatı |
| `lineTotal` | `BigDecimal` | `unitPrice * quantity`            |

Örnek entity:

```java
@Getter
@Setter
@Entity
@Table(
    name = "basket_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"basket_id", "product_id"})
)
public class BasketItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;
}
```

`uniqueConstraints` neden önemli?

Aynı sepette aynı ürün iki ayrı satır olarak tutulmamalıdır.

Kötü yaklaşım:

| Product | Quantity |
| ------- | -------- |
| Pizza   | 1        |
| Pizza   | 1        |

Doğru yaklaşım:

| Product | Quantity |
| ------- | -------- |
| Pizza   | 2        |

### 4.4 Order entity

`Order`, tamamlanmış sipariş kaydıdır.

Önerilen alanlar:

| Field        | Type              | Açıklama                     |
| ------------ | ----------------- | ---------------------------- |
| `id`         | `UUID`            | `BaseEntity` üzerinden gelir |
| `user`       | `User`            | Siparişi veren kullanıcı     |
| `items`      | `List<OrderItem>` | Siparişteki ürün satırları   |
| `totalPrice` | `BigDecimal`      | Sipariş toplamı              |
| `status`     | `OrderStatus`     | Sipariş durumu               |
| `orderedAt`  | `LocalDateTime`   | Sipariş oluşturulma zamanı   |

Örnek entity:

```java
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false)
    private LocalDateTime orderedAt;
}
```

Sipariş durumu için enum:

```java
public enum OrderStatus {
    CREATED,
    PREPARING,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}
```

### 4.5 OrderItem entity

`OrderItem`, sipariş anındaki ürün satırıdır.

Önerilen alanlar:

| Field         | Type         | Açıklama                     |
| ------------- | ------------ | ---------------------------- |
| `id`          | `UUID`       | `BaseEntity` üzerinden gelir |
| `order`       | `Order`      | Hangi siparişe ait           |
| `product`     | `Product`    | Ürün referansı               |
| `productName` | `String`     | Sipariş anındaki ürün adı    |
| `quantity`    | `Integer`    | Kaç adet                     |
| `unitPrice`   | `BigDecimal` | Sipariş anındaki birim fiyat |
| `lineTotal`   | `BigDecimal` | Satır toplamı                |

Örnek entity:

```java
@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;
}
```

`productName` neden tekrar tutuluyor?

Çünkü ürün adı sonradan değişirse geçmiş sipariş bozulmamalıdır. Kullanıcı "Margherita Pizza" siparişi verdiyse, ürün adı yarın "Classic Margherita" olsa bile geçmiş siparişte eski isim görülebilir.

---

## 5. Veritabanı Şeması

Beklenen tablolar:

```text
users
products
categories
baskets
basket_items
orders
order_items
```

Basitleştirilmiş ilişki:

```text
users.id          -> baskets.user_id
users.id          -> orders.user_id
baskets.id        -> basket_items.basket_id
products.id       -> basket_items.product_id
orders.id         -> order_items.order_id
products.id       -> order_items.product_id
```

Örnek tablo mantığı:

| Table          | Açıklama                 |
| -------------- | ------------------------ |
| `baskets`      | Aktif sepet üst bilgisi  |
| `basket_items` | Sepetteki ürün satırları |
| `orders`       | Sipariş üst bilgisi      |
| `order_items`  | Sipariş ürün satırları   |

> Başlangıçta `spring.jpa.hibernate.ddl-auto=update` ile tablo oluşumunu öğrenmek kolaydır. Gerçek projede ise Flyway veya Liquibase gibi migration araçları tercih edilir.

---

## 6. Adım Adım Uygulama

### 6.1 Paketleri oluştur

Şu paketleri ekleyin:

```text
basket/controller
basket/dto
basket/entity
basket/mapper
basket/repository
basket/service
order/controller
order/dto
order/entity
order/mapper
order/repository
order/service
```

Mevcut `product` ve `category` yapısını örnek alın.

### 6.2 Basket creation logic

Projede JWT auth zaten aktif olduğu için sepet, giriş yapmış kullanıcıya bağlanır. `userIdentifier` query param kullanılmaz.

Örnek istek:

```http
GET /api/v1/baskets/current
Authorization: Bearer <jwt-token>
```

Service mantığı:

1. JWT filter'dan gelen kullanıcıyı al.
2. Kullanıcının aktif sepeti var mı kontrol et.
3. Varsa onu döndür.
4. Yoksa yeni `Basket` oluştur.
5. `active = true`, `totalPrice = 0` olarak başlat.

Repository method:

```java
Optional<Basket> findByUser_IdAndActiveTrue(UUID userId);
```

Service örneği:

```java
@Transactional
public Basket getOrCreateActiveBasket(User user) {
    return basketRepository.findByUser_IdAndActiveTrue(user.getId())
            .orElseGet(() -> {
                Basket basket = new Basket();
                basket.setUser(user);
                basket.setActive(true);
                basket.setTotalPrice(BigDecimal.ZERO);
                return basketRepository.save(basket);
            });
}
```

> Spring Data JPA dokümantasyonunda repository method isimlerinden query üretme desteklenir. Bu projede de `findByCategoryId` kullanıldığı için aynı yaklaşımı `findByUser_IdAndActiveTrue` gibi method'larla sürdürebilirsiniz.

### 6.3 Add product to basket

Request DTO:

```java
@Data
public class AddBasketItemRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
```

Akış:

1. Aktif sepeti bul veya oluştur.
2. Ürünü bul.
3. Ürün aktif mi kontrol et.
4. Stok yeterli mi kontrol et.
5. Sepette aynı ürün zaten var mı kontrol et.
6. Varsa quantity artır.
7. Yoksa yeni `BasketItem` oluştur.
8. Satır toplamını hesapla.
9. Sepet toplamını yeniden hesapla.
10. Sepeti kaydet.

Repository method:

```java
Optional<BasketItem> findByBasketIdAndProductId(UUID basketId, UUID productId);
```

Service örneği:

```java
@Transactional
public BasketResponse addProduct(User user, AddBasketItemRequest request) {
    Basket basket = getOrCreateActiveBasket(user);

    Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

    if (!Boolean.TRUE.equals(product.getActive())) {
        throw new IllegalArgumentException("Pasif ürün sepete eklenemez");
    }

    if (product.getStock() < request.getQuantity()) {
        throw new IllegalArgumentException("Yeterli stok yok");
    }

    BasketItem item = basketItemRepository
            .findByBasketIdAndProductId(basket.getId(), product.getId())
            .orElseGet(() -> {
                BasketItem newItem = new BasketItem();
                newItem.setBasket(basket);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                newItem.setUnitPrice(product.getPrice());
                return newItem;
            });

    item.setQuantity(item.getQuantity() + request.getQuantity());
    item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

    basketItemRepository.save(item);
    recalculateBasketTotal(basket);

    return basketMapper.toResponse(basket);
}
```

### 6.4 Remove product from basket

İki farklı yaklaşım vardır:

| Yaklaşım          | Açıklama                                 |
| ----------------- | ---------------------------------------- |
| Ürünü tamamen sil | Sepetteki satır komple kaldırılır        |
| Quantity azalt    | Adet 1 azaltılır, 0 olursa satır silinir |

Başlangıç için ürünü tamamen silmek daha basittir.

Endpoint:

```http
DELETE /api/v1/baskets/items/{productId}
Authorization: Bearer <jwt-token>
```

Akış:

1. Aktif sepeti bul.
2. Sepette ilgili ürünü bul.
3. Item'ı sil.
4. Sepet toplamını yeniden hesapla.
5. Güncel sepeti dön.

Service örneği:

```java
@Transactional
public BasketResponse removeProduct(User user, UUID productId) {
    Basket basket = basketRepository.findByUser_IdAndActiveTrue(user.getId())
            .orElseThrow(() -> new BasketNotFoundException(user.getId()));

    BasketItem item = basketItemRepository
            .findByBasketIdAndProductId(basket.getId(), productId)
            .orElseThrow(() -> new BasketItemNotFoundException(productId));

    basketItemRepository.delete(item);
    recalculateBasketTotal(basket);

    return basketMapper.toResponse(basket);
}
```

### 6.5 Get basket

Endpoint:

```http
GET /api/v1/baskets/current
Authorization: Bearer <jwt-token>
```

Bu endpoint aktif sepeti getirir. Sepet yoksa boş sepet oluşturabilir.

Response örneği:

```json
{
  "success": true,
  "code": 200,
  "message": "Sepet başarıyla getirildi",
  "data": {
    "id": "4d2b1f16-3f4c-4a3e-9a6f-111111111111",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "totalPrice": 420.0,
    "items": [
      {
        "productId": "8a1a3f27-cc1d-42f8-8c2a-222222222222",
        "productName": "Margherita Pizza",
        "quantity": 2,
        "unitPrice": 180.0,
        "lineTotal": 360.0
      },
      {
        "productId": "0c8d5f9e-13d4-4c24-b4f4-333333333333",
        "productName": "Ayran",
        "quantity": 2,
        "unitPrice": 30.0,
        "lineTotal": 60.0
      }
    ]
  }
}
```

### 6.6 Convert basket to order

Bu fazın en önemli business logic kısmı burasıdır.

Akış:

1. Kullanıcının aktif sepetini bul.
2. Sepet boş mu kontrol et.
3. Sepetteki her ürün için stok kontrolü yap.
4. Yeni `Order` oluştur.
5. Her `BasketItem` için bir `OrderItem` oluştur.
6. Ürün adını ve fiyatını `OrderItem` içine kopyala.
7. Order toplamını hesapla.
8. Ürün stoklarını düş.
9. Order'ı kaydet.
10. Basket'i pasife al veya item'ları temizle.
11. Response olarak yeni order'ı dön.

Service örneği:

```java
@Transactional
public OrderResponse createOrderFromBasket(User user) {
    Basket basket = basketRepository.findByUser_IdAndActiveTrue(user.getId())
            .orElseThrow(() -> new BasketNotFoundException(user.getId()));

    if (basket.getItems().isEmpty()) {
        throw new IllegalArgumentException("Boş sepetten sipariş oluşturulamaz");
    }

    Order order = new Order();
    order.setUser(user);
    order.setStatus(OrderStatus.CREATED);
    order.setOrderedAt(LocalDateTime.now());

    BigDecimal total = BigDecimal.ZERO;

    for (BasketItem basketItem : basket.getItems()) {
        Product product = basketItem.getProduct();

        if (product.getStock() < basketItem.getQuantity()) {
            throw new IllegalArgumentException("Yeterli stok yok: " + product.getName());
        }

        product.setStock(product.getStock() - basketItem.getQuantity());

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(basketItem.getQuantity());
        orderItem.setUnitPrice(product.getPrice());
        orderItem.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(basketItem.getQuantity())));

        order.getItems().add(orderItem);
        total = total.add(orderItem.getLineTotal());
    }

    order.setTotalPrice(total);
    Order savedOrder = orderRepository.save(order);

    basket.setActive(false);
    basket.getItems().clear();
    basket.setTotalPrice(BigDecimal.ZERO);

    return orderMapper.toResponse(savedOrder);
}
```

> Bu method `@Transactional` olmalıdır. Çünkü sipariş kaydı, sipariş item'ları, stok düşme ve sepet kapatma tek iş işlemi olarak düşünülmelidir. Bir adım başarısız olursa tamamı rollback olmalıdır.

---

## 7. API Endpoint'leri

Mevcut projede endpoint'ler `/api/v1/...` formatında. Basket ve order endpoint'leri **authenticated** olmalıdır; isteklerde `Authorization: Bearer <token>` header'ı gönderilir.

### 7.1 Basket endpoints

| Method   | Endpoint                              | Açıklama               | Auth |
| -------- | ------------------------------------- | ---------------------- | ---- |
| `GET`    | `/api/v1/baskets/current`             | Aktif sepeti getirir   | JWT  |
| `POST`   | `/api/v1/baskets/items`               | Sepete ürün ekler      | JWT  |
| `DELETE` | `/api/v1/baskets/items/{productId}`   | Ürünü sepetten çıkarır | JWT  |
| `DELETE` | `/api/v1/baskets/current`             | Sepeti temizler        | JWT  |

Add to basket request:

```http
POST /api/v1/baskets/items
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": "8a1a3f27-cc1d-42f8-8c2a-222222222222",
  "quantity": 2
}
```

Controller örneği:

```java
@RestController
@RequestMapping("/api/v1/baskets")
@Tag(name = "Baskets")
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @GetMapping("/current")
    public ResponseEntity<BaseResponse<BasketResponse>> getCurrentBasket(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BasketResponse basket = basketService.getCurrentBasket(userDetails.getUser());
        return ResponseEntity.ok(BaseResponse.success(200, "Sepet başarıyla getirildi", basket));
    }

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<BasketResponse>> addProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddBasketItemRequest request) {
        BasketResponse basket = basketService.addProduct(userDetails.getUser(), request);
        return ResponseEntity.ok(BaseResponse.success(200, "Ürün sepete eklendi", basket));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<BaseResponse<BasketResponse>> removeProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId) {
        BasketResponse basket = basketService.removeProduct(userDetails.getUser(), productId);
        return ResponseEntity.ok(BaseResponse.success(200, "Ürün sepetten çıkarıldı", basket));
    }
}
```

> `CustomUserDetails` sınıfına `getUser()` method'u eklemeniz gerekir. JWT filter zaten `CustomUserDetails`'i `SecurityContext`'e koyuyor; controller bu bilgiyi `@AuthenticationPrincipal` ile alır.

### 7.2 Order endpoints

| Method | Endpoint                   | Açıklama                            | Auth |
| ------ | -------------------------- | ----------------------------------- | ---- |
| `POST` | `/api/v1/orders`           | Aktif sepetten sipariş oluşturur    | JWT  |
| `GET`  | `/api/v1/orders`           | Kullanıcının siparişlerini listeler | JWT  |
| `GET`  | `/api/v1/orders/{orderId}` | Sipariş detayını getirir            | JWT  |

Create order request:

```http
POST /api/v1/orders
Authorization: Bearer <jwt-token>
```

Response örneği:

```json
{
  "success": true,
  "code": 201,
  "message": "Sipariş başarıyla oluşturuldu",
  "data": {
    "id": "94d7ac1a-5b4f-4f1a-a33f-444444444444",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "CREATED",
    "totalPrice": 420.0,
    "orderedAt": "2026-06-22T10:15:00",
    "items": [
      {
        "productId": "8a1a3f27-cc1d-42f8-8c2a-222222222222",
        "productName": "Margherita Pizza",
        "quantity": 2,
        "unitPrice": 180.0,
        "lineTotal": 360.0
      }
    ]
  }
}
```

Controller örneği:

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse order = orderService.createOrderFromBasket(userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "Sipariş başarıyla oluşturuldu", order));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> orders = orderService.findOrdersByUser(userDetails.getUser());
        return ResponseEntity.ok(BaseResponse.success(200, "Siparişler başarıyla listelendi", orders));
    }
}
```

---

## 8. Business Logic Açıklaması

### 8.1 Basket total nasıl hesaplanır?

Sepet toplamı, sepet item'larının `lineTotal` değerlerinin toplamıdır.

Formül:

```text
item.lineTotal = item.unitPrice * item.quantity
basket.totalPrice = sum(item.lineTotal)
```

Örnek:

| Product | Unit Price | Quantity | Line Total |
| ------- | ---------- | -------- | ---------- |
| Pizza   | 180        | 2        | 360        |
| Ayran   | 30         | 2        | 60         |
| Toplam  |            |          | 420        |

Service helper method:

```java
private void recalculateBasketTotal(Basket basket) {
    BigDecimal total = basket.getItems().stream()
            .map(BasketItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    basket.setTotalPrice(total);
    basketRepository.save(basket);
}
```

Yeni öğrenenler için önemli not:

`BigDecimal` kullanın. Para işlemlerinde `double` veya `float` kullanmak doğru değildir. Çünkü küsuratlı sayılarda hassasiyet problemleri çıkabilir.

### 8.2 Order nasıl basket'ten oluşturulur?

Order oluşturma, sepetin kopyalanması değildir. Order, sepetin işlenmiş ve sabitlenmiş halidir.

Basket'ten Order'a dönüşüm:

| Basket tarafı        | Order tarafı            |
| -------------------- | ----------------------- |
| `Basket.user`        | `Order.user`            |
| `Basket.totalPrice`  | `Order.totalPrice`      |
| `BasketItem.product`      | `OrderItem.product`     |
| `BasketItem.product.name` | `OrderItem.productName` |
| `BasketItem.quantity`     | `OrderItem.quantity`    |
| `Product.price`           | `OrderItem.unitPrice`   |

> Sipariş oluştururken fiyatı yeniden `Product.price` üzerinden almak daha güvenlidir. Böylece sepette uzun süre bekleyen eski fiyatlı ürünler siparişe yanlış yansımaz. Ancak bazı gerçek projelerde sepete ekleme anındaki fiyat kilitlenir. Bu ürün ve kampanya politikasına bağlıdır.

### 8.3 Sepet siparişten sonra ne olmalı?

İki seçenek vardır:

| Seçenek          | Açıklama                         | Başlangıç için öneri |
| ---------------- | -------------------------------- | -------------------- |
| Sepeti sil       | Basket ve item kayıtları silinir | Hayır                |
| Sepeti pasife al | `active=false` yapılır           | Evet                 |

Başlangıç için sepeti pasife almak daha öğreticidir. Çünkü geçmişte hangi sepetten sipariş oluştuğunu takip etmek ileride faydalı olabilir.

---

## 9. Service Layer Tasarımı

Bu fazda service katmanı özellikle önemlidir. Çünkü controller sadece request alıp response dönmelidir. İş kuralları service içinde olmalıdır.

### 9.1 BasketService sorumlulukları

Tüm kullanıcıya özel method'lar `User` parametresi alır (controller'dan `@AuthenticationPrincipal` ile gelir).

| Method                    | Sorumluluk                          |
| ------------------------- | ----------------------------------- |
| `getCurrentBasket(User)`  | Kullanıcının aktif sepetini getirir |
| `getOrCreateActiveBasket(User)` | Aktif sepet yoksa oluşturur |
| `addProduct(User, ...)`   | Ürün ekler veya quantity artırır    |
| `removeProduct(User, ...)`| Ürünü sepetten çıkarır              |
| `clearBasket(User)`       | Sepeti temizler                     |
| `recalculateBasketTotal`  | Sepet toplamını hesaplar            |

`BasketService`, sipariş oluşturmamalıdır. Sipariş oluşturma `OrderService` sorumluluğudur.

### 9.2 OrderService sorumlulukları

| Method                        | Sorumluluk                          |
| ----------------------------- | ----------------------------------- |
| `createOrderFromBasket(User)` | Aktif sepeti siparişe dönüştürür    |
| `findOrdersByUser(User)`      | Giriş yapmış kullanıcının siparişlerini listeler |
| `findById`              | Sipariş detayını getirir            |
| `changeStatus`          | İleride sipariş durumunu değiştirir |

### 9.3 Transaction sınırı

`createOrderFromBasket` tek transaction olmalıdır.

Çünkü bu method içinde birden fazla veri değişir:

- `orders` tablosuna kayıt eklenir.
- `order_items` tablosuna kayıtlar eklenir.
- `products.stock` azaltılır.
- `baskets.active` false yapılır.
- `basket_items` temizlenir veya pasif sepetle kalır.

Bu işlemlerden biri başarısız olursa hepsi geri alınmalıdır.

---

## 10. DTO ve Mapper Tasarımı

Entity'leri direkt response olarak dönmeyin. Bu projede zaten DTO kullanılıyor. Aynı yaklaşımı sürdürün.

### 10.1 Basket DTO'ları

Request:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBasketItemRequest {

    @NotNull(message = "Ürün ID boş olamaz")
    private UUID productId;

    @NotNull(message = "Adet boş olamaz")
    @Min(value = 1, message = "Adet en az 1 olmalıdır")
    private Integer quantity;
}
```

Response:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal totalPrice;
    private List<BasketItemResponse> items;
}
```

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemResponse {
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
```

### 10.2 Order DTO'ları

Response:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime orderedAt;
    private List<OrderItemResponse> items;
}
```

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
```

### 10.3 MapStruct mapper mantığı

Mevcut projede `ProductMapper` ve `CategoryMapper` var. Aynı stili koruyun.

Örnek:

```java
@Mapper(componentModel = "spring")
public interface BasketMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "items", target = "items")
    BasketResponse toResponse(Basket basket);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    BasketItemResponse toItemResponse(BasketItem item);
}
```

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "items", target = "items")
    OrderResponse toResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    OrderItemResponse toItemResponse(OrderItem item);
}
```

Circular reference probleminden kaçınmak için response içinde `BasketItem.basket` veya `OrderItem.order` dönmeyin.

---

## 11. Exception Handling Tasarımı

Mevcut projede her domain için ayrı exception handler kullanılıyor. Aynı yaklaşımı sürdürün.

Basket tarafı:

```text
BasketNotFoundException
BasketItemNotFoundException
BasketExceptionHandler
```

Order tarafı:

```text
OrderNotFoundException
OrderExceptionHandler
```

Örnek handler:

```java
@RestControllerAdvice
public class BasketExceptionHandler {

    @ExceptionHandler(BasketNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleBasketNotFound(BasketNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(BaseResponse.error(400, ex.getMessage()));
    }
}
```

Başlangıçta `IllegalArgumentException` kullanabilirsiniz. İleride daha temiz bir yapı için domain'e özel exception'lar oluşturabilirsiniz:

- `EmptyBasketException`
- `InsufficientStockException`
- `InactiveProductException`

---

## 12. Mevcut Auth Sistemi ile Entegrasyon

Projede auth sistemi zaten uygulanmış durumda. `SecurityConfig` içinde register ve login dışındaki endpoint'ler `authenticated()` olarak korunuyor; `JwtAuthenticationFilter` her istekte `Authorization: Bearer <token>` header'ını okuyup `SecurityContext`'e kullanıcıyı yerleştiriyor.

Bu fazda sepet ve sipariş endpoint'leri bu mevcut yapıya bağlanır. Ayrıntılı auth akışı için bkz. [spring-security-learning-guide.md](./spring-security-learning-guide.md).

### 12.1 Projede mevcut auth bileşenleri

| Bileşen                    | Durum | Konum |
| -------------------------- | ----- | ----- |
| `User` entity              | ✅    | `auth.entity` |
| `Role` enum (`USER`, `ADMIN`) | ✅ | `auth.entity` |
| `AuthController`           | ✅    | `/api/auth/register`, `/api/auth/login` |
| `AuthService`              | ✅    | Register + login + JWT üretimi |
| `JwtService`               | ✅    | Token üretme ve doğrulama |
| `JwtAuthenticationFilter`  | ✅    | Her istekte token okuma |
| `CustomUserDetailsService` | ✅    | Email ile kullanıcı yükleme |
| `CustomUserDetails`        | ✅    | `UserDetails` implementasyonu |
| `SecurityConfig`           | ✅    | JWT filter + endpoint kuralları |

### 12.2 Basket ve Order'da kullanıcı bağlantısı

Auth hazır olduğu için `userIdentifier` string alanı yerine doğrudan `User` entity ilişkisi kullanılır:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

Bu ilişki şu entity'lerde tanımlanır:

- `Basket`
- `Order`

Repository sorguları da buna göre yazılır:

```java
// BasketRepository
Optional<Basket> findByUser_IdAndActiveTrue(UUID userId);

// OrderRepository
List<Order> findByUser_IdOrderByOrderedAtDesc(UUID userId);
```

### 12.3 Controller'da giriş yapmış kullanıcıyı alma

JWT filter token'ı doğruladıktan sonra `CustomUserDetails`'i `SecurityContext`'e koyar. Controller'da `@AuthenticationPrincipal` ile bu bilgiye erişilir:

```java
@GetMapping("/current")
public ResponseEntity<BaseResponse<BasketResponse>> getCurrentBasket(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    BasketResponse basket = basketService.getCurrentBasket(userDetails.getUser());
    return ResponseEntity.ok(BaseResponse.success(200, "Sepet başarıyla getirildi", basket));
}
```

`CustomUserDetails` sınıfına şu method eklenmelidir:

```java
public User getUser() {
    return user;
}
```

### 12.4 Mevcut auth endpoint'leri

| Method | Endpoint            | Açıklama                             | Auth |
| ------ | ------------------- | ------------------------------------ | ---- |
| `POST` | `/api/auth/register` | Yeni kullanıcı oluşturur             | Yok  |
| `POST` | `/api/auth/login`    | Kullanıcı giriş yapar ve token döner | Yok  |

Register request:

```json
{
  "email": "oktay@example.com",
  "password": "secret12345",
  "firstName": "Oktay",
  "lastName": "Çavuş"
}
```

Login response:

```json
{
  "success": true,
  "code": 200,
  "message": "Başarıyla giriş yapıldı",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  }
}
```

### 12.5 Mevcut SecurityConfig

Projedeki `SecurityConfig` şu an şöyle çalışıyor:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                    ).permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

Basket ve order endpoint'leri `anyRequest().authenticated()` kapsamına girer. İstek örneği:

```http
GET /api/v1/baskets/current
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

`userIdentifier` query param artık kullanılmaz; kullanıcı bilgisi token'dan alınır.

### 12.6 Postman ile test akışı

1. `POST /api/auth/register` ile kullanıcı oluştur.
2. `POST /api/auth/login` ile `accessToken` al.
3. Sonraki tüm basket/order isteklerinde `Authorization: Bearer <token>` header'ı gönder.
4. Farklı kullanıcıyla login yaparak sepetlerin birbirinden izole olduğunu doğrula.

---

## 13. Projeyi Gerçek Dünya Projesi Gibi Geliştirme Önerileri

Bu fazı bitirdikten sonra projeyi şu yönlerde geliştirebilirsiniz.

### 13.1 Stock reservation

Başlangıçta stok sadece sipariş oluşturulurken düşülür. Gerçek projede sepet aşamasında stok rezerve etmek isteyebilirsiniz.

Basit yaklaşım:

- Sepete eklerken stok düşme.
- Sipariş oluştururken stok düş.
- Stok yetmezse siparişi reddet.

Gelişmiş yaklaşım:

- Sepete eklerken stok rezerve et.
- Sepet belirli süre içinde siparişe dönmezse rezervasyonu iptal et.

### 13.2 Order status history

`Order.status` tek alan olarak yeterli olabilir. Fakat gerçek projede durum geçmişi de tutulur.

Örnek:

```text
OrderStatusHistory
- order
- oldStatus
- newStatus
- changedAt
- changedBy
```

Böylece siparişin hangi aşamalardan geçtiği takip edilir.

### 13.3 Payment sistemi

Sipariş oluşturmak ile ödeme almak aynı şey değildir.

İleride şu ayrım yapılabilir:

| Entity               | Sorumluluk                  |
| -------------------- | --------------------------- |
| `Order`              | Sipariş bilgisi             |
| `Payment`            | Ödeme bilgisi               |
| `PaymentTransaction` | Ödeme sağlayıcı işlem kaydı |

### 13.4 Address sistemi

Siparişin teslim edileceği adres ayrıca modellenmelidir.

Önerilen yapı:

```text
User
UserAddress
Order.deliveryAddressSnapshot
```

Sipariş anındaki adres de snapshot olarak tutulmalıdır. Kullanıcı adresini sonradan değiştirirse eski siparişin adresi değişmemelidir.

### 13.5 Test yazma

Bu fazdan itibaren test yazmak daha önemli hale gelir.

Önerilen testler:

- Boş sepetten sipariş oluşturulamaz.
- Aynı ürün sepete ikinci kez eklenirse yeni satır açılmaz, quantity artar.
- Stok yetersizse sepete ekleme veya sipariş oluşturma reddedilir.
- Sipariş oluşturulunca stok düşer.
- Sipariş oluşturulunca basket pasife alınır.

### 13.6 Database migration

Başlangıçta Hibernate `ddl-auto=update` öğrenmek için yeterlidir. Gerçek dünya yaklaşımı:

- Flyway veya Liquibase eklenir.
- Her tablo değişikliği migration dosyasıyla yapılır.
- Production veritabanı rastgele güncellenmez.

---

## 14. Yaygın Hatalar

### 14.1 Entity'leri direkt JSON olarak dönmek

`Basket` içinde `BasketItem`, `BasketItem` içinde tekrar `Basket` olduğu için circular reference oluşabilir.

Çözüm:

- Response DTO kullanın.
- Mapper ile sadece gerekli alanları dönün.

### 14.2 Aynı ürünü sepette iki satır olarak tutmak

Bu veri tutarsızlığına yol açar.

Çözüm:

- `basket_id + product_id` unique constraint kullanın.
- Service içinde aynı ürün varsa quantity artırın.

### 14.3 Siparişte fiyatı sadece Product'a bağlı bırakmak

Eğer `OrderItem` içinde fiyat tutulmazsa geçmiş siparişler ürün fiyatı değiştikçe yanlış görünür.

Çözüm:

- `OrderItem.unitPrice`
- `OrderItem.lineTotal`
- Gerekirse `OrderItem.productName`

### 14.4 Transaction kullanmamak

Sipariş oluştururken order kaydolup stok düşmezse sistem tutarsız olur.

Çözüm:

- `OrderService.createOrderFromBasket` methodunu `@Transactional` yapın.

### 14.5 Stok kontrolünü unutmak

Sepette ürün var diye sipariş her zaman oluşmamalıdır. Sipariş anında stok tekrar kontrol edilmelidir.

Çözüm:

- Order oluştururken her ürün için stok kontrolü yapın.

### 14.6 Başka kullanıcının sepetine erişmeye çalışmak

JWT olmadan veya yanlış kullanıcı bilgisiyle sepete erişmek güvenlik açığı oluşturur.

Çözüm:

- Tüm basket/order endpoint'lerinde `@AuthenticationPrincipal CustomUserDetails` kullanın.
- Repository sorgularını `findByUser_Id...` ile yazın; kullanıcı sadece kendi verisine erişebilsin.
- Sipariş detayında `order.getUser().getId()` ile giriş yapmış kullanıcıyı karşılaştırın.

### 14.7 `Order` tablo adını direkt kullanmak

`order` SQL tarafında problem çıkarabilir.

Çözüm:

- Entity adı `Order` olabilir.
- Table adı `orders` olmalıdır.

---

## 15. Bu Fazda Neler Öğreneceksiniz

Bu fazı tamamladığınızda şunları öğrenmiş olacaksınız:

- `@OneToMany` ve `@ManyToOne` ilişkilerini gerçek iş senaryosunda kullanmak
- Parent-child entity tasarımı yapmak
- `cascade` ve `orphanRemoval` mantığını anlamak
- DTO ile circular reference problemini önlemek
- Service layer içinde business logic yazmak
- Sepet toplamı gibi hesaplanmış alanları yönetmek
- Sepeti siparişe dönüştürürken transaction kullanmak
- Para işlemlerinde `BigDecimal` kullanmanın önemini anlamak
- Sipariş geçmişinde snapshot veri tutmayı öğrenmek
- JWT auth ile sepet ve siparişi giriş yapmış kullanıcıya bağlamak
- `@AuthenticationPrincipal` ile controller'da kullanıcı bilgisine erişmek

Bu fazdan sonra proje artık şu seviyeye gelir:

```text
Product CRUD
Category ilişkisi
JWT Auth (register, login)
Basket sistemi
Order sistemi
Stok düşme
Sipariş geçmişi
Kullanıcıya özel sepet ve sipariş
```

---

## 16. Uygulama Checklist'i

Basket tarafı:

- [ ] `Basket` entity oluşturuldu
- [ ] `BasketItem` entity oluşturuldu
- [ ] `BasketRepository` oluşturuldu
- [ ] `BasketItemRepository` oluşturuldu
- [ ] `AddBasketItemRequest` oluşturuldu
- [ ] `BasketResponse` oluşturuldu
- [ ] `BasketItemResponse` oluşturuldu
- [ ] `BasketMapper` oluşturuldu
- [ ] `BasketService` oluşturuldu
- [ ] `BasketController` oluşturuldu
- [ ] Sepete ürün ekleme çalışıyor
- [ ] Sepetten ürün çıkarma çalışıyor
- [ ] Sepet toplamı doğru hesaplanıyor

Order tarafı:

- [ ] `Order` entity oluşturuldu
- [ ] `OrderItem` entity oluşturuldu
- [ ] `OrderStatus` enum oluşturuldu
- [ ] `OrderRepository` oluşturuldu
- [ ] `OrderItemRepository` oluşturuldu
- [ ] `OrderResponse` oluşturuldu
- [ ] `OrderItemResponse` oluşturuldu
- [ ] `OrderMapper` oluşturuldu
- [ ] `OrderService` oluşturuldu
- [ ] `OrderController` oluşturuldu
- [ ] Sepetten sipariş oluşturma çalışıyor
- [ ] Sipariş sonrası stok düşüyor
- [ ] Sipariş sonrası sepet pasife alınıyor
- [ ] Sipariş geçmişi listeleniyor

Auth entegrasyonu:

- [ ] `Basket` ve `Order` entity'lerinde `User` ilişkisi tanımlandı
- [ ] `CustomUserDetails.getUser()` method'u eklendi
- [ ] Basket/order controller'larında `@AuthenticationPrincipal` kullanıldı
- [ ] Repository sorguları `findByUser_Id...` formatında yazıldı
- [ ] Postman'de register → login → basket akışı test edildi
- [ ] Farklı kullanıcıların sepetleri birbirinden izole

Son kontrol:

- [ ] Swagger'da endpoint'ler görünüyor
- [ ] Boş sepetten sipariş oluşturulamıyor
- [ ] Pasif ürün sepete eklenemiyor
- [ ] Stok yetersizse sipariş oluşturulamıyor
- [ ] Response'larda entity yerine DTO dönülüyor

---

## Faz 3 Sonrası Önerilen Sıradaki Faz

Bu fazdan sonra en doğru ilerleme sırası:

1. **Faz 4: Address ve Payment Hazırlığı**
   - UserAddress
   - Order address snapshot
   - Payment entity
   - Payment status

2. **Faz 5: Admin ve Order Management**
   - Admin siparişleri listeler
   - Sipariş durumunu değiştirir
   - Role bazlı authorization (`/api/admin/**` endpoint'leri)

3. **Faz 6: İyileştirmeler**
   - `GET /api/auth/me` endpoint'i
   - Order status history
   - Stock reservation

> Bu sırayla giderseniz proje sadece çalışan bir CRUD uygulaması değil, gerçek dünyadaki backend mimarisine benzeyen bir mini food delivery backend olur.
