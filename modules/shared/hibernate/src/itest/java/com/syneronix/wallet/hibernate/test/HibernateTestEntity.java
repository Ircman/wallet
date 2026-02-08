package com.syneronix.wallet.hibernate.test;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test", schema = "memory_db")
public class HibernateTestEntity {
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.UUID, generator = "uuid2")
    @Column(length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String test;
}
