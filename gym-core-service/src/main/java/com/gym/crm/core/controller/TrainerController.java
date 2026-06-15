package com.gym.crm.core.controller;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.core.annotation.ValidUsername;
import com.gym.crm.core.facade.GymFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TrainerCreateResponse> register(@Valid @RequestBody TrainerCreateRequest request) {
        return ResponseEntity.ok(facade.createTrainer(request));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerGetResponse> getTrainerProfile(@PathVariable @ValidUsername String username) {
        return ResponseEntity.ok(facade.getTrainerByUsername(username));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PutMapping("/{username}")
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(@PathVariable @ValidUsername String username,
                                                                      @Valid @RequestBody TrainerUpdateRequest request) {
        return ResponseEntity.ok(facade.updateTrainer(username, request));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<GetTrainerTrainingResponse>> getTrainerTrainings(@PathVariable @ValidUsername String username,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                                @RequestParam(required = false) String traineeName) {
        return ResponseEntity.ok(facade.findTrainingsByTrainerCriteria(username, fromDate, toDate, traineeName));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PatchMapping("/{username}/activation")
    public ResponseEntity<Void> changeActivationStatus(@PathVariable @ValidUsername String username,
                                                       @Valid @RequestBody ActivationStatusRequest request) {
        facade.changeTrainerActivationStatus(username, request);

        return ResponseEntity.ok().build();
    }
}