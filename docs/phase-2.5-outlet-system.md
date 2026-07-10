# Faz 2.5: Outlet Sistemi

**Proje:** Mini Food Delivery Backend  
**Faz:** N fazdan 2.5.  
**Odak:** Outlet entity + Category -> Outlet ilişkisi + Product -> Outlet ilişkisi  
**Bu fazda:** Outlet CRUD, kategorilerin outlet'e bağlanması, outlet menüsü listeleme, ürünlerin outlet'e bağlanması  
**Henüz yok:** Sepet, sipariş, outlet sahibi rolü, teslimat ücreti, adres sistemi

> Bu rehber, Faz 2'de kurulan Category ilişkilerini **outlet bazlı** modele taşır. Faz 3'e (sepet ve sipariş) geçmeden önce hem kategorilerin hem ürünlerin hangi restoran/şube menüsüne ait olduğunu modellemeniz gerekir. Aksi halde sepet sistemi gerçek dünyaya uymaz.

> **Önceki faz:** [phase-2-category-system-relationships.md](./phase-2-category-system-relationships.md)  
> **Sonraki faz:** [phase-3-basket-order-system.md](./phase-3-basket-order-system.md) — Faz 2.5 tamamlandıktan sonra uygulanmalıdır.

> **Dokümantasyon kaynağı:** Bu rehberdeki Spring Data JPA repository yaklaşımı ve `findBy...` query method örnekleri, Context7 üzerinden çekilen güncel **Spring Data JPA** dokümantasyonuna dayandırılmıştır: `/spring-projects/spring-data-jpa`.

> **Bu fazı nasıl okumalısınız?** Outlet, Faz 2'de öğrendiğiniz Category pattern'inin tekrarıdır. Fark şu: artık hem kategori hem ürün bir outlet menüsüne aittir. "Pizza" kategorisi global değildir; Pizza Palace Kadıköy'ün menüsündeki Pizza kategorisi ile Burger House Beşiktaş'ın Pizza kategorisi ayrı kayıtlardır.

---

## İçindekiler

