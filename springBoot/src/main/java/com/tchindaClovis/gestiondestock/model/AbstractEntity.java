package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.Instant;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) //écouter la classe et à chaque fois que @CreatedDate et @LastModifiedDate, elle va directement mettre à jour dans la BDD
public class AbstractEntity implements Serializable {
    @Id
    @GeneratedValue        //(strategy = GenerationType.IDENTITY)-->argument de l'annotation
    private Integer id;

    @CreatedDate
    @Column(name="creationDate", nullable = false, updatable = false)
    private Instant creationDate;

    @LastModifiedDate
    @Column(name="lastModifiedDate")
    private Instant lastModifiedDate;

//    @PrePersist
//    void prePersist(){
//        creationDate = Instant.now();
//    }
//
//    @PreUpdate
//    void preupdate(){
//        lastModifiedDate = Instant.now();
//    }
}
