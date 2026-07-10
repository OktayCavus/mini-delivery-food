# Spring Security Öğrenme Rehberi — Adım Adım

**Proje:** delivery_food  
**Spring Boot:** 3.5.x | **Java:** 21

> Bu doküman **sırayla** okunmak için yazıldı. Önce register, sonra login, sonra JWT, en sonda filter ve SecurityConfig güncellemesi. Her adımda: **ne yapıyoruz → neden → kod → istek akışı**.

---

## İçindekiler

0. [Başlamadan Önce — 5 Dakikalık Büyük Resim](#0-başlamadan-önce--5-dakikalık-büyük-resim)
1. [Adım 1 — SecurityConfig (✅ yaptın)](#adım-1--securityconfig--yaptın)
2. [Adım 2 — User ve Role Entity (✅ yaptın)](#adım-2--user-ve-role-entity--yaptın)
3. [Adım 3 — Register (/register)](#adım-3--register-register)
4. [Adım 4 — UserDetails ve UserDetailsService](#adım-4--userdetails-ve-userdetailsservice)
5. [Adım 5 — Login (/login) ve AuthenticationManager](#adım-5--login-login-ve-authenticationmanager)
6. [Adım 6 — JwtService (Token Üretme)](#adım-6--jwtservice-token-üretme)
7. [Adım 7 — SecurityConfig Güncelle (Endpoint Kuralları)](#adım-7--securityconfig-güncelle-endpoint-kuralları)
8. [Adım 8 — JwtAuthenticationFilter](#adım-8--jwtauthenticationfilter)
9. [Adım 9 — SecurityContext (Artık Anlam Kazanır)](#adım-9--securitycontext-artık-anlam-kazanır)
10. [Adım 10 — AuditorAware Güncelle](#adım-10--auditoraware-güncelle)
11. [Adım 11 — Baştan Sona Tam Akış](#adım-11--baştan-sona-tam-akış)
12. [Sözlük](#sözlük)

---

## 0. Başlamadan Önce — 5 Dakikalık Büyük Resim

### Spring Security ne işe yarar?

İki soruya cevap verir:

1. **Authentication** — Bu isteği kim gönderdi? (login)
2. **Authorization** — Bu kişi bu endpoint'e erişebilir mi? (rol kontrolü)

### Bir istek uygulamada nasıl akar?

```
Client (Postman / Mobil)
        │
        ▼
Security Filter Chain     ← JWT filter burada (henüz yok)
        │
        ▼
DispatcherServlet
        │
        ▼
Controller  →  Service  →  Repository  →  PostgreSQL
```

> **💡 Şimdilik bunu bil yeter:** Her HTTP isteği Controller'a gitmeden önce Security filter'larından geçer. JWT filter'ı yazınca token burada okunacak.

### Bu dokümanda yapacağın sıra

```
SecurityConfig ✅
User + Role ✅
Register ✅ (kısmen yaptın)
UserDetailsService ⬜  ← login'den ÖNCE lazım
Login + AuthenticationManager ⬜
JwtService ⬜
SecurityConfig güncelle ⬜
JwtAuthenticationFilter ⬜
AuditorAware güncelle ⬜
```

### Merkez kavram (sonra detaylandıracağız)

Tüm sistemin kalbi şu satır:

```java
SecurityContextHolder.getContext().setAuthentication(authentication);
```

Bunu **login sırasında** veya **JWT filter'da** yaparsın. Yaptıktan sonra her yerden "şu an kim giriş yapmış?" diye sorabilirsin.

---

## Adım 1 — SecurityConfig (✅ yaptın)

### Ne yaptın?

Spring Security'yi projeye ekledin ve "şimdilik herkes her yere girebilsin" dedin.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
```

| Parça | Ne demek? |
|-------|-----------|
| `@EnableWebSecurity` | Spring Security'yi aç |
| `SecurityFilterChain` | Tüm güvenlik filter'larının zinciri |
| `csrf.disable()` | REST API'de CSRF token gerekmez |
| `permitAll()` | Kimlik doğrulama olmadan herkes erişir |

### Şimdilik neden permitAll?

Register ve login'i test ederken engellenmemen için. JWT filter'ı yazınca **Adım 7**'de bunu değiştireceksin.

---

## Adım 2 — User ve Role Entity (✅ yaptın)

### Ne yaptın?

Veritabanında kullanıcıları tutacak tabloyu tanımladın.

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    private String email;       // login'de kullanılacak
    private String password;    // BCrypt hash olarak saklanacak
    private String firstName;
    private String lastName;
    private Role role;          // USER veya ADMIN
}

enum Role {
    USER,
    ADMIN
}
```

> **💡 Not:** `Role`'ü ayrı dosyaya (`Role.java`) taşıman iyi olur. Şu an `User.java` içinde tanımlı.

### BaseEntity ne sağlıyor?

`createdAt`, `updatedAt`, `createdBy`, `updatedBy` alanları. JWT tamamlandığında `createdBy` otomatik dolacak (**Adım 10**).

---

## Adım 3 — Register (/register)

Bu adımda **kayıt olma** ile ilgili her şeyi tek yerde topluyoruz.

### 3.1 Register ne yapar?

Kullanıcı email + şifre + isim gönderir → veritabanına yeni user kaydedilir → şifre **asla düz metin** saklanmaz.

```
POST /api/auth/register
Body: { email, password, firstName, lastName }
        │
        ▼
AuthController.register()
        │
        ▼
AuthService.register()
        │
        ├── Email daha önce var mı? → varsa hata
        ├── User entity oluştur
        ├── Şifreyi BCrypt ile hash'le
        └── Veritabanına kaydet
        │
        ▼
Response: { id, email, firstName, lastName }
```

### 3.2 PasswordEncoder nedir?

Şifreleri veritabanına **hash** olarak kaydeder. Hash tek yönlüdür — geri çevrilemez.

```java
// Kayıt olurken:
String hash = passwordEncoder.encode("12345678");
// → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."

// Login olurken karşılaştırma:
passwordEncoder.matches("12345678", hash);  // true veya false
```

SecurityConfig'e bean ekle (henüz eklemediysen):

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 3.3 Register için gerekli dosyalar

| Dosya | Görevi |
|-------|--------|
| `RegisterRequest.java` | Gelen JSON (email, password, firstName, lastName) |
| `RegisterResponse.java` | Dönen JSON (id, email, ...) |
| `AuthRepository.java` | `save()`, `existsByEmail()`, `findByEmail()` |
| `UserMapper.java` | Request → Entity, Entity → Response |
| `AuthService.java` | İş mantığı |
| `AuthController.java` | HTTP endpoint |

### 3.4 RegisterRequest (✅ yaptın)

```java
@Getter
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    @NotBlank @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank @Size(min = 2, max = 50)
    private String lastName;
}
```

### 3.5 AuthService.register() (✅ yaptın)

```java
@Transactional
public RegisterResponse register(RegisterRequest request) {

    // 1. Email kontrolü
    if (authRepository.existsByEmail(request.getEmail())) {
        throw new EmailAlreadyExistException(request.getEmail());
    }

    // 2. DTO → Entity
    User user = userMapper.toEntity(request);

    // 3. Şifreyi hash'le — DÜZ METİN ASLA KAYDETME
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    // 4. Varsayılan rol (UserMapper'da veya burada)
    user.setRole(Role.USER);

    // 5. Kaydet ve dön
    User saved = authRepository.save(user);
    return userMapper.toRegisterResponse(saved);
}
```

**Satır satır:**

| Satır | Ne yapıyor? |
|-------|-------------|
| `existsByEmail` | Aynı email varsa kayıt engellenir |
| `userMapper.toEntity` | RegisterRequest → User entity |
| `passwordEncoder.encode` | Şifreyi BCrypt hash'e çevirir |
| `setRole(USER)` | Yeni kullanıcı varsayılan USER rolü alır |
| `save` | PostgreSQL'e INSERT |

### 3.6 AuthController.register() (✅ yaptın)

```java
@PostMapping("/register")
public ResponseEntity<BaseResponse<RegisterResponse>> register(
        @RequestBody RegisterRequest request) {

    RegisterResponse response = authService.register(request);
    return ResponseEntity.ok(
        BaseResponse.success(200, "Başarıyla oluşturuldu", response)
    );
}
```

### 3.7 Register test

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "oktay@test.com",
  "password": "12345678",
  "firstName": "Oktay",
  "lastName": "Cavus"
}
```

Beklenen: 200 OK, veritabanında `users` tablosunda hash'lenmiş şifre.

> **💡 Register tamamlandı.** Login'e geçmeden önce **Adım 4**'te UserDetailsService yazman lazım — Spring Security login'i bu interface üzerinden çalıştırır.

---

## Adım 4 — UserDetails ve UserDetailsService

Login'e geçmeden önce bunu anlaman şart. Yoksa AuthenticationManager ne yaptığını anlayamazsın.

### 4.1 Problem: Spring Security User entity'ni tanımıyor

Senin `User` entity'n JPA için yazıldı. Spring Security ise kendi `UserDetails` interface'ini kullanır:

```java
public interface UserDetails {
    String getUsername();      // email
    String getPassword();      // hash
    Collection<? extends GrantedAuthority> getAuthorities();  // roller
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```

**Neden ayrı?** Entity = veritabanı. UserDetails = güvenlik. Karıştırma.

### 4.2 CustomUserDetails — sen yazacaksın

`User` entity'ni `UserDetails`'a saran sınıf:

```java
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public String getUsername() {
        return user.getEmail();  // Spring Security "username" der, sen email kullanıyorsun
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // BCrypt hash
    }

//! Kullanıcının sahip olduğu yetkileri spring security'ye bildiriyor
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        // USER → "ROLE_USER", ADMIN → "ROLE_ADMIN"
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
```

> **💡 `ROLE_` prefix:** Spring Security `hasRole("ADMIN")` dediğinde arka planda `ROLE_ADMIN` arar. Bu yüzden `"ROLE_" + role.name()` yazıyorsun.

### 4.3 CustomUserDetailsService — sen yazacaksın

Email ile kullanıcıyı veritabanından yükler:

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = authRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        return new CustomUserDetails(user);
    }
}
```

### 4.4 İlişki diyagramı

```
Veritabanı (users)
        │
        ▼
User entity
        │
        ▼
CustomUserDetails (UserDetails)
        │
        ├── getUsername() → email
        ├── getPassword() → hash
        └── getAuthorities() → [ROLE_USER]
        │
        ▼
Authentication.getPrincipal()
```

### 4.5 Bu adımı neden login'den önce yazıyoruz?

Çünkü **AuthenticationManager** login sırasında otomatik olarak `UserDetailsService.loadUserByUsername()` çağırır. JWT filter da her istekte aynı servisi kullanır. Tek kaynak, tutarlı sistem.

---

## Adım 5 — Login (/login) ve AuthenticationManager

Bu adımda **login ile ilgili her şey** burada.

### 5.1 Login ne yapar?

Kullanıcı email + şifre gönderir → doğruysa **JWT token** döner.

```
POST /api/auth/login
Body: { email, password }
        │
        ▼
AuthController.login()
        │
        ▼
AuthService.login()
        │
        ├── AuthenticationManager → user bul + şifre doğrula
        ├── JWT üret
        └── Token döndür
        │
        ▼
Response: { token: "eyJhbGciOiJIUzI1NiIs..." }
```

### 5.2 AuthenticationManager nedir?

**Basit cevap:** Spring Security'nin "giriş yapmayı dene" dediğin kapısı.

Sen tek satır yazarsın:

```java
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(email, password)
);
```

Spring arka planda şunları **senin yerine** yapar:

```
authenticationManager.authenticate(token)
        │
        ▼
AuthenticationProvider
        │
        ├── UserDetailsService.loadUserByUsername(email)  → DB'den user
        ├── PasswordEncoder.matches(sifre, hash)           → şifre doğru mu?
        │
        ├── Doğruysa → Authentication nesnesi döner ✅
        └── Yanlışsa → BadCredentialsException fırlatır ❌
```

### 5.3 "AuthenticationManager'a devret" ne demek?

Dokümandaki o yorum şunu kastediyor: User bulma + şifre kontrol + exception fırlatma işini **elle yazma**, `authenticationManager.authenticate(...)` çağır.

**Senin şu anki kod (elle yapıyorsun):**

```java
// AuthService.login() — mevcut hali
User user = authRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new UserNotFoundException(...));

if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new InvalidPasswordException(...);
}
```

**Spring Security yolu (devret):**

```java
// AuthService.login() — hedef hali
Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getEmail(),
        request.getPassword()
    )
);
// Buraya geldiyse email + şifre doğru demektir
```

| Elle (şu anki) | AuthenticationManager |
|----------------|------------------------|
| `findByEmail` yazarsın | UserDetailsService yapar |
| `passwordEncoder.matches` yazarsın | AuthenticationProvider yapar |
| Kendi exception'ların | Spring'in `BadCredentialsException` |
| JWT filter ile farklı mantık | Aynı UserDetailsService her yerde |

### 5.4 AuthenticationManager bean'i

SecurityConfig'e ekle:

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

AuthService'e inject et:

```java
private final AuthenticationManager authenticationManager;
```

### 5.5 LoginRequest (✅ yaptın)

```java
@Getter
public class LoginRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}
```

### 5.6 LoginResponse — sen güncelleyeceksin

Login artık token döndürmeli:

```java
@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";  // opsiyonel
}
```

### 5.7 AuthService.login() — hedef kod

```java
@Transactional(readOnly = true)
public LoginResponse login(LoginRequest request) {

    // 1. Spring Security'ye devret: user bul + şifre doğrula
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );

    // 2. Principal = CustomUserDetails
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // 3. JWT üret (Adım 6'da yazacaksın)
    String token = jwtService.generateToken(userDetails);

    // 4. Dön
    return new LoginResponse(token);
}
```

**Satır satır:**

| Satır | Ne yapıyor? |
|-------|-------------|
| `authenticate(...)` | Email/şifre doğrulanır; yanlışsa exception |
| `getPrincipal()` | Doğrulanmış kullanıcı (CustomUserDetails) |
| `generateToken(...)` | JWT string üretilir |
| `new LoginResponse(token)` | Client'a token gönderilir |

### 5.8 AuthController.login() (✅ yaptın, sadece response değişecek)

```java
@PostMapping("/login")
public ResponseEntity<BaseResponse<LoginResponse>> login(
        @RequestBody LoginRequest request) {

    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(
        BaseResponse.success(200, "Başarıyla giriş yapıldı", response)
    );
}
```

### 5.9 Login test

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "oktay@test.com",
  "password": "12345678"
}
```

Beklenen:

```json
{
  "status": 200,
  "message": "Başarıyla giriş yapıldı",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

> **💡 Login henüz korumalı endpoint'leri açmaz.** Token'ı aldın ama henüz hiçbir istekte kullanılmıyor. Bunun için Adım 6 (JwtService) ve Adım 8 (JwtAuthenticationFilter) lazım.

---

## Adım 6 — JwtService (Token Üretme)

### 6.1 JWT nedir?

Login başarılı olunca client'a verdiğin **imzalı kimlik belgesi**.

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJva3RheUB0ZXN0LmNvbSIsImV4cCI6MTc...xxx.SflKxwRJSMeKKF2...
        │                              │                                    │
      HEADER                        PAYLOAD                            SIGNATURE
```

| Bölüm | İçerik | Şifreli mi? |
|-------|--------|-------------|
| Header | Algoritma (HS256) | Hayır — Base64 |
| Payload | email, süre (exp) | Hayır — Base64 |
| Signature | Header+Payload+Secret ile imza | **Evet — bu güvenliği sağlar** |

> **⚠️ Payload'a şifre koyma.** Herkes okuyabilir. Sadece email ve süre yeter.

### 6.2 Token nasıl oluşur?

```
Login başarılı
        │
        ▼
JwtService.generateToken(userDetails)
        │
        ├── Header: { alg: HS256, typ: JWT }
        ├── Payload: { sub: email, iat: now, exp: now+1saat }
        ├── Secret key ile imzala
        └── header.payload.signature birleştir
        │
        ▼
Client token'ı saklar
```

### 6.3 pom.xml dependency

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 6.4 application.properties

```properties
jwt.secret=test-jwt-secret-for-unit-tests-only-256bit-minimum-length-required123456789
jwt.expiration=3600000
```

`jwt.expiration` = 1 saat (milisaniye).

### 6.5 JwtService — sen yazacaksın

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    // ── LOGIN'DE ÇAĞRILIR: token üret ──
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())   // email
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    // ── FILTER'DA ÇAĞRILIR: token'dan email çıkar ──
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ── FILTER'DA ÇAĞRILIR: token geçerli mi? ──
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            Base64.getEncoder().encodeToString(secretKey.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
```

**Metotların görevi:**

| Metot | Ne zaman? | Ne yapar? |
|-------|-----------|-----------|
| `generateToken` | Login'de | Email + süre → imzalı token |
| `extractUsername` | Her istekte (filter) | Token'dan email okur |
| `isTokenValid` | Her istekte (filter) | İmza + süre + email kontrol |

### 6.6 Client token'ı nasıl kullanır?

Login'den aldığı token'ı sonraki isteklerde header'a koyar:

```http
GET http://localhost:8080/api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

> **💡 JwtService sadece token üretir ve doğrular.** Token'ı her istekte okuyup SecurityContext'e koyma işi **JwtAuthenticationFilter**'ın işi (Adım 8).

---

## Adım 7 — SecurityConfig Güncelle (Endpoint Kuralları)

JWT filter yazmadan önce hangi URL'nin açık, hangisinin korumalı olduğunu belirle.

### 7.1 Hedef SecurityConfig

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**Satır satır:**

| Satır | Ne demek? |
|-------|-----------|
| `STATELESS` | Session yok; her istekte JWT gelmeli |
| `permitAll()` | Register/login herkese açık |
| `hasRole("ADMIN")` | Admin endpoint'leri sadece ADMIN |
| `authenticated()` | Diğer her şey JWT gerektirir |
| `addFilterBefore(...)` | JWT filter'ı zincire ekle |

> **💡 JwtAuthenticationFilter henüz yoksa** SecurityConfig'i güncelleme — önce Adım 8'i yaz, sonra burayı birleştir.

---

## Adım 8 — JwtAuthenticationFilter

### 8.1 Filter nedir? (kısa)

Controller'a ulaşmadan **önce** çalışan ara katman. JWT filter her istekte token'ı okur ve SecurityContext'i doldurur.

```
Request
    │
    ▼
JwtAuthenticationFilter   ← token oku, doğrula, context doldur
    │
    ▼
AuthorizationFilter         ← "authenticated mi?" kontrol
    │
    ▼
Controller
```

### 8.2 OncePerRequestFilter

JWT filter'ın `OncePerRequestFilter`'dan extend etmesi gerekir — her HTTP isteğinde **tam bir kez** çalışır.

### 8.3 JwtAuthenticationFilter — sen yazacaksın

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization header var mı?
        final String authHeader = request.getHeader("Authorization");

        // 2. "Bearer " ile başlamıyorsa → token yok, devam et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Token'ı al ("Bearer " = 7 karakter)
        final String jwt = authHeader.substring(7);

        // 4. Zaten auth varsa tekrar yapma
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Token'dan email çıkar
        final String email = jwtService.extractUsername(jwt);

        // 6. Email varsa user yükle ve token doğrula
        if (email != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Authentication oluştur
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                // 8. ★ MERKEZ ★ SecurityContext'e koy
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Sıradaki filter'a / Controller'a devam et
        filterChain.doFilter(request, response);
    }
}
```

**Satır satır:**

| # | Kod | Neden? |
|---|-----|--------|
| 1 | `getHeader("Authorization")` | Token burada taşınır |
| 2 | `startsWith("Bearer ")` | JWT standardı |
| 3 | `substring(7)` | Sadece token kısmını al |
| 5 | `extractUsername(jwt)` | Payload'dan email oku |
| 6 | `loadUserByUsername` | DB'den güncel user + roller |
| 6 | `isTokenValid` | İmza + süre kontrol |
| 8 | **`setAuthentication(...)`** | **Bundan sonra her yerden kim giriş yapmış bilinir** |
| 9 | `filterChain.doFilter(...)` | **Olmazsa istek Controller'a ulaşmaz** |

### 8.4 filterChain.doFilter() neden şart?

Filter zincirindesin. İşini bitirince sıradakine paslamalısın:

```
JwtAuthenticationFilter
    → işini yap
    → filterChain.doFilter()   ← BUNU ÇAĞIRMAZSAN İSTEK TAKILIR
        → AuthorizationFilter
            → Controller
```

---

## Adım 9 — SecurityContext (Artık Anlam Kazanır)

Adım 8'i yazdıktan sonra bu kavramlar oturur.

### 9.1 Hiyerarşi

```
SecurityContextHolder          ← her yerden erişim (ThreadLocal)
        │
        ▼
SecurityContext                ← bir kutu
        │
        ▼
Authentication                 ← giriş yapmış kullanıcı
        │
        ├── Principal            ← CustomUserDetails (email, roller)
        └── Authorities        ← [ROLE_USER]
```

### 9.2 setAuthentication vs getAuthentication

```java
// JWT filter YAZAR (Adım 8):
SecurityContextHolder.getContext().setAuthentication(authToken);

// Controller / Service / AuditorAware OKUR:
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String email = auth.getName();
```

**setAuthentication çağrılmadan getAuthentication anlamsız olur** — null veya anonymous döner.

### 9.3 ThreadLocal nedir?

Her HTTP isteği ayrı thread'de işlenir. Ali'nin token'ı Ayşe'ninkine karışmaz:

```
Thread-1 (Ali)  → SecurityContext: oktay@test.com
Thread-2 (Ayşe) → SecurityContext: ayse@test.com
```

Bu yüzden Controller → Service → Repository zincirinde her metoda `User user` parametresi taşımana gerek yok.

---

## Adım 10 — AuditorAware Güncelle

### 10.1 Ne işe yarar?

`Product` kaydedilirken `createdBy` alanını otomatik doldurur.

```
POST /api/products (JWT ile)
        │
        ▼
JwtAuthenticationFilter → setAuthentication(oktay@test.com)
        │
        ▼
ProductService.save(product)
        │
        ▼
AuditingEntityListener → getCurrentAuditor() → "oktay@test.com"
        │
        ▼
INSERT ... created_by = 'oktay@test.com'
```

### 10.2 Mevcut hali (sabit SYSTEM)

```java
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("SYSTEM");
    }
}
```

### 10.3 Hedef hali — sen güncelleyeceksin

```java
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return Optional.of(authentication.getName());  // email
    }
}
```

> **💡 Bu kod ancak JWT filter `setAuthentication` yaptıktan sonra anlamlı çalışır.**

---

## Adım 11 — Baştan Sona Tam Akış

### Senaryo: Register → Login → GET /products

#### A) Register

```
POST /api/auth/register  { email, password, firstName, lastName }
    → SecurityConfig: permitAll ✅
    → AuthController → AuthService
    → passwordEncoder.encode() → DB'ye kaydet
    → 200 OK
```

#### B) Login

```
POST /api/auth/login  { email, password }
    → SecurityConfig: permitAll ✅
    → AuthService.login()
        → authenticationManager.authenticate()  (user bul + şifre doğrula)
        → jwtService.generateToken()
    → { token: "eyJ..." }
```

#### C) GET /products (token ile)

```
GET /api/products
Header: Authorization: Bearer eyJ...
    │
    ▼
JwtAuthenticationFilter
    → token oku
    → email çıkar
    → UserDetailsService.loadUserByUsername()
    → jwtService.isTokenValid() ✅
    → setAuthentication(authToken)  ★
    │
    ▼
AuthorizationFilter
    → authenticated() ✅
    │
    ▼
ProductController → ProductService → ProductRepository → PostgreSQL
    │
    ▼
200 OK [ ürün listesi ]
```

---

## Sözlük

| Terim | Basit açıklama |
|-------|----------------|
| **Authentication** | Kimlik doğrulama — "Sen kimsin?" |
| **Authorization** | Yetkilendirme — "Girebilir misin?" |
| **AuthenticationManager** | Login denemesini yöneten Spring Security kapısı |
| **UserDetails** | Spring Security'nin kullanıcı modeli |
| **UserDetailsService** | Email ile user yükleyen servis |
| **PasswordEncoder** | Şifre hash'leme (BCrypt) |
| **JWT** | Login sonrası verilen imzalı token |
| **JwtService** | Token üretme ve doğrulama |
| **JwtAuthenticationFilter** | Her istekte token okuyup SecurityContext dolduran filter |
| **SecurityContextHolder** | "Şu an kim giriş yapmış?" sorusunun cevabı |
| **setAuthentication** | Context'e kullanıcı koy — **tüm sistemin merkezi** |
| **Filter Chain** | İstek Controller'a gitmeden geçtiği filter sırası |
| **permitAll** | Login gerektirmez |
| **authenticated** | Geçerli JWT/login gerekir |
| **AuditorAware** | createdBy/updatedBy otomatik doldurma |

---

> **Kaynak:** [Spring Security 6.5 Reference](https://docs.spring.io/spring-security/reference/6.5/index.html)

> **Sonraki adımın:** Adım 4 — `CustomUserDetails` ve `CustomUserDetailsService` yaz. Sonra Adım 5'te login'i AuthenticationManager'a geçir.
