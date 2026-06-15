package com.gym.crm.controller;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gym.crm.annotation.ValidUsername;
import com.gym.crm.facade.GymFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("${app.api.base-path}/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TraineeCreateResponse> register(@Valid @RequestBody TraineeCreateRequest request) {
        return ResponseEntity.ok(facade.createTrainee(request));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeGetResponse> getTraineeProfile(@PathVariable @ValidUsername String username) {
        return ResponseEntity.ok(facade.getTraineeByUsername(username));
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(@PathVariable @ValidUsername String username,
                                                                      @Valid @RequestBody TraineeUpdateRequest request) {
        return ResponseEntity.ok(facade.updateTrainee(username, request));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrainee(@PathVariable @ValidUsername String username) {
        facade.deleteTraineeByUsername(username);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.principal.username")
    @GetMapping("/{username}/available-trainers")
    public ResponseEntity<List<AssignedTrainerResponse>> getAvailableTrainers(@PathVariable @ValidUsername String username) {
        return ResponseEntity.ok(facade.findAllTrainersNotAssignedToTrainee(username));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeAssignedTrainersUpdateResponse> updateTraineeTrainers(@PathVariable @ValidUsername String username,
                                                                                       @Valid @RequestBody TraineeAssignedTrainersUpdateRequest request) {
        return ResponseEntity.ok(facade.updateTraineeTrainers(username, request));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<GetTraineeTrainingResponse>> getTraineeTrainings(@PathVariable @ValidUsername String username,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                                @RequestParam(required = false) String trainerName,
                                                                                @RequestParam(required = false) String trainingType) {
        return ResponseEntity.ok(facade.findTrainingsByTraineeCriteria(username, fromDate, toDate, trainerName, trainingType));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PatchMapping("/{username}/activation")
    public ResponseEntity<Void> changeActivationStatus(@PathVariable @ValidUsername String username,
                                                       @Valid @RequestBody ActivationStatusRequest request) {
        facade.changeTraineeActivationStatus(username, request);

        return ResponseEntity.ok().build();
    }
}