1. [Bu Fazın Amacı](#1-bu-fazın-amacı)
2. [Outlet Gerçek Projelerde Neden Önemlidir?](#2-outlet-gerçek-projelerde-neden-önemlidir)
3. [Bu Fazda İnşa Edilecek Yapı](#3-bu-fazda-inşa-edilecek-yapı)
4. [Entity Tasarımı ve İlişkiler](#4-entity-tasarımı-ve-i̇lişkiler)
5. [İlişki Mantığı: Outlet → Category → Product](#5-i̇lişki-mantığı-outlet--category--product)
6. [Veritabanı Şeması](#6-veritabanı-şeması)
7. [Adım Adım Uygulama](#7-adım-adım-uygulama)
8. [API Endpoint'leri](#8-api-endpointleri)
9. [Mevcut Category ve Product Kodunun Refactor Edilmesi](#9-mevcut-category-ve-product-kodunun-refactor-edilmesi)
10. [Auth ve Security Notları](#10-auth-ve-security-notları)
11. [Faz 3'e Hazırlık: Sepet = User + Outlet](#11-faz-3e-hazırlık-sepet--user--outlet)
12. [Yaygın Hatalar](#12-yaygın-hatalar)
13. [Bu Fazda Neler Öğreneceksiniz](#13-bu-fazda-neler-öğreneceksiniz)
14. [Uygulama Checklist'i](#14-uygulama-checklisti)
15. [Sözlük](#15-sözlük)

---

## 1. Bu Fazın Amacı

Faz 1'de `Product` CRUD yapısını kurdunuz. Faz 2'de ürünleri kategorilere bağladınız. Faz 2.5'te artık **kategorilerin ve ürünlerin hangi outlet menüsüne ait olduğunu** modelleyeceksiniz.

Bu fazın ana hedefleri:

- `Outlet` entity'si ile restoran/şube kavramını oluşturmak
- `Category` entity'sine outlet ilişkisi eklemek (kategori artık outlet bazlı)
- `Product` entity'sine outlet ilişkisi eklemek
- Outlet CRUD API'si sunmak
- Outlet kategorilerini listelemek (`GET /outlets/{id}/categories`)
- Outlet menüsünü listelemek (`GET /outlets/{id}/products`)
- Faz 3 sepet sistemine doğru domain temeli atmak

Örnek gerçek dünya senaryosu:

| Outlet | Kategoriler | Ürünler |
| ------ | ----------- | ------- |
| Pizza Palace — Kadıköy | Pizza, İçecek | Margherita Pizza, Kola |
| Burger House — Beşiktaş | Burger, İçecek | Cheeseburger, Kola |
| Pizza Palace — Beşiktaş | Pizza, İçecek | Margherita Pizza (farklı fiyat/stok), Kola |

Aynı kategori adı ve ürün adı farklı outlet'lerde **ayrı kayıt** olabilir. Menü, fiyat ve stok outlet bazlıdır.

> **Bu fazın kuralları:**
> - Bir kategori mutlaka bir outlet'e aittir.
> - Bir ürün mutlaka bir outlet'e aittir.
> - Ürünün kategorisi, ürünün outlet'i ile aynı olmalıdır.
> - Global, outlet'siz kategori veya ürün kalmamalıdır.

---

## 2. Outlet Gerçek Projelerde Neden Önemlidir?

Getir Yemek, Yemeksepeti veya Trendyol Go gibi uygulamalarda kullanıcı akışı genelde şöyledir:

1. Yakındaki restoran/outlet listesini görür.
2. Bir outlet seçer.
3. O outlet'in menüsünü inceler.
4. Sepete ürün ekler.
5. Sipariş verir.

Bu akışta **ürünler global bir katalog değildir**. Her outlet'in kendi menüsü vardır.

### 2.1 Outlet olmadan ne olur?

Faz 2 sonrası mevcut yapıda tüm ürünler tek havuzda durur:

```http
GET /api/v1/products
```

Bu endpoint Pizza Palace ile Burger House ürünlerini karışık döner. Kullanıcı hangi restorandan sipariş verdiğini bilemez.

Sepet fazında daha ciddi bir problem oluşur: kullanıcı Pizza Palace'dan pizza, Burger House'tan burger ekleyebilir. Gerçek delivery uygulamalarında bu **tek sepette farklı restoran** kuralına aykırıdır.

### 2.2 Outlet ile ne kazanırsınız?

| Konu | Outlet olmadan | Outlet ile |
| ---- | -------------- | ---------- |
| Menü listeleme | Tüm ürünler karışık | Outlet menüsü ayrı |
| Ürün sahipliği | Belirsiz | Her ürün bir outlet'e ait |
| Sepet kapsamı | Kullanıcı başına tek sepet | Kullanıcı + outlet başına sepet (Faz 3) |
| Sipariş | Hangi restoran? | Order.outlet ile net |

### 2.3 Category ile Outlet ilişkisi

Bu iki kavram birbirinin yerine geçmez; birlikte çalışır:

| Kavram | Soruya cevap verir | Örnek |
| ------ | ------------------ | ----- |
| **Outlet** | Menü hangi restoran/şubeye ait? | Pizza Palace Kadıköy |
| **Category** | Bu outlet menüsünde ürün hangi grupta? | Pizza, Burger, İçecek |
| **Product** | Menüdeki somut ürün kaydı | Margherita Pizza — 250 TL |

Hiyerarşi:

```text
Outlet: Pizza Palace Kadıköy
├── Category: Pizza
│   ├── Margherita Pizza
│   └── Pepperoni Pizza
└── Category: İçecek
    └── Kola
```

> **Tasarım kararı (bu proje):** Category **outlet bazlıdır**. Her outlet kendi kategori listesini yönetir. Pizza Palace Kadıköy'de "Pizza" kategorisi ile Burger House Beşiktaş'ta "Pizza" kategorisi farklı kayıtlardır. Bu, gerçek delivery uygulamalarındaki menü yapısına daha yakındır.

---

## 3. Bu Fazda İnşa Edilecek Yapı

Mevcut projede `product` ve `category` paketleri domain bazlı ayrılmış durumda. Aynı stili koruyarak `outlet` paketini ekleyin:

```text
com.cavus.delivery_food
├── auth          ← mevcut (register, login, JWT)
├── outlet        ← bu fazda yeni
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
├── product       ← refactor (outlet ilişkisi + kategori-outlet uyumu)
├── category      ← refactor (outlet ilişkisi)
├── config
└── entity
```

Genel akış:

```mermaid
flowchart TD
    Client[Client]
    OutletController[OutletController]
    OutletService[OutletService]
    CategoryService[CategoryService]
    OutletRepository[OutletRepository]
    CategoryRepository[CategoryRepository]
    ProductService[ProductService]
    ProductRepository[ProductRepository]
    DB[(PostgreSQL)]

    Client --> OutletController
    OutletController --> OutletService
    OutletController --> CategoryService
    OutletController --> ProductService
    OutletService --> OutletRepository
    CategoryService --> CategoryRepository
    CategoryService --> OutletService
    ProductService --> ProductRepository
    ProductService --> OutletService
    ProductService --> CategoryService
    OutletRepository --> DB
    CategoryRepository --> DB
    ProductRepository --> DB
```

Bu fazda yeni ve güncellenen domain'ler:

| Domain | Sorumluluk |
| ------ | ---------- |
| `outlet` | Restoran/şube CRUD, menü ve kategori listeleme |
| `category` | Kategori artık outlet'e bağlı; create/list akışları güncellenir |
| `product` | Ürün outlet'e bağlı; kategori aynı outlet'ten seçilmeli |

---

## 4. Entity Tasarımı ve İlişkiler

Bu fazda bir yeni entity eklenir; mevcut `Category` ve `Product` güncellenir:

- `Outlet` (yeni)
- `Category` (outlet ilişkisi eklenir)
- `Product` (outlet ilişkisi eklenir)

### 4.1 Genel ilişki diyagramı

```mermaid
erDiagram
    OUTLET ||--o{ CATEGORY : has
    OUTLET ||--o{ PRODUCT : has
    CATEGORY ||--o{ PRODUCT : groups

    OUTLET {
        uuid id
        string name
        string description
        string imageUrl
        boolean active
    }

    CATEGORY {
        uuid id
        uuid outlet_id
        string name
        boolean active
    }

    PRODUCT {
        uuid id
        uuid outlet_id
        uuid category_id
        string name
        decimal price
        int stock
    }
```

### 4.2 Outlet entity

`Outlet`, bir restoran veya şubeyi temsil eder.

Önerilen alanlar:

| Field | Type | Açıklama |
| ----- | ---- | -------- |
| `id` | UUID | `BaseEntity` üzerinden gelir |
| `name` | String | Outlet adı (unique, normalize edilir) |
| `description` | String | Kısa açıklama |
| `imageUrl` | String | Logo veya kapak görseli |
| `active` | Boolean | Outlet aktif mi? |
| `categories` | List\<Category\> | Bu outlet menüsündeki kategoriler |
| `products` | List\<Product\> | Bu outlet menüsündeki ürünler |

Entity örneği:

```java
package com.cavus.delivery_food.outlet.entity;

import com.cavus.delivery_food.entity.BaseEntity;
import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "outlets")
public class Outlet extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "outlet")
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "outlet")
    private List<Product> products = new ArrayList<>();
}
```

Buradaki önemli noktalar:

- `@Table(name = "outlets")`: Tablo adını açıkça belirtir.
- `name unique`: Aynı isimde iki outlet olmamalı (normalize edilmiş haliyle kontrol edilir).
- `@OneToMany(mappedBy = "outlet")`: Foreign key hem `Category.outlet` hem `Product.outlet` tarafında tutulur.

> **Yeni başlayan notu:** Outlet entity'si artık hem kategori hem ürün listesini tutar. Faz 2'de öğrendiğiniz `@OneToMany(mappedBy = "...")` pattern'ini burada iki kez kullanıyorsunuz.

### 4.3 Category entity güncellemesi

Faz 2'de global olan kategori artık bir outlet'e bağlanır.

Önerilen alanlar:

| Field | Type | Açıklama |
| ----- | ---- | -------- |
| `id` | UUID | `BaseEntity` üzerinden gelir |
| `name` | String | Kategori adı (aynı outlet içinde unique) |
| `description` | String | Kısa açıklama |
| `active` | Boolean | Kategori aktif mi? |
| `outlet` | Outlet | Kategorinin ait olduğu outlet |
| `products` | List\<Product\> | Bu kategorideki ürünler |

Entity örneği:

```java
package com.cavus.delivery_food.category.entity;

import com.cavus.delivery_food.entity.BaseEntity;
import com.cavus.delivery_food.outlet.entity.Outlet;
import com.cavus.delivery_food.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
    name = "categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"name", "outlet_id"})
)
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id", nullable = false)
    private Outlet outlet;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();
}
```

Buradaki önemli noktalar:

- `@Table(uniqueConstraints = ...)`: Aynı outlet içinde iki "Pizza" kategorisi olamaz; farklı outlet'lerde olabilir.
- Faz 2'deki `@Column(unique = true)` kaldırılır — global unique artık geçerli değildir.
- `@ManyToOne Outlet outlet`: Her kategori tek bir outlet menüsüne aittir.

### 4.4 Product entity güncellemesi

Mevcut `Product` entity'sine outlet alanı eklenir:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "outlet_id", nullable = false)
private Outlet outlet;
```

Güncellenmiş Product alanları:

| Alan | Tip | Açıklama |
| ---- | --- | -------- |
| `id` | UUID | BaseEntity'den gelir |
| `name` | String | Ürün adı |
| `description` | String | Ürün açıklaması |
| `price` | BigDecimal | Ürün fiyatı |
| `imageUrl` | String | Ürün görseli |
| `stock` | Integer | Stok miktarı |
| `unit` | String | Birim |
| `active` | Boolean | Ürün aktif mi? |
| `category` | Category | Ürünün kategorisi (aynı outlet'ten olmalı) |
| `outlet` | Outlet | Ürünün ait olduğu outlet menüsü |

Tam entity örneği:

```java
package com.cavus.delivery_food.product.entity;

import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.entity.BaseEntity;
import com.cavus.delivery_food.outlet.entity.Outlet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id", nullable = false)
    private Outlet outlet;
}
```

#### Product'ta iki ManyToOne ilişki

Artık `Product` iki farklı entity'ye bağlanır; ikisi de aynı outlet kapsamında olmalıdır:

```java
@ManyToOne private Category category;  // ürün hangi grupta?
@ManyToOne private Outlet outlet;      // ürün hangi menüde?
```

Veritabanında karşılığı:

```text
categories.outlet_id -> outlets.id
products.category_id -> categories.id
products.outlet_id   -> outlets.id
```

Service katmanında şu kural zorunludur:

```java
if (!category.getOutlet().getId().equals(outlet.getId())) {
    throw new IllegalArgumentException("Kategori, seçilen outlet'e ait değil");
}
```

Her iki ilişki de `Product` tarafında `@ManyToOne` olarak tanımlanır. JPA her biri için ayrı foreign key kolonu üretir.

---

## 5. İlişki Mantığı: Outlet → Category → Product

### 5.1 Outlet ↔ Category

```java
// Category.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "outlet_id", nullable = false)
private Outlet outlet;

// Outlet.java
@OneToMany(mappedBy = "outlet")
private List<Category> categories = new ArrayList<>();
```

Okuma:

> One Outlet has many Categories.  
> Category belongs to one Outlet.

Türkçe:

> Bir outlet birçok kategoriye sahip olabilir.  
> Bir kategori bir outlet'e aittir.

### 5.2 Outlet ↔ Product

```java
// Product.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "outlet_id", nullable = false)
private Outlet outlet;

// Outlet.java
@OneToMany(mappedBy = "outlet")
private List<Product> products = new ArrayList<>();
```

Okuma:

> One Outlet has many Products.  
> Product belongs to one Outlet.

### 5.3 Category ↔ Product

Faz 2'deki Category-Product ilişkisi korunur; artık her iki taraf da aynı outlet kapsamındadır:

```java
// Product.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;

// Category.java
@OneToMany(mappedBy = "category")
private List<Product> products = new ArrayList<>();
```

### 5.4 Üç entity birlikte nasıl çalışır?

Örnek veri:

| Outlet | Category | Product | Price |
| ------ | -------- | ------- | ----- |
| Pizza Palace Kadıköy | Pizza | Margherita | 250 |
| Pizza Palace Kadıköy | İçecek | Kola | 40 |
| Burger House Beşiktaş | Burger | Cheeseburger | 180 |
| Burger House Beşiktaş | İçecek | Kola | 45 |

Dikkat:

- "Pizza" kategorisi iki farklı outlet'te **ayrı Category kaydı** olarak durur.
- "Kola" ürünü iki farklı outlet'te **ayrı Product kaydı** olarak durur.
- Pizza Palace Kadıköy'deki bir ürün, Burger House Beşiktaş'taki bir kategoriye bağlanamaz.

### 5.5 DTO katmanında ilişki gösterimi

Client, entity nesnelerini değil ID'leri gönderir:

| Katman | Category | Outlet |
| ------ | -------- | ------ |
| Request DTO | `UUID categoryId` | `UUID outletId` |
| Response DTO | `categoryId`, `categoryName` | `outletId`, `outletName` |
| Entity | `Category category` | `Outlet outlet` |

Kategori oluştururken outlet bilgisi zorunludur. Nested route kullanıyorsanız `outletId` path'ten gelir; flat route kullanıyorsanız body'de gönderilir.

### 5.6 Circular reference uyarısı

`OutletResponse` içinde `List<Product>` döndürmeyin. Product zaten outlet bilgisi taşır; ters yönde nested response circular reference ve dev JSON üretir.

Doğru yaklaşım:

- Outlet listesi: sadece outlet bilgisi
- Outlet menüsü: ayrı endpoint (`GET /outlets/{id}/products`)

---

## 6. Veritabanı Şeması

Bu faz sonunda veritabanında üç ana tablo olacaktır: `categories`, `outlets`, `products`.

### 6.1 outlets tablosu

| Kolon | Tip | Kural |
| ----- | --- | ----- |
| `id` | UUID | Primary key |
| `name` | varchar(150) | Not null, unique |
| `description` | varchar(500) | Nullable |
| `image_url` | varchar(255) | Nullable |
| `active` | boolean | Not null |
| `created_at` | timestamp | BaseEntity |
| `updated_at` | timestamp | BaseEntity |
| `created_by` | varchar | BaseEntity auditing |
| `updated_by` | varchar | BaseEntity auditing |

Örnek veri:

| id | name | description | active |
| -- | ---- | ----------- | ------ |
| `o1...` | Pizza Palace Kadıköy | Napoli usulü pizza | true |
| `o2...` | Burger House Beşiktaş | Smash burger | true |

### 6.2 categories tablosu (güncellenmiş)

| Kolon | Tip | Kural |
| ----- | --- | ----- |
| `id` | UUID | Primary key |
| `name` | varchar(100) | Not null |
| `description` | varchar(500) | Nullable |
| `active` | boolean | Not null |
| `outlet_id` | UUID | Foreign key → outlets, **not null** |
| `created_at` | timestamp | BaseEntity |
| `updated_at` | timestamp | BaseEntity |

Unique kural: `(name, outlet_id)` — aynı outlet içinde aynı isimde iki kategori olamaz.

Örnek veri:

| id | name | outlet_id | active |
| -- | ---- | --------- | ------ |
| `c1...` | pizza | Pizza Palace Kadıköy | true |
| `c2...` | pizza | Burger House Beşiktaş | true |
| `c3...` | içecek | Pizza Palace Kadıköy | true |

### 6.3 products tablosu (güncellenmiş)

| Kolon | Tip | Kural |
| ----- | --- | ----- |
| `id` | UUID | Primary key |
| `name` | varchar | Not null |
| `description` | varchar(500) | Nullable |
| `price` | numeric(10,2) | Not null |
| `image_url` | varchar(255) | Nullable |
| `stock` | integer | Not null |
| `unit` | varchar(20) | Nullable |
| `active` | boolean | Not null |
| `category_id` | UUID | Foreign key → categories |
| `outlet_id` | UUID | Foreign key → outlets, **not null** |
| `created_at` | timestamp | BaseEntity |
| `updated_at` | timestamp | BaseEntity |

Örnek veri:

| name | price | category_id | outlet_id |
| ---- | ----- | ----------- | --------- |
| Margherita Pizza | 250.00 | Pizza kategorisi | Pizza Palace Kadıköy |
| Cheeseburger | 180.00 | Burger kategorisi | Burger House Beşiktaş |

### 6.4 Foreign key ilişkileri

```text
outlets.id     -> categories.outlet_id
outlets.id     -> products.outlet_id
categories.id  -> products.category_id
```

### 6.5 SQL mantığı

JPA `ddl-auto=update` ile tabloyu güncelleyebilir. Arka plandaki fikir:

```sql
CREATE TABLE outlets (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(255),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

ALTER TABLE categories
ADD COLUMN outlet_id UUID;

-- Mevcut veri varsa: önce default outlet oluştur, kategorilere ata
UPDATE categories SET outlet_id = '<default-outlet-uuid>' WHERE outlet_id IS NULL;

ALTER TABLE categories
ALTER COLUMN outlet_id SET NOT NULL;

ALTER TABLE categories
ADD CONSTRAINT fk_categories_outlet
FOREIGN KEY (outlet_id)
REFERENCES outlets(id);

-- Faz 2'deki global unique kaldırılır, outlet bazlı unique eklenir
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;
ALTER TABLE categories
ADD CONSTRAINT uk_categories_name_outlet UNIQUE (name, outlet_id);

ALTER TABLE products
ADD COLUMN outlet_id UUID;

-- Mevcut veri varsa: ürünlere default outlet ata
UPDATE products SET outlet_id = '<default-outlet-uuid>' WHERE outlet_id IS NULL;

ALTER TABLE products
ALTER COLUMN outlet_id SET NOT NULL;

ALTER TABLE products
ADD CONSTRAINT fk_products_outlet
FOREIGN KEY (outlet_id)
REFERENCES outlets(id);
```

### 6.6 Mevcut veri migration notu

Veritabanınızda Faz 2'den kalan kategoriler ve ürünler varsa şu sırayı izleyin:

1. `outlets` tablosunu oluşturun.
2. Geçici bir default outlet kaydedin (ör. "Legacy Outlet").
3. Tüm mevcut `categories` satırlarına bu outlet'in ID'sini yazın.
4. `categories.outlet_id` kolonunu `NOT NULL` yapın; global `name unique` kuralını `(name, outlet_id)` ile değiştirin.
5. Tüm mevcut `products` satırlarına aynı outlet'in ID'sini yazın.
6. `products.outlet_id` kolonunu `NOT NULL` yapın.
7. Test verinizi yeniden düzenleyin: en az 2 outlet, her birinde kendi kategorileri ve ürünleri.

> **Yeni başlayan notu:** Production ortamında bu tür şema değişiklikleri Flyway/Liquibase migration dosyalarıyla yapılır. Öğrenme aşamasında Hibernate `ddl-auto=update` yeterlidir; fakat migration adımlarını bilinçli olarak düşünün. Temiz bir veritabanıyla başlıyorsanız entity'leri yazıp uygulamayı çalıştırmanız yeterlidir.

---

## 7. Adım Adım Uygulama

Bu bölümde dosya dosya nasıl ilerleyeceğiniz anlatılır.

### 7.1 Paket yapısını oluştur

```text
src/main/java/com/cavus/delivery_food/outlet
├── controller
│   └── OutletController.java
├── dto
│   ├── OutletRequest.java
│   └── OutletResponse.java
├── entity
│   └── Outlet.java
├── mapper
│   └── OutletMapper.java
├── repository
│   └── OutletRepository.java
└── service
    ├── OutletExceptionHandler.java
    ├── OutletNotFoundException.java
    └── OutletService.java
```

| Klasör | Ne işe yarar? |
| ------ | ------------- |
| `entity` | `outlets` tablosunun Java karşılığı |
| `dto` | API request/response modelleri |
| `repository` | Veritabanı erişimi |
| `service` | İş kuralları (isim normalize, aktif kontrol) |
| `controller` | REST endpoint'leri |
| `mapper` | Entity ↔ DTO dönüşümü (MapStruct) |

### 7.2 Outlet entity oluştur

Dosya: `src/main/java/com/cavus/delivery_food/outlet/entity/Outlet.java`

Faz 2'deki `Category.java` dosyasını referans alın. Alanlar: `name`, `description`, `imageUrl`, `active`, `products`.

### 7.3 Outlet DTO'larını oluştur

**OutletRequest.java:**

```java
package com.cavus.delivery_food.outlet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutletRequest {

    @NotBlank(message = "Outlet adı boş olamaz")
    @Size(max = 150, message = "Outlet adı en fazla 150 karakter olabilir")
    private String name;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;

    @Size(max = 255)
    private String imageUrl;

    private Boolean active = true;
}
```

**OutletResponse.java:**

```java
package com.cavus.delivery_food.outlet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutletResponse {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean active;
}
```

> Response'da `List<Product>` yok. Menü için ayrı endpoint kullanılır.

### 7.4 OutletRepository oluştur

```java
package com.cavus.delivery_food.outlet.repository;

import com.cavus.delivery_food.outlet.entity.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutletRepository extends JpaRepository<Outlet, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<Outlet> findByActiveTrue();
}
```

Spring Data JPA bu method isimlerinden otomatik sorgu üretir:

| Method | Üretilen sorgu mantığı |
| ------ | ---------------------- |
| `existsByNameIgnoreCase` | Aynı isimde outlet var mı? (büyük/küçük harf duyarsız) |
| `findByActiveTrue` | Sadece aktif outlet'leri getir |

### 7.5 OutletMapper oluştur

`CategoryMapper` ile aynı MapStruct pattern:

```java
package com.cavus.delivery_food.outlet.mapper;

import com.cavus.delivery_food.outlet.dto.OutletRequest;
import com.cavus.delivery_food.outlet.dto.OutletResponse;
import com.cavus.delivery_food.outlet.entity.Outlet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OutletMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Outlet toEntity(OutletRequest request);

    OutletResponse toOutletResponse(Outlet outlet);

    List<OutletResponse> toOutletResponseList(List<Outlet> outlets);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateOutletFromRequest(OutletRequest request, @MappingTarget Outlet outlet);
}
```

### 7.6 OutletNotFoundException oluştur

```java
package com.cavus.delivery_food.outlet.service;

import java.util.UUID;

public class OutletNotFoundException extends RuntimeException {

    public OutletNotFoundException(UUID id) {
        super("Outlet bulunamadı: " + id);
    }
}
```

### 7.7 OutletExceptionHandler oluştur

`CategoryExceptionHandler` ile aynı yapı:

```java
package com.cavus.delivery_food.outlet.service;

import com.cavus.delivery_food.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OutletExceptionHandler {

    @ExceptionHandler(OutletNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFound(OutletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(400, ex.getMessage()));
    }
}
```

### 7.8 OutletService oluştur

`CategoryService` pattern'ini takip edin. Önemli method'lar:

```java
@Service
@Transactional
public class OutletService {

    private final OutletRepository outletRepository;
    private final OutletMapper outletMapper;

    public OutletResponse create(OutletRequest request) {
        String normalizedName = normalizeName(request.getName());

        if (outletRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Bu outlet adı zaten kullanılıyor: " + request.getName());
        }

        Outlet entity = outletMapper.toEntity(request);
        entity.setName(normalizedName);
        Outlet saved = outletRepository.save(entity);
        return outletMapper.toOutletResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OutletResponse> findAll() {
        return outletMapper.toOutletResponseList(outletRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<OutletResponse> findAllActive() {
        return outletMapper.toOutletResponseList(outletRepository.findByActiveTrue());
    }

    @Transactional(readOnly = true)
    public OutletResponse findById(UUID id) {
        return outletMapper.toOutletResponse(getEntityById(id));
    }

    /// ProductService bu method'u outlet ilişkisi kurarken kullanır
    @Transactional(readOnly = true)
    public Outlet getEntityById(UUID outletId) {
        return outletRepository.findById(outletId)
                .orElseThrow(() -> new OutletNotFoundException(outletId));
    }

    public OutletResponse update(UUID id, OutletRequest request) {
        Outlet outlet = getEntityById(id);
        outletMapper.updateOutletFromRequest(request, outlet);

        if (request.getName() != null) {
            String normalizedName = normalizeName(request.getName());
            if (!normalizedName.equalsIgnoreCase(outlet.getName())
                    && outletRepository.existsByNameIgnoreCase(normalizedName)) {
                throw new IllegalArgumentException("Bu outlet adı zaten kullanılıyor: " + request.getName());
            }
            outlet.setName(normalizedName);
        }

        return outletMapper.toOutletResponse(outletRepository.save(outlet));
    }

    public void deactivate(UUID id) {
        Outlet outlet = getEntityById(id);
        outlet.setActive(false);
        outletRepository.save(outlet);
    }

    public List<OutletResponse> createBulk(List<OutletRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Outlet listesi boş olamaz");
        }

        // CategoryService.createBulk ile aynı duplicate kontrol mantığı
        // ...

        List<Outlet> outlets = requests.stream()
                .map(request -> {
                    Outlet outlet = outletMapper.toEntity(request);
                    outlet.setName(normalizeName(request.getName()));
                    return outlet;
                })
                .toList();

        return outletMapper.toOutletResponseList(outletRepository.saveAll(outlets));
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
```

> **Önemli:** `getEntityById()` entity döner, `findById()` response döner. `ProductService`, ürün oluştururken gerçek `Outlet` entity'sine ihtiyaç duyar — tıpkı `CategoryService.getEntityById()` gibi.

### 7.9 OutletController oluştur

```java
@RestController
@RequestMapping("/api/v1/outlets")
@Tag(name = "Outlets")
public class OutletController {

    private final OutletService outletService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public OutletController(OutletService outletService,
                            ProductService productService,
                            CategoryService categoryService) {
        this.outletService = outletService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<OutletResponse>> create(
            @Valid @RequestBody OutletRequest request) {
        OutletResponse created = outletService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location)
                .body(BaseResponse.success(201, "Outlet başarıyla oluşturuldu", created));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<OutletResponse>>> getAll() {
        return ResponseEntity.ok(BaseResponse.success(200, "Outlet'ler listelendi",
                outletService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OutletResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(BaseResponse.success(200, "Outlet getirildi",
                outletService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<OutletResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OutletRequest request) {
        return ResponseEntity.ok(BaseResponse.success(200, "Outlet güncellendi",
                outletService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deactivate(@PathVariable UUID id) {
        outletService.deactivate(id);
        return ResponseEntity.ok(BaseResponse.success(200, "Outlet pasife alındı", null));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<List<OutletResponse>>> createBulk(
            @Valid @RequestBody List<@Valid OutletRequest> requests) {
        List<OutletResponse> created = outletService.createBulk(requests);
        return ResponseEntity.status(201)
                .body(BaseResponse.success(201, "Outlet'ler oluşturuldu", created));
    }

    @GetMapping("/{outletId}/categories")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getOutletCategories(
            @PathVariable UUID outletId) {
        outletService.getEntityById(outletId);
        List<CategoryResponse> categories = categoryService.findByOutletId(outletId);
        return ResponseEntity.ok(BaseResponse.success(200, "Outlet kategorileri listelendi", categories));
    }

    @PostMapping("/{outletId}/categories")
    public ResponseEntity<BaseResponse<CategoryResponse>> createOutletCategory(
            @PathVariable UUID outletId,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.createForOutlet(outletId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location)
                .body(BaseResponse.success(201, "Kategori başarıyla oluşturuldu", created));
    }

    @GetMapping("/{outletId}/products")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getOutletMenu(
            @PathVariable UUID outletId) {
        outletService.getEntityById(outletId); // outlet var mı kontrol
        List<ProductResponse> products = productService.findByOutletId(outletId);
        return ResponseEntity.ok(BaseResponse.success(200, "Outlet menüsü listelendi", products));
    }

    @GetMapping("/{outletId}/categories/{categoryId}/products")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getOutletMenuByCategory(
            @PathVariable UUID outletId,
            @PathVariable UUID categoryId) {
        outletService.getEntityById(outletId);
        List<ProductResponse> products = productService.findByOutletIdAndCategoryId(outletId, categoryId);
        return ResponseEntity.ok(BaseResponse.success(200, "Kategoriye göre menü listelendi", products));
    }
}
```

### 7.10 Product entity'ye outlet ekle

`Product.java` dosyasına `@ManyToOne Outlet outlet` alanını ekleyin (Bölüm 4.3'teki örnek).

Import eklemeyi unutmayın:

```java
import com.cavus.delivery_food.outlet.entity.Outlet;
```

### 7.11 Product DTO güncellemeleri

**ProductRequest.java** — `outletId` ekleyin:

```java
@NotNull(message = "Outlet zorunludur")
private UUID outletId;
```

**ProductResponse.java** — outlet bilgisi ekleyin:

```java
private String outletId;
private String outletName;
```

### 7.12 ProductMapper güncellemesi

```java
@Mapping(target = "category", ignore = true)
@Mapping(target = "outlet", ignore = true)
Product toEntity(ProductRequest productRequest);

@Mapping(source = "category.id", target = "categoryId")
@Mapping(source = "category.name", target = "categoryName")
@Mapping(source = "outlet.id", target = "outletId")
@Mapping(source = "outlet.name", target = "outletName")
ProductResponse toProductResponse(Product product);
```

`updateProductFromRequest` method'unda da `outlet`'i ignore edin; outlet ataması service'te yapılır.

### 7.13 ProductRepository query method'ları

```java
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategoryId(UUID categoryId);

    List<Product> findByOutletId(UUID outletId);

    List<Product> findByOutletIdAndCategoryId(UUID outletId, UUID categoryId);
}
```

Spring Data JPA method isimlerini parçalar:

| Method | Anlam |
| ------ | ----- |
| `findByOutletId` | `Product.outlet.id = ?` |
| `findByOutletIdAndCategoryId` | Hem outlet hem kategori filtresi |

### 7.14 ProductService güncellemesi

`ProductService` constructor'ına `OutletService` enjekte edin:

```java
private final OutletService outletService;
```

**create** method'unda outlet ataması ve kategori-outlet uyumu:

```java
@Transactional
public ProductResponse create(ProductRequest request) {
    Product entity = productMapper.toEntity(request);

    Outlet outlet = outletService.getEntityById(request.getOutletId());
    entity.setOutlet(outlet);

    if (request.getCategoryId() != null) {
        Category category = categoryService.getEntityById(request.getCategoryId());
        validateCategoryBelongsToOutlet(category, outlet);
        entity.setCategory(category);
    }

    Product savedProduct = productRepository.save(entity);
    return productMapper.toProductResponse(savedProduct);
}

private void validateCategoryBelongsToOutlet(Category category, Outlet outlet) {
    if (!category.getOutlet().getId().equals(outlet.getId())) {
        throw new IllegalArgumentException("Kategori, seçilen outlet'e ait değil");
    }
}
```

**update** method'unda outlet ve kategori değişikliği:

```java
Outlet targetOutlet = product.getOutlet();

if (request.getOutletId() != null) {
    targetOutlet = outletService.getEntityById(request.getOutletId());
    product.setOutlet(targetOutlet);
}

if (request.getCategoryId() != null) {
    Category category = categoryService.getEntityById(request.getCategoryId());
    validateCategoryBelongsToOutlet(category, targetOutlet);
    product.setCategory(category);
}
```

Yeni listeleme method'ları:

```java
@Transactional(readOnly = true)
public List<ProductResponse> findByOutletId(UUID outletId) {
    outletService.getEntityById(outletId);
    return productMapper.toProductResponseList(productRepository.findByOutletId(outletId));
}

@Transactional(readOnly = true)
public List<ProductResponse> findByOutletIdAndCategoryId(UUID outletId, UUID categoryId) {
    Outlet outlet = outletService.getEntityById(outletId);
    Category category = categoryService.getEntityById(categoryId);
    validateCategoryBelongsToOutlet(category, outlet);
    return productMapper.toProductResponseList(
            productRepository.findByOutletIdAndCategoryId(outletId, categoryId));
}
```

### 7.15 ProductController güncellemesi

Mevcut endpoint'leri koruyun, yeni endpoint ekleyin:

```java
@GetMapping("/by-outlet/{outletId}")
@Operation(summary = "Outlet menüsündeki ürünleri listele")
public ResponseEntity<BaseResponse<List<ProductResponse>>> getProductsByOutlet(
        @PathVariable UUID outletId) {
    List<ProductResponse> products = productService.findByOutletId(outletId);
    return ResponseEntity.ok(
            BaseResponse.success(200, "Outlet ürünleri listelendi", products));
}
```

`POST /api/v1/products` body'sine artık `outletId` zorunlu gelmelidir.

### 7.16 Category entity'ye outlet ekle

`Category.java` dosyasına `@ManyToOne Outlet outlet` alanını ekleyin (Bölüm 4.3'teki örnek).

- Faz 2'deki `@Column(unique = true)` kaldırın.
- `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "outlet_id"}))` ekleyin.

### 7.17 Category DTO güncellemeleri

**CategoryRequest.java** — flat route kullanıyorsanız `outletId` ekleyin (nested route'ta path'ten gelir):

```java
private UUID outletId; // nested route kullanıyorsanız service set eder
```

**CategoryResponse.java** — outlet bilgisi ekleyin:

```java
private String outletId;
private String outletName;
```

### 7.18 CategoryRepository güncellemesi

```java
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByOutletId(UUID outletId);

    List<Category> findByOutletIdAndActiveTrue(UUID outletId);

    boolean existsByNameIgnoreCaseAndOutlet_Id(String name, UUID outletId);
}
```

| Method | Anlam |
| ------ | ----- |
| `findByOutletId` | Belirli outlet'in tüm kategorileri |
| `existsByNameIgnoreCaseAndOutlet_Id` | Aynı outlet içinde isim çakışması var mı? |

### 7.19 CategoryMapper güncellemesi

```java
@Mapping(target = "outlet", ignore = true)
@Mapping(target = "products", ignore = true)
Category toEntity(CategoryRequest request);

@Mapping(source = "outlet.id", target = "outletId")
@Mapping(source = "outlet.name", target = "outletName")
CategoryResponse toCategoryResponse(Category category);
```

### 7.20 CategoryService güncellemesi

`CategoryService` constructor'ına `OutletService` enjekte edin:

```java
private final OutletService outletService;
```

**createForOutlet** — nested route için ana method:

```java
public CategoryResponse createForOutlet(UUID outletId, CategoryRequest request) {
    Outlet outlet = outletService.getEntityById(outletId);
    String normalizedName = normalizeName(request.getName());

    if (categoryRepository.existsByNameIgnoreCaseAndOutlet_Id(normalizedName, outletId)) {
        throw new IllegalArgumentException("Bu outlet'te kategori adı zaten kullanılıyor: " + request.getName());
    }

    Category entity = categoryMapper.toEntity(request);
    entity.setName(normalizedName);
    entity.setOutlet(outlet);
    Category saved = categoryRepository.save(entity);
    return categoryMapper.toCategoryResponse(saved);
}
```

**findByOutletId**:

```java
@Transactional(readOnly = true)
public List<CategoryResponse> findByOutletId(UUID outletId) {
    outletService.getEntityById(outletId);
    return categoryMapper.toCategoryResponseList(categoryRepository.findByOutletIdAndActiveTrue(outletId));
}
```

**createBulk** güncellemesi: bulk istekte tüm kategoriler aynı outlet'e ait olmalıdır; duplicate kontrolü `existsByNameIgnoreCaseAndOutlet_Id` ile yapılır.

### 7.21 CategoryController güncellemesi

Flat global `/api/v1/categories` endpoint'leri kaldırılabilir veya deprecated bırakılabilir. Tercih edilen yaklaşım: kategori işlemlerini outlet altında toplamak.

```java
// OutletController içinde (Bölüm 7.9)
GET  /api/v1/outlets/{outletId}/categories
POST /api/v1/outlets/{outletId}/categories
```

Mobil akış: kullanıcı önce outlet seçer, sonra o outlet'in kategorilerini görür.

### 7.22 Test verisi oluştur

Uygulama bittikten sonra en az şu veriyi oluşturun:

**Adım 1 — Outlet'ler:**

```json
POST /api/v1/outlets/bulk
[
  { "name": "Pizza Palace Kadıköy", "description": "Napoli usulü pizza", "active": true },
  { "name": "Burger House Beşiktaş", "description": "Smash burger", "active": true }
]
```

**Adım 2 — Her outlet için kategoriler:**

```json
POST /api/v1/outlets/{pizza-palace-uuid}/categories
{ "name": "Pizza", "description": "Pizza çeşitleri", "active": true }

POST /api/v1/outlets/{pizza-palace-uuid}/categories
{ "name": "İçecek", "active": true }

POST /api/v1/outlets/{burger-house-uuid}/categories
{ "name": "Burger", "active": true }
```

**Adım 3 — Ürünler (outletId + aynı outlet'ten categoryId ile):**

```json
POST /api/v1/products
{
  "name": "Margherita Pizza",
  "price": 250.00,
  "stock": 50,
  "active": true,
  "categoryId": "<pizza-palace-pizza-category-uuid>",
  "outletId": "<pizza-palace-uuid>"
}
```

**Adım 4 — Kategori ve menü doğrulama:**

```http
GET /api/v1/outlets/{pizza-palace-uuid}/categories
GET /api/v1/outlets/{pizza-palace-uuid}/products
GET /api/v1/outlets/{pizza-palace-uuid}/categories/{pizza-category-uuid}/products
```

Yanıtta sadece Pizza Palace'a ait kategoriler ve ürünler gelmelidir. Burger House kategorisi bu listede görünmemelidir.

---

## 8. API Endpoint'leri

### 8.1 Outlet CRUD

#### Create outlet

```http
POST /api/v1/outlets
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Pizza Palace Kadıköy",
  "description": "Napoli usulü pizza",
  "imageUrl": "https://example.com/pizza-palace.jpg",
  "active": true
}
```

Response:

```json
{
  "success": true,
  "code": 201,
  "message": "Outlet başarıyla oluşturuldu",
  "data": {
    "id": "outlet-uuid",
    "name": "pizza palace kadıköy",
    "description": "Napoli usulü pizza",
    "imageUrl": "https://example.com/pizza-palace.jpg",
    "active": true
  }
}
```

#### List active outlets

```http
GET /api/v1/outlets
Authorization: Bearer <jwt-token>
```

Response:

```json
{
  "success": true,
  "code": 200,
  "message": "Outlet'ler listelendi",
  "data": [
    {
      "id": "outlet-uuid-1",
      "name": "pizza palace kadıköy",
      "description": "Napoli usulü pizza",
      "imageUrl": null,
      "active": true
    },
    {
      "id": "outlet-uuid-2",
      "name": "burger house beşiktaş",
      "description": "Smash burger",
      "imageUrl": null,
      "active": true
    }
  ]
}
```

#### Get outlet by id

```http
GET /api/v1/outlets/{id}
Authorization: Bearer <jwt-token>
```

#### Update outlet

```http
PUT /api/v1/outlets/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

#### Deactivate outlet (soft delete)

```http
DELETE /api/v1/outlets/{id}
Authorization: Bearer <jwt-token>
```

Outlet silinmez; `active = false` yapılır. Menüsünde ürün varsa hard delete yerine bu yaklaşım tercih edilir.

#### Bulk create outlets

```http
POST /api/v1/outlets/bulk
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### 8.2 Outlet kategorileri

#### Outlet kategorilerini listele

```http
GET /api/v1/outlets/{outletId}/categories
Authorization: Bearer <jwt-token>
```

Response:

```json
{
  "success": true,
  "code": 200,
  "message": "Outlet kategorileri listelendi",
  "data": [
    {
      "id": "category-uuid-1",
      "name": "pizza",
      "description": "Pizza çeşitleri",
      "active": true,
      "outletId": "outlet-uuid",
      "outletName": "pizza palace kadıköy"
    },
    {
      "id": "category-uuid-2",
      "name": "içecek",
      "description": null,
      "active": true,
      "outletId": "outlet-uuid",
      "outletName": "pizza palace kadıköy"
    }
  ]
}
```

#### Outlet için kategori oluştur

```http
POST /api/v1/outlets/{outletId}/categories
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Pizza",
  "description": "Pizza çeşitleri",
  "active": true
}
```

Aynı outlet içinde aynı isimde ikinci kategori oluşturulursa `400 Bad Request` döner.

### 8.3 Outlet menüsü

#### Outlet menüsünü listele

```http
GET /api/v1/outlets/{outletId}/products
Authorization: Bearer <jwt-token>
```

Response:

```json
{
  "success": true,
  "code": 200,
  "message": "Outlet menüsü listelendi",
  "data": [
    {
      "id": "product-uuid-1",
      "name": "Margherita Pizza",
      "price": 250.00,
      "stock": 50,
      "active": true,
      "categoryId": "category-uuid",
      "categoryName": "Pizza",
      "outletId": "outlet-uuid",
      "outletName": "pizza palace kadıköy"
    }
  ]
}
```

#### Outlet menüsünü kategoriye göre filtrele

```http
GET /api/v1/outlets/{outletId}/categories/{categoryId}/products
Authorization: Bearer <jwt-token>
```

Bu endpoint, mobil uygulamada "Pizza Palace → Pizza kategorisi" ekranını besler.

### 8.4 Product endpoint değişiklikleri

#### Create product (outletId zorunlu)

```http
POST /api/v1/products
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Margherita Pizza",
  "description": "Domates, mozzarella, fesleğen",
  "price": 250.00,
  "stock": 50,
  "unit": "adet",
  "active": true,
  "categoryId": "category-uuid",
  "outletId": "outlet-uuid"
}
```

`outletId` olmadan gönderilen istek `400 Bad Request` dönmelidir. `categoryId`, seçilen `outletId` ile aynı outlet'e ait değilse yine `400` dönmelidir.

#### List products by outlet (alternatif route)

```http
GET /api/v1/products/by-outlet/{outletId}
Authorization: Bearer <jwt-token>
```

> **API tasarım notu:** Menü listeleme için nested route (`/outlets/{id}/products`) tercih edilir. Flat route (`/products/by-outlet/{id}`) geriye dönük uyumluluk veya admin paneli için tutulabilir.

### 8.5 Endpoint özeti

| Method | Path | Açıklama |
| ------ | ---- | -------- |
| POST | `/api/v1/outlets` | Outlet oluştur |
| GET | `/api/v1/outlets` | Aktif outlet listesi |
| GET | `/api/v1/outlets/{id}` | Outlet detay |
| PUT | `/api/v1/outlets/{id}` | Outlet güncelle |
| DELETE | `/api/v1/outlets/{id}` | Outlet pasife al |
| POST | `/api/v1/outlets/bulk` | Toplu outlet oluştur |
| GET | `/api/v1/outlets/{outletId}/categories` | Outlet kategorileri |
| POST | `/api/v1/outlets/{outletId}/categories` | Outlet için kategori oluştur |
| GET | `/api/v1/outlets/{outletId}/products` | Outlet menüsü |
| GET | `/api/v1/outlets/{outletId}/categories/{categoryId}/products` | Outlet + kategori filtresi |
| POST | `/api/v1/products` | Ürün oluştur (`outletId` zorunlu, kategori aynı outlet'ten) |
| GET | `/api/v1/products/by-outlet/{outletId}` | Outlet ürünleri (alternatif) |

---

## 9. Mevcut Category ve Product Kodunun Refactor Edilmesi

Bu bölüm, Faz 2'den gelen kodda hangi dosyaların değişeceğini özetler.

### 9.1 Değişecek dosyalar

| Dosya | Değişiklik |
| ----- | ---------- |
| `Category.java` | `@ManyToOne Outlet outlet` eklendi; global `unique` kaldırıldı |
| `CategoryRequest.java` | Nested route dışında `outletId` desteği |
| `CategoryResponse.java` | `outletId`, `outletName` eklendi |
| `CategoryMapper.java` | outlet mapping eklendi |
| `CategoryRepository.java` | `findByOutletId`, `existsByNameIgnoreCaseAndOutlet_Id` |
| `CategoryService.java` | `OutletService` enjekte, outlet bazlı create/list |
| `CategoryController.java` | Global endpoint'ler kaldırılır veya outlet nested route'a taşınır |
| `Product.java` | `@ManyToOne Outlet outlet` eklendi |
| `ProductRequest.java` | `outletId` alanı eklendi |
| `ProductResponse.java` | `outletId`, `outletName` eklendi |
| `ProductMapper.java` | outlet mapping eklendi |
| `ProductRepository.java` | `findByOutletId`, `findByOutletIdAndCategoryId` |
| `ProductService.java` | `OutletService` enjekte, kategori-outlet uyumu kontrolü |
| `ProductController.java` | `by-outlet` endpoint eklendi |
| `OutletController.java` | kategori listeleme/oluşturma endpoint'leri eklendi |

### 9.2 Faz 2'den kaldırılan varsayımlar

| Eski varsayım (Faz 2) | Yeni durum (Faz 2.5) |
| --------------------- | -------------------- |
| Kategori global | Kategori outlet bazlı |
| `categories.name` global unique | `(name, outlet_id)` composite unique |
| `POST /api/v1/categories` | `POST /api/v1/outlets/{id}/categories` |
| `GET /api/v1/categories` tüm kategoriler | `GET /api/v1/outlets/{id}/categories` outlet menüsü |

### 9.3 createBulk dikkat noktası

`ProductService.createBulk()` method'u varsa, bulk request'lerde de her ürün için `outletId` zorunlu olmalıdır. Aksi halde `outlet_id NOT NULL` constraint ihlali oluşur.

Önerilen yaklaşım: bulk create'de tüm ürünler aynı outlet'e ait olabilir; request'e top-level `outletId` veya her item'da ayrı `outletId` kabul edilebilir. Basitlik için her `ProductRequest` içinde `outletId` zorunlu tutun.

### 9.4 Global GET /products ve GET /categories uyarısı

`GET /api/v1/products` tüm outlet'lerin ürünlerini karışık döner. Eski `GET /api/v1/categories` endpoint'i outlet kapsamı olmadan anlamsız hale gelir. Mobil uygulama akışında **outlet-scoped listeleme** tercih edilmelidir.

---

## 10. Auth ve Security Notları

Projede JWT auth zaten aktif. [`SecurityConfig`](../src/main/java/com/cavus/delivery_food/config/SecurityConfig.java) şu an tüm `/api/v1/**` isteklerini authenticated tutuyor.

### 10.1 Bu fazda security kuralı

Outlet endpoint'leri de diğer API'ler gibi JWT gerektirir:

```http
GET /api/v1/outlets
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Auth detayları için bkz. [spring-security-learning-guide.md](./spring-security-learning-guide.md).

### 10.2 İleride yapılabilecekler

| Konu | Açıklama |
| ---- | -------- |
| Public outlet listeleme | `GET /api/v1/outlets` permitAll — kullanıcı giriş yapmadan restoran listesi görebilir |
| Admin-only outlet CRUD | `POST/PUT/DELETE /api/admin/outlets` — sadece ADMIN rolü |
| Outlet owner rolü | Restoran sahibi sadece kendi outlet menüsünü yönetir |

Bu fazda basitlik için tüm outlet endpoint'leri authenticated user'a açıktır.

### 10.3 Postman test akışı

1. `POST /api/auth/register` ile kullanıcı oluştur.
2. `POST /api/auth/login` ile `accessToken` al.
3. `POST /api/v1/outlets` ile outlet oluştur.
4. `POST /api/v1/outlets/{outletId}/categories` ile kategori oluştur.
5. `POST /api/v1/products` ile `outletId` ve aynı outlet'ten `categoryId` göndererek ürün oluştur.
6. `GET /api/v1/outlets/{outletId}/categories` ile kategori listesini doğrula.
7. `GET /api/v1/outlets/{outletId}/products` ile menüyü doğrula.
8. Farklı outlet'e kategori/ürün ekleyip listelerin birbirinden izole olduğunu kontrol et.
9. Başka outlet'in kategorisiyle ürün create dene → `400` beklenir.
10. `outletId` olmadan product create dene → `400` beklenir.

---

## 11. Faz 3'e Hazırlık: Sepet = User + Outlet

Faz 2.5 tamamlandığında domain, sepet sistemine hazır hale gelir. [phase-3-basket-order-system.md](./phase-3-basket-order-system.md) dokümanı henüz outlet'siz yazılmıştır; Faz 3'e geçmeden önce aşağıdaki değişiklikleri planlayın.

### 11.1 Basket entity güncellemesi

Mevcut Phase 3 tasarımı:

```java
// Sadece user — eksik
@ManyToOne
private User user;
```

Faz 2.5 sonrası önerilen tasarım:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "outlet_id", nullable = false)
private Outlet outlet;
```

### 11.2 Sepet benzersizlik kuralı

Phase 3'teki `findByUser_IdAndActiveTrue` yerine:

```java
Optional<Basket> findByUser_IdAndOutlet_IdAndActiveTrue(UUID userId, UUID outletId);
```

Anlam: Bir kullanıcının **her outlet için ayrı aktif sepeti** olabilir.

| Kullanıcı | Outlet | Aktif Sepet |
| --------- | ------ | ----------- |
| Ali | Pizza Palace | 2 pizza, 1 kola |
| Ali | Burger House | 1 burger |
| Ayşe | Pizza Palace | boş (henüz ürün yok) |

### 11.3 Sepete ürün ekleme kuralı

```java
if (!product.getOutlet().getId().equals(basket.getOutlet().getId())) {
    throw new IllegalArgumentException(
        "Bu ürün sepetinizdeki outlet menüsüne ait değil");
}
```

Bu kural, Faz 2.5'te kurduğunuz `Product -> Outlet` ilişkisinin sepet fazındaki karşılığıdır.

### 11.4 Önerilen Phase 3 API değişiklikleri

| Eski (outlet'siz) | Yeni (outlet'li) |
| ----------------- | ---------------- |
| `GET /api/v1/baskets/current` | `GET /api/v1/outlets/{outletId}/baskets/current` |
| `POST /api/v1/baskets/items` | `POST /api/v1/outlets/{outletId}/baskets/items` |
| `POST /api/v1/orders` | `POST /api/v1/outlets/{outletId}/orders` |

### 11.5 Order entity

Sipariş de hangi outlet'ten verildiğini bilmelidir:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "outlet_id", nullable = false)
private Outlet outlet;
```

Sipariş oluşturulduktan sonra outlet adı değişse bile geçmiş sipariş etkilenmemeli. İleride `Order` içinde `outletName` snapshot alanı eklenebilir (Faz 3'teki `OrderItem.unitPrice` mantığıyla aynı).

### 11.6 Phase 3 doküman güncellemesi

Faz 2.5 bittikten sonra `phase-3-basket-order-system.md` dosyasında şu bölümler güncellenmelidir:

- Entity diyagramına `OUTLET` eklenmeli
- `Basket` ve `Order` entity örneklerine `outlet` alanı eklenmeli
- Repository query method'ları outlet bazlı olmalı
- API endpoint'leri nested outlet route kullanmalı
- Business logic bölümüne cross-outlet kontrolü eklenmeli

---

## 12. Yaygın Hatalar

### 12.1 Product oluştururken outletId unutmak

**Belirti:** `outlet_id` NOT NULL constraint violation.

**Çözüm:** `ProductRequest`'e `@NotNull outletId` ekleyin; service'te `outletService.getEntityById()` çağırın.

### 12.2 mappedBy yanlış yazmak

**Yanlış:**

```java
// Outlet.java
@OneToMany(mappedBy = "outlets")  // Product'ta böyle bir alan yok
```

**Doğru:**

```java
@OneToMany(mappedBy = "outlet")
```

`mappedBy` değeri, karşı entity'deki field adıyla birebir aynı olmalıdır.

### 12.3 OutletResponse içinde Product listesi döndürmek

**Belirti:** JSON circular reference, çok büyük response, StackOverflowError riski.

**Çözüm:** Menü için ayrı endpoint kullanın. Response DTO'da sadece outlet bilgisi tutun.

### 12.4 Outlet silinince orphan product bırakmak

**Belirti:** Pasif outlet'in ürünleri hâlâ siparişe açık görünür.

**Çözüm:** Outlet deactivate edilirken menüsündeki ürünleri de `active = false` yapın veya deactivate öncesi ürün kontrolü yapın.

### 12.5 Global GET /products ile menü karıştırmak

**Belirti:** Farklı restoran ürünleri aynı listede.

**Çözüm:** Kullanıcı akışında `GET /outlets/{id}/products` kullanın.

### 12.6 Mevcut veriyi migrate etmeden NOT NULL eklemek

**Belirti:** Hibernate startup hatası veya null outlet_id.

**Çözüm:** Bölüm 6.5'teki migration sırasını izleyin: önce default outlet, sonra NOT NULL.

### 12.7 Cross-outlet kategori ataması

**Belirti:** Pizza Palace ürününe Burger House kategorisi bağlanabiliyor.

**Çözüm:** `ProductService` içinde `validateCategoryBelongsToOutlet()` kontrolünü create ve update akışlarına ekleyin.

### 12.8 Global kategori unique kuralını korumak

**Belirti:** Farklı outlet'lerde aynı "Pizza" kategorisi oluşturulamıyor.

**Çözüm:** `@Column(unique = true)` kaldırın; `(name, outlet_id)` composite unique kullanın. Repository'de `existsByNameIgnoreCaseAndOutlet_Id` ile kontrol edin.

### 12.9 Category'yi outlet'siz bırakmak

**Belirti:** `outlet_id NOT NULL` constraint violation veya outlet'siz global kategori listesi.

**Çözüm:** Kategori oluşturma akışını `POST /outlets/{id}/categories` altına taşıyın; service'te outlet atamasını zorunlu kılın.

---

## 13. Bu Fazda Neler Öğreneceksiniz

- Yeni bir domain paketi (`outlet`) oluşturmayı
- Mevcut entity'ye `@ManyToOne` ilişki eklemeyi (Category ve Product)
- Category pattern'ini outlet kapsamına taşımayı
- Composite unique constraint (`name + outlet_id`) kavramını
- Nested REST route tasarımını (`/outlets/{id}/categories`, `/outlets/{id}/products`)
- Global vs scoped listeleme farkını
- Cross-outlet business rule doğrulamasını
- Domain-driven faz sıralamasını: outlet → sepet → sipariş
- Mevcut veri migration düşüncesini
- Faz 3 sepet tasarımına zemin hazırlamayı

---

## 14. Uygulama Checklist'i

### Outlet domain

- [ ] `outlet` paket yapısı oluşturuldu
- [ ] `Outlet` entity yazıldı
- [ ] `OutletRequest` ve `OutletResponse` DTO'ları yazıldı
- [ ] `OutletRepository` — `existsByNameIgnoreCase`, `findByActiveTrue`
- [ ] `OutletMapper` (MapStruct) yazıldı
- [ ] `OutletService` — create, findAll, findById, getEntityById, update, deactivate, createBulk
- [ ] `OutletNotFoundException` ve `OutletExceptionHandler` yazıldı
- [ ] `OutletController` — CRUD + menü endpoint'leri

### Category refactor

- [ ] `Category` entity'ye `@ManyToOne Outlet outlet` eklendi
- [ ] Global `name unique` kaldırıldı; `(name, outlet_id)` unique eklendi
- [ ] `CategoryResponse`'a `outletId`, `outletName` eklendi
- [ ] `CategoryRepository` — `findByOutletId`, `existsByNameIgnoreCaseAndOutlet_Id`
- [ ] `CategoryMapper` outlet mapping ile güncellendi
- [ ] `CategoryService` — `createForOutlet`, `findByOutletId`
- [ ] `OutletController` — kategori listeleme/oluşturma endpoint'leri

### Product refactor

- [ ] `Product` entity'ye `@ManyToOne Outlet outlet` eklendi
- [ ] `ProductRequest`'e `outletId` eklendi (`@NotNull`)
- [ ] `ProductResponse`'a `outletId`, `outletName` eklendi
- [ ] `ProductMapper` outlet mapping ile güncellendi
- [ ] `ProductRepository` — `findByOutletId`, `findByOutletIdAndCategoryId`
- [ ] `ProductService` — kategori-outlet uyumu kontrolü eklendi
- [ ] `ProductController` — `by-outlet` endpoint (opsiyonel)

### Veritabanı ve test

- [ ] Mevcut kategoriler default outlet'e migrate edildi
- [ ] Mevcut ürünler default outlet'e migrate edildi
- [ ] `outlet_id NOT NULL` constraint çalışıyor (categories + products)
- [ ] En az 2 outlet test verisi oluşturuldu
- [ ] Her outlet'e kendi kategorileri ve ürünleri atandı
- [ ] `GET /outlets/{id}/categories` doğru kapsamı döndürüyor
- [ ] `GET /outlets/{id}/products` menüyü doğru döndürüyor
- [ ] Cross-outlet kategori ataması → 400 dönüyor
- [ ] `outletId` olmadan product create → 400 dönüyor
- [ ] JWT ile tüm outlet endpoint'leri test edildi

### Faz 3 hazırlık (planlama)

- [ ] Basket = user + outlet modeli anlaşıldı
- [ ] Phase 3 doküman güncelleme maddeleri not edildi

---

## 15. Sözlük

| Terim | Açıklama |
| ----- | -------- |
| **Outlet** | Restoran veya şube. Kategorilerin ve ürünlerin ait olduğu menü sahibi. |
| **Menü** | Bir outlet'e bağlı kategori ve ürün listesi. |
| **Outlet kategorisi** | Bir outlet menüsüne ait kategori. Aynı isim farklı outlet'lerde tekrar edebilir. |
| **Scoped listeleme** | Veriyi belirli bir kapsama göre filtreleme (outlet menüsü/kategorisi gibi). |
| **Composite unique** | Birden fazla kolonun birlikte benzersiz olması (`name + outlet_id`). |
| **Soft delete** | Kaydı silmek yerine `active = false` yapma. |
| **Foreign key** | Bir tablonun başka tabloya referans veren kolonu (`outlet_id`). |
| **Nested route** | URL'de parent-child ilişkisi: `/outlets/{id}/categories`, `/outlets/{id}/products`. |
| **Migration** | Mevcut veritabanı şemasını yeni modele taşıma işlemi. |
| **Cross-outlet** | Farklı outlet'lere ait verinin yanlışlıkla aynı kapsamda birleşmesi. |

---

## Sonraki Faz İçin Hazırlık

Faz 2.5 tamamlandığında sistemde artık:

- Kategoriler outlet menülerine bağlıdır (Faz 2.5 refactor)
- Ürünler outlet menülerine bağlıdır (Faz 2.5)
- Kullanıcı auth sistemi hazırdır

Bu temel üzerine [Faz 3: Basket ve Order Sistemi](./phase-3-basket-order-system.md) inşa edilebilir. Faz 3'e geçmeden önce Phase 3 dokümanını outlet-aware olacak şekilde güncellemeniz önerilir (Bölüm 11).

### İleride genişletilebilecek outlet alanları

Bu fazda minimal alan seti kullanıldı. Gerçek projede outlet entity'sine şunlar eklenebilir:

| Alan | Açıklama |
| ---- | -------- |
| `address` | Teslimat adresi |
| `phone` | İletişim telefonu |
| `minOrderAmount` | Minimum sipariş tutarı |
| `deliveryFee` | Teslimat ücreti |
| `openingHours` | Çalışma saatleri |
| `latitude`, `longitude` | Konum bazlı outlet arama |

Bu alanlar Faz 3 veya sonrasında address/teslimat fazında eklenebilir.

---

## İlgili Dokümanlar

| Doküman | İlişki |
| ------- | ------ |
| [phase-1-basic-crud-foundation.md](./phase-1-basic-crud-foundation.md) | Product CRUD temeli |
| [phase-2-category-system-relationships.md](./phase-2-category-system-relationships.md) | Category ilişkisi (önceki faz) |
| [phase-3-basket-order-system.md](./phase-3-basket-order-system.md) | Sepet ve sipariş (sonraki faz) |
| [spring-security-learning-guide.md](./spring-security-learning-guide.md) | JWT auth entegrasyonu |
