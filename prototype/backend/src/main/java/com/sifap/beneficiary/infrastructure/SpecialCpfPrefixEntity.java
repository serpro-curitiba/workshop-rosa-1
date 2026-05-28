package com.sifap.beneficiary.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "special_cpf_prefix")
public class SpecialCpfPrefixEntity {

    @Id
    @Column(name = "prefix", nullable = false, length = 3)
    private String prefix;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "created_by", nullable = false, length = 200)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SpecialCpfPrefixEntity() {
    }
}