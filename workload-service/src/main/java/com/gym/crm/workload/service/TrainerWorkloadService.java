package com.gym.crm.workload.service;

import com.gym.crm.workload.openapi.TrainerMonthlyWorkloadResponse;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;

public interface TrainerWorkloadService {

    void updateTrainerWorkload(TrainerWorkloadRequest request);

    TrainerMonthlyWorkloadResponse getMonthlyWorkload(String username, int year, int month);
}