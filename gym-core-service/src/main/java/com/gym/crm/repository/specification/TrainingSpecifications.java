package com.gym.crm.repository.specification;

import com.gym.crm.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.model.Training;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrainingSpecifications {

    public static Specification<Training> forTraineeCriteria(TraineeTrainingSearchFilter filter) {
        return Specification
                .where(traineeUsername(filter.getUsername()))
                .and(fromDate(filter.getFromDate()))
                .and(toDate(filter.getToDate()))
                .and(trainerFullNameLike(filter.getTrainerFullName()))
                .and(trainingTypeName(filter.getTrainingTypeName()));
    }

    private static Specification<Training> traineeUsername(String username) {
        if (username == null || username.isBlank()) return null;

        return (root, query, cb) -> {
            Join<Object, Object> trainee = root.join("trainee", JoinType.LEFT);
            Join<Object, Object> user = trainee.join("user", JoinType.LEFT);

            return cb.equal(user.get("username"), username);
        };
    }

    private static Specification<Training> trainerFullNameLike(String fullName) {
        if (fullName == null || fullName.isBlank()) return null;

        return (root, query, cb) -> {
            Join<Object, Object> trainer = root.join("trainer", JoinType.LEFT);
            Join<Object, Object> user = trainer.join("user", JoinType.LEFT);
            Expression<String> fullNameExpr = cb.concat(
                    cb.concat(user.get("firstName"), " "),
                    user.get("lastName"));

            return cb.like(cb.lower(fullNameExpr), "%" + fullName.toLowerCase() + "%");
        };
    }

    private static Specification<Training> trainingTypeName(String typeName) {
        if (typeName == null || typeName.isBlank()) return null;

        return (root, query, cb) -> cb.equal(root.get("trainingType").get("trainingTypeName"), typeName);
    }

    public static Specification<Training> forTrainerCriteria(TrainerTrainingSearchFilter filter) {
        return Specification
                .where(trainerUsername(filter.getUsername()))
                .and(fromDate(filter.getFromDate()))
                .and(toDate(filter.getToDate()))
                .and(traineeFullNameLike(filter.getTraineeFullName()));
    }

    private static Specification<Training> trainerUsername(String username) {
        if (username == null || username.isBlank()) return null;

        return (root, query, cb) -> {
            Join<Object, Object> trainer = root.join("trainer", JoinType.LEFT);
            Join<Object, Object> user = trainer.join("user", JoinType.LEFT);

            return cb.equal(user.get("username"), username);
        };
    }

    private static Specification<Training> traineeFullNameLike(String fullName) {
        if (fullName == null || fullName.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Object, Object> trainee = root.join("trainee", JoinType.LEFT);
            Join<Object, Object> user = trainee.join("user", JoinType.LEFT);
            Expression<String> fullNameExpr = cb.concat(
                    cb.concat(user.get("firstName"), " "),
                    user.get("lastName"));

            return cb.like(cb.lower(fullNameExpr), "%" + fullName.toLowerCase() + "%");
        };
    }

    private static Specification<Training> fromDate(java.time.LocalDate from) {
        if (from == null) return null;

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("trainingDate"), from);
    }

    private static Specification<Training> toDate(java.time.LocalDate to) {
        if (to == null) return null;

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("trainingDate"), to);
    }
}