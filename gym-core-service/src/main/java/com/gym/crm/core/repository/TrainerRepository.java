package com.gym.crm.core.repository;

import com.gym.crm.core.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("""
            FROM Trainer t
            JOIN FETCH t.user
            JOIN FETCH t.specialization
            WHERE t.user.username = :username
            """)
    Optional<Trainer> findByUserUsername(@Param("username") String username);

    @Query("SELECT DISTINCT t FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization")
    List<Trainer> findAllWithUserAndSpecialization();

    @Query("""
            SELECT t FROM Trainer t
            JOIN FETCH t.user
            JOIN FETCH t.specialization
            WHERE t.user.isActive = true
              AND t.id NOT IN (
                  SELECT tr.id FROM Trainee tn
                  JOIN tn.trainers tr
                  WHERE tn.user.username = :traineeUsername
              )
            """)
    List<Trainer> findAllActiveNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);

    @Query("SELECT t FROM Trainer t WHERE t.user.username IN :usernames")
    List<Trainer> findAllByUserUsernameIn(@Param("usernames") List<String> usernames);

    boolean existsByUserUsername(String username);

    long countByUserIsActiveTrue();
}