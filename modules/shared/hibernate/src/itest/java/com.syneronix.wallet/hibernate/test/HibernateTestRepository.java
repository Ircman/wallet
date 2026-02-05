package com.syneronix.wallet.hibernate.test;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HibernateTestRepository extends CrudRepository<HibernateTestEntity, UUID> {
}
