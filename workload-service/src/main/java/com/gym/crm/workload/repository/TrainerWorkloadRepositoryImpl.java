package com.gym.crm.workload.repository;

import com.gym.crm.workload.model.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TrainerWorkloadRepositoryImpl implements TrainerWorkloadRepository {

    private final Map<String, TrainerWorkload> trainerWorkloads = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        return Optional.ofNullable(trainerWorkloads.get(username));
    }

    @Override
    public TrainerWorkload save(TrainerWorkload trainerWorkload) {
        trainerWorkloads.put(trainerWorkload.getTrainerUsername(), trainerWorkload);

        return trainerWorkload;
    }

    @Override
    public boolean existsByUsername(String username) {
        return trainerWorkloads.containsKey(username);
    }
}