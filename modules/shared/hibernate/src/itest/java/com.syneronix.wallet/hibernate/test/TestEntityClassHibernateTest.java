package com.syneronix.wallet.hibernate.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ContextConfiguration(classes = {HibernateTestEntity.class, HibernateTestRepository.class})
@EnableJpaRepositories(value = "com.syneronix.wallet.hibernate.test")
@EntityScan(value = "com.syneronix.wallet.hibernate.test")
@ExtendWith(SpringExtension.class)
class TestEntityClassHibernateTest {

    @Autowired
    private HibernateTestRepository hibernateTestRepository;

    @Test
    void saveToDatabase() {
        // Assume
        HibernateTestEntity hibernateTestEntity = new HibernateTestEntity();
        hibernateTestEntity.setTest("Test string data");
        // Act
        HibernateTestEntity result = hibernateTestRepository.save(hibernateTestEntity);
        // Assert
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTest()).isEqualTo("Test string data");
    }
}
