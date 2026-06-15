package com.gym.crm.core.facade;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gia.openapi.model.LoginResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.core.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.dto.request.CreateTraineeRequest;
import com.gym.crm.core.dto.request.CreateTrainerRequest;
import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.dto.request.UpdateTrainerRequest;
import com.gym.crm.core.dto.request.UserCredentials;
import com.gym.crm.core.dto.response.AssignedTrainerInfo;
import com.gym.crm.core.dto.response.TraineeCreatedResponse;
import com.gym.crm.core.dto.response.TraineeProfileResponse;
import com.gym.crm.core.dto.response.TrainerCreatedResponse;
import com.gym.crm.core.dto.response.TrainerProfileResponse;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.mapper.AuthMapper;
import com.gym.crm.core.mapper.TraineeMapper;
import com.gym.crm.core.mapper.TraineeRestMapper;
import com.gym.crm.core.mapper.TrainerMapper;
import com.gym.crm.core.mapper.TrainerRestMapper;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.mapper.TrainingRestMapper;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.service.TraineeService;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.TrainingService;
import com.gym.crm.core.service.TrainingTypeService;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.AuthenticationService;
import com.gym.crm.core.service.common.CoreValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;


@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final JwtService jwtService;

    private TraineeMapper traineeMapper;
    private TraineeRestMapper traineeRestMapper;
    private TrainerMapper trainerMapper;
    private TrainerRestMapper trainerRestMapper;
    private TrainingMapper trainingMapper;
    private TrainingRestMapper trainingRestMapper;
    private AuthMapper authMapper;
    private CoreValidator coreValidator;
    private AuthenticationService authenticationService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TrainingTypeService trainingTypeService,
                     UserService userService,
                     UserProfileService userProfileService,
                     JwtService jwtService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.jwtService = jwtService;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Autowired
    public void setTraineeRestMapper(TraineeRestMapper traineeRestMapper) {
        this.traineeRestMapper = traineeRestMapper;
    }

    @Autowired
    public void setAuthMapper(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setTrainerRestMapper(TrainerRestMapper trainerRestMapper) {
        this.trainerRestMapper = trainerRestMapper;
    }

    @Autowired
    public void setTrainingMapper(TrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Autowired
    public void setTrainingRestMapper(TrainingRestMapper trainingRestMapper) {
        this.trainingRestMapper = trainingRestMapper;
    }

    @Autowired
    public void setValidationService(CoreValidator coreValidator) {
        this.coreValidator = coreValidator;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public LoginResponse login(LoginRequest request) {
        UserCredentials credentials = authMapper.toCredentials(request);
        coreValidator.validate(credentials);

        authenticationService.validateCredentials(credentials);

        String token = jwtService.generateToken(credentials.getUsername());

        return new LoginResponse()
                .username(credentials.getUsername())
                .token(token);
    }

    public void logout(String authorizationHeader) {
        authenticationService.logout(authorizationHeader);
    }

    public void changePassword(LoginChangeRequest request) {
        ChangePasswordRequest changeRequest = authMapper.toChangePassword(request);
        coreValidator.validate(changeRequest);

        UserCredentials credentials = UserCredentials.builder()
                .username(changeRequest.getUsername())
                .password(changeRequest.getOldPassword())
                .build();

        authenticationService.validateCredentials(credentials);
        userService.changePassword(changeRequest);
    }

    @Transactional
    public TraineeCreateResponse createTrainee(TraineeCreateRequest request) {
        CreateTraineeRequest internalRequest = traineeRestMapper.toCreateRequest(request);
        coreValidator.validate(internalRequest);

        String candidateUsername = userProfileService.generateUsername(
                internalRequest.getFirstName(), internalRequest.getLastName());
        trainerService.findByUsername(candidateUsername).ifPresent(existing -> {
            throw new EntityValidationException(
                    "User '%s' is already registered as a trainer".formatted(candidateUsername));
        });

        TraineeCreatedResponse response = traineeMapper.toCreatedResponse(
                traineeService.create(traineeMapper.toEntity(internalRequest)));

        return traineeRestMapper.toCreateResponse(response);
    }

    public TraineeGetResponse getTraineeByUsername(String username) {

        TraineeProfileResponse traineeProfile = traineeService.findByUsername(username)
                .map(traineeMapper::toProfileResponse)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        return traineeRestMapper.toGetResponse(traineeProfile);
    }

    public TraineeUpdateResponse updateTrainee(String username, TraineeUpdateRequest request) {
        var internalRequest = traineeRestMapper.toUpdateRequest(username, request);
        coreValidator.validate(internalRequest);

        var updated = traineeService.update(traineeMapper.toEntity(internalRequest));
        List<AssignedTrainerInfo> assignedTrainers =
                traineeMapper.toAssignedTrainerInfoList(updated.getTrainers());

        var profile = traineeMapper.toProfileResponse(updated).toBuilder()
                .trainers(assignedTrainers)
                .build();

        return traineeRestMapper.toUpdateResponse(profile);
    }

    public void deleteTraineeByUsername(String username) {
        traineeService.deleteByUsername(username);
    }

    public TraineeAssignedTrainersUpdateResponse updateTraineeTrainers(String username, TraineeAssignedTrainersUpdateRequest request) {
        List<AssignedTrainerInfo> trainers = traineeService.updateTrainers(username, request.getTrainerUsernames()).stream()
                .map(traineeMapper::toAssignedTrainerInfo)
                .toList();

        return traineeRestMapper.toAssignedTrainersUpdateResponse(trainers);
    }

    public void changeTraineeActivationStatus(String username, ActivationStatusRequest request) {
        ActivationRequest activationRequest = traineeRestMapper.toActivationRequest(username, request);

        Consumer<ActivationRequest> action = request.getIsActive()
                ? traineeService::activate
                : traineeService::deactivate;

        action.accept(activationRequest);
    }

    public TrainerCreateResponse createTrainer(TrainerCreateRequest request) {
        CreateTrainerRequest internalRequest = trainerRestMapper.toCreateRequest(request);
        coreValidator.validate(internalRequest);

        String candidateUsername = userProfileService.generateUsername(
                internalRequest.getFirstName(), internalRequest.getLastName());
        traineeService.findByUsername(candidateUsername).ifPresent(existing -> {
            throw new EntityValidationException(
                    "User '%s' is already registered as a trainee".formatted(candidateUsername));
        });

        TrainerCreatedResponse response = trainerMapper.toCreatedResponse(
                trainerService.create(trainerMapper.toEntity(internalRequest), internalRequest.getSpecializationName()));

        return trainerRestMapper.toCreateResponse(response);
    }

    public TrainerUpdateResponse updateTrainer(String username, TrainerUpdateRequest request) {
        UpdateTrainerRequest internalRequest = trainerRestMapper.toUpdateRequest(username, request);
        coreValidator.validate(internalRequest);

        TrainerProfileResponse profile = trainerMapper.toProfileResponse(
                trainerService.update(trainerMapper.toEntity(internalRequest)));

        return trainerRestMapper.toUpdateResponse(profile);
    }

    public TrainerGetResponse getTrainerByUsername(String username) {
        TrainerProfileResponse profile = trainerService.findByUsername(username)
                .map(trainerMapper::toProfileResponse)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        return trainerRestMapper.toGetResponse(profile);
    }

    public List<AssignedTrainerResponse> findAllTrainersNotAssignedToTrainee(String username) {
        return trainerService.findAllNotAssignedToTrainee(username).stream()
                .map(traineeMapper::toAssignedTrainerInfo)
                .map(traineeRestMapper::toAssignedTrainerResponse)
                .toList();
    }

    public void changeTrainerActivationStatus(String username, ActivationStatusRequest statusRequest) {
        ActivationRequest request = trainerRestMapper.toActivationRequest(username, statusRequest);

        Consumer<ActivationRequest> action = request.getIsActive()
                ? trainerService::activate
                : trainerService::deactivate;

        action.accept(request);
    }

    public void createTraining(TrainingCreateRequest request) {
        CreateTrainingRequest internalRequest = trainingRestMapper.toCreateRequest(request);
        coreValidator.validate(internalRequest);

        trainingService.create(internalRequest);
    }

    public List<TrainingTypeResponse> findAllTrainingTypes() {
        return trainingRestMapper.toTrainingTypeResponseList(trainingTypeService.findAll());
    }

    public List<GetTraineeTrainingResponse> findTrainingsByTraineeCriteria(String username, LocalDate fromDate, LocalDate toDate,
                                                                           String trainerName, String trainingType) {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .trainerFullName(trainerName)
                .trainingTypeName(trainingType)
                .build();

        return trainingService.findByTraineeCriteria(filter).stream()
                .map(trainingMapper::toResponse)
                .map(traineeRestMapper::toTraineeTrainingResponse)
                .toList();
    }

    public List<GetTrainerTrainingResponse> findTrainingsByTrainerCriteria(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .traineeFullName(traineeName)
                .build();

        return trainingService.findByTrainerCriteria(filter).stream()
                .map(trainingMapper::toResponse)
                .map(trainerRestMapper::toTrainerTrainingResponse)
                .toList();
    }
}