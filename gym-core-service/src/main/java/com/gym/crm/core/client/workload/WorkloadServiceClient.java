package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {

    private final RestClient workloadRestClient;

    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        workloadRestClient.put()
                .uri("/trainer-workloads")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}