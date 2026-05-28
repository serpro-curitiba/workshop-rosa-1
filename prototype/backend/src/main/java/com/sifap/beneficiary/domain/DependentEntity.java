package com.sifap.beneficiary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "dependent")
public class DependentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private BeneficiaryEntity beneficiary;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DependentEntity() {
    }

    private DependentEntity(String name, LocalDate birthDate, String relationship) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.birthDate = Objects.requireNonNull(birthDate, "birthDate must not be null");
        this.relationship = Objects.requireNonNull(relationship, "relationship must not be null");
    }

    public static DependentEntity create(String name, LocalDate birthDate, String relationship) {
        return new DependentEntity(name, birthDate, relationship);
    }

    void attachTo(BeneficiaryEntity beneficiary) {
        this.beneficiary = Objects.requireNonNull(beneficiary, "beneficiary must not be null");
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getRelationship() {
        return relationship;
    }
}