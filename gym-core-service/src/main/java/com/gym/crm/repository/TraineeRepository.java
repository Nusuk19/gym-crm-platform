package com.gym.crm.repository;

import com.gym.crm.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @Query("""
            SELECT DISTINCT t FROM Trainee t
            JOIN FETCH t.user
            LEFT JOIN FETCH t.trainers tr
            LEFT JOIN FETCH tr.user
            LEFT JOIN FETCH tr.specialization
            WHERE t.user.username = :username
            """)
    Optional<Trainee> findByUserUsername(@Param("username") String username);

    @Query("SELECT DISTINCT t FROM Trainee t JOIN FETCH t.user")
    List<Trainee> findAllWithUser();

    boolean existsByUserUsername(String username);

    long countByUserIsActiveTrue();
}