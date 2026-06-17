package com.gym.crm.workload.controller;

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
@RequestMapping("/api/v1/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadController {

    private final TrainerWorkloadServiceImpl trainerWorkloadService;

    @PutMapping
    public ResponseEntity<Void> updateTrainerWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        trainerWorkloadService.updateTrainerWorkload(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<Integer> getTrainerMonthlyWorkload(@PathVariable String username, @RequestParam int year,
                                                             @RequestParam int month) {
        int monthlyWorkload = trainerWorkloadService.getMonthlyWorkload(username, year, month);

        return ResponseEntity.ok(monthlyWorkload);
    }
}