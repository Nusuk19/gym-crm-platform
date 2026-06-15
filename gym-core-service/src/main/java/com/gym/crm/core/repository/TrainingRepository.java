package com.gym.crm.core.repository;

import com.gym.crm.core.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {

    @Query("""
            FROM Training t
            JOIN FETCH t.trainee
            JOIN FETCH t.trainer
            JOIN FETCH t.trainingType
            """)
    List<Training> findAllWithAssociations();
}