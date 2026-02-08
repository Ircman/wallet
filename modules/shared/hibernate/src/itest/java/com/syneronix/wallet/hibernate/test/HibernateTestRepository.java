package com.syneronix.wallet.hibernate.test;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HibernateTestRepository extends CrudRepository<HibernateTestEntity, UUID> {
}
