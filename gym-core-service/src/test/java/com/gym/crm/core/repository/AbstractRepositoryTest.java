package com.gym.crm.core.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public abstract class AbstractRepositoryTest<T> {

    @Autowired
    T repository;

    @Autowired
    protected TestEntityManager em;

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }
}