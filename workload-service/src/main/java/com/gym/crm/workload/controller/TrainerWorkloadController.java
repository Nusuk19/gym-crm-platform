package com.gym.crm.workload.controller;

import com.gym.crm.workload.openapi.TrainerMonthlyWorkloadResponse;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;
import com.gym.crm.workload.service.TrainerWorkloadServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadController {

    private final TrainerWorkloadServiceImpl service;

    @PutMapping
    public ResponseEntity<Void> updateTrainerWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        service.updateTrainerWorkload(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerMonthlyWorkloadResponse> getTrainerMonthlyWorkload(@PathVariable String username,
                                                                                    @RequestParam int year,
                                                                                    @RequestParam int month) {
        TrainerMonthlyWorkloadResponse response = service.getMonthlyWorkload(username, year, month);

        return ResponseEntity.ok(response);
    }
}