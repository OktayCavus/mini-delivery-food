package com.cavus.delivery_food.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
/// AuditingEntityListener.class bu createdby lastmodifiedby gibi alanları otomatik olarak doldurur.
/// 1- Entity'ye bu annotation'u ekle 
/// 2- AuditAwareImpl.java dosyasını oluştur ve getCurrentAuditor() method'unu override et.
/// 3- Auditing'i aktif etmek için bir config sınıfı oluştur (JpaConfig) ve @EnableJpaAuditing annotation'unu ekleyin.
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime  createdAt;

    @LastModifiedDate
    private LocalDateTime  updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;


    /// createdBy ve updatedBy'da var onları sonrasında araştıracağız.gzm

    /*
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

     */
}