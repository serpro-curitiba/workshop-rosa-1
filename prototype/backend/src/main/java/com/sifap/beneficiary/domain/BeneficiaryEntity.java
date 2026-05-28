package com.sifap.beneficiary.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "beneficiary")
public class BeneficiaryEntity {

    private static final int MAX_DEPENDENTS = 5;
    private static final int SUSPENSION_AGE_THRESHOLD = 75;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BeneficiaryStatus status;

    @Column(name = "cod_region", nullable = false)
    private int codRegion;

    @Column(name = "family_members", nullable = false)
    private int familyMembers;

    @Column(name = "family_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal familyIncome;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DependentEntity> dependents = new ArrayList<>();

    protected BeneficiaryEntity() {
    }

    private BeneficiaryEntity(String cpf, String name, LocalDate birthDate, BeneficiaryStatus status,
            int codRegion, int familyMembers, BigDecimal familyIncome) {
        this.cpf = Objects.requireNonNull(cpf, "cpf must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.birthDate = Objects.requireNonNull(birthDate, "birthDate must not be null");
        this.status = status == null ? BeneficiaryStatus.ACTIVE : status;
        this.codRegion = codRegion;
        this.familyMembers = familyMembers;
        this.familyIncome = Objects.requireNonNull(familyIncome, "familyIncome must not be null");
    }

    public static BeneficiaryEntity create(String cpf, String name, LocalDate birthDate, BeneficiaryStatus status,
            int codRegion, int familyMembers, BigDecimal familyIncome) {
        return new BeneficiaryEntity(cpf, name, birthDate, status, codRegion, familyMembers, familyIncome);
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        applyAutomaticSuspensionIfNeeded();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        applyAutomaticSuspensionIfNeeded();
    }

    public void applyAutomaticSuspensionIfNeeded() {
        if (Period.between(birthDate, LocalDate.now()).getYears() > SUSPENSION_AGE_THRESHOLD) {
            status = BeneficiaryStatus.SUSPENDED;
        }
    }

    public void addDependent(DependentEntity dependent) {
        if (status == BeneficiaryStatus.CANCELLED || status == BeneficiaryStatus.INACTIVE) {
            throw new DependentNotAllowedException("Dependents are not allowed for beneficiary status " + status);
        }
        if (dependents.size() >= MAX_DEPENDENTS) {
            throw new DependentLimitReachedException("Limite de dependentes atingido");
        }
        dependent.attachTo(this);
        dependents.add(dependent);
    }

    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public BeneficiaryStatus getStatus() {
        return status;
    }

    public int getCodRegion() {
        return codRegion;
    }

    public int getFamilyMembers() {
        return familyMembers;
    }

    public BigDecimal getFamilyIncome() {
        return familyIncome;
    }

    public List<DependentEntity> getDependents() {
        return List.copyOf(dependents);
    }
}