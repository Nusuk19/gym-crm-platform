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
import com.gym.crm.core.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.dto.request.CreateTraineeRequest;
import com.gym.crm.core.dto.request.CreateTrainerRequest;
import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.dto.request.UpdateTraineeRequest;
import com.gym.crm.core.dto.request.UpdateTrainerRequest;
import com.gym.crm.core.dto.request.UserCredentials;
import com.gym.crm.core.dto.response.AssignedTrainerInfo;
import com.gym.crm.core.dto.response.TraineeCreatedResponse;
import com.gym.crm.core.dto.response.TraineeProfileResponse;
import com.gym.crm.core.dto.response.TrainerCreatedResponse;
import com.gym.crm.core.dto.response.TrainerProfileResponse;
import com.gym.crm.core.dto.response.TrainingResponse;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.facade.GymFacade;
import com.gym.crm.core.mapper.AuthMapper;
import com.gym.crm.core.mapper.TraineeMapper;
import com.gym.crm.core.mapper.TraineeRestMapper;
import com.gym.crm.core.mapper.TrainerMapper;
import com.gym.crm.core.mapper.TrainerRestMapper;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.mapper.TrainingRestMapper;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.service.TraineeService;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.TrainingService;
import com.gym.crm.core.service.TrainingTypeService;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.AuthenticationService;
import com.gym.crm.core.service.common.CoreValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final Long EXISTING_ID = 1L;
    private static final String USERNAME = "Abdul.Hariton";
    private static final String TRAINER_USERNAME = "Mike.Tyson";
    private static final String TOKEN = "jwt-token";

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TrainingTypeService trainingTypeService;
    @Mock
    private UserService userService;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private JwtService jwtService;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private TraineeRestMapper traineeRestMapper;
    @Mock
    private TrainerRestMapper trainerRestMapper;
    @Mock
    private TrainingRestMapper trainingRestMapper;
    @Mock
    private CoreValidator coreValidator;
    @Mock
    private AuthenticationService authenticationService;

    private GymFacade facade;
    private Trainee trainee;
    private Trainer trainer;
    private Training training;
    private TraineeCreatedResponse traineeCreatedResponse;
    private TraineeProfileResponse traineeProfileResponse;
    private TrainerCreatedResponse trainerCreatedResponse;
    private TrainerProfileResponse trainerProfileResponse;
    private TrainingResponse trainingResponse;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService, trainingTypeService, userService, userProfileService, jwtService);
        facade.setTraineeMapper(traineeMapper);
        facade.setTrainerMapper(trainerMapper);
        facade.setTrainingMapper(trainingMapper);
        facade.setAuthMapper(authMapper);
        facade.setTraineeRestMapper(traineeRestMapper);
        facade.setTrainerRestMapper(trainerRestMapper);
        facade.setTrainingRestMapper(trainingRestMapper);
        facade.setValidationService(coreValidator);
        facade.setAuthenticationService(authenticationService);

        trainee = buildTrainee();
        trainer = buildTrainer();
        training = buildTraining();
        traineeCreatedResponse = buildTraineeCreatedResponse();
        traineeProfileResponse = buildTraineeProfileResponse();
        trainerCreatedResponse = buildTrainerCreatedResponse();
        trainerProfileResponse = buildTrainerProfileResponse();
        trainingResponse = buildTrainingResponse();
    }

    @Test
    void login_validatesCredentials_returnsLoginResponseWithToken() {
        LoginRequest request = new LoginRequest(USERNAME, "oldpassword1");
        UserCredentials credentials = UserCredentials.builder()
                .username(USERNAME)
                .password("oldpassword1")
                .build();

        when(authMapper.toCredentials(request)).thenReturn(credentials);
        when(jwtService.generateToken(USERNAME)).thenReturn(TOKEN);

        LoginResponse actual = facade.login(request);

        assertThat(actual.getUsername()).isEqualTo(USERNAME);
        assertThat(actual.getToken()).isEqualTo(TOKEN);
        verify(authMapper).toCredentials(request);
        verify(coreValidator).validate(credentials);
        verify(authenticationService).validateCredentials(credentials);
        verify(jwtService).generateToken(USERNAME);
    }

    @Test
    void logoutShouldClearSecurityContext() {
        String token = "jwt-token";
        String authorizationHeader = "Bearer " + token;

        facade.logout(authorizationHeader);

        verify(authenticationService).logout(authorizationHeader);
    }

    @Test
    void changePassword_validatesRequestThenAuthenticatesThenChanges() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, "oldpassword1", "newpassword1");
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .username(USERNAME)
                .oldPassword("oldpassword1")
                .newPassword("newpassword1")
                .build();

        when(authMapper.toChangePassword(request)).thenReturn(changeRequest);

        facade.changePassword(request);

        verify(authMapper).toChangePassword(request);
        verify(coreValidator).validate(changeRequest);
        ArgumentCaptor<UserCredentials> captor = ArgumentCaptor.forClass(UserCredentials.class);
        verify(authenticationService).validateCredentials(captor.capture());
        verify(userService).changePassword(changeRequest);
        assertThat(captor.getValue().getUsername()).isEqualTo(USERNAME);
        assertThat(captor.getValue().getPassword()).isEqualTo("oldpassword1");
    }

    @Test
    void createTrainee_whenValidRequest_returnsResponse() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        CreateTraineeRequest internalRequest = CreateTraineeRequest.builder()
                .firstName("Abdul")
                .lastName("Hariton")
                .build();
        TraineeCreateResponse expected = new TraineeCreateResponse();

        when(traineeRestMapper.toCreateRequest(request)).thenReturn(internalRequest);
        when(traineeMapper.toEntity(internalRequest)).thenReturn(trainee);
        when(traineeService.create(trainee)).thenReturn(trainee);
        when(traineeMapper.toCreatedResponse(trainee)).thenReturn(traineeCreatedResponse);
        when(traineeRestMapper.toCreateResponse(traineeCreatedResponse)).thenReturn(expected);

        TraineeCreateResponse actual = facade.createTrainee(request);

        assertThat(actual).isEqualTo(expected);
        verify(traineeRestMapper).toCreateRequest(request);
        verify(coreValidator).validate(internalRequest);
        verify(traineeMapper).toEntity(internalRequest);
        verify(traineeService).create(trainee);
        verify(traineeMapper).toCreatedResponse(trainee);
        verify(traineeRestMapper).toCreateResponse(traineeCreatedResponse);
    }

    @Test
    void createTrainee_whenUserAlreadyTrainer_shouldThrowEntityValidationException() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName("Yordan");
        request.setLastName("Green");
        CreateTraineeRequest internalRequest = CreateTraineeRequest.builder()
                .firstName("Yordan")
                .lastName("Green")
                .build();

        when(traineeRestMapper.toCreateRequest(request)).thenReturn(internalRequest);
        when(userProfileService.generateUsername("Yordan", "Green")).thenReturn("Yordan.Green");
        when(trainerService.findByUsername("Yordan.Green")).thenReturn(Optional.of(buildTrainer("Yordan.Green")));

        assertThatThrownBy(() -> facade.createTrainee(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Yordan.Green")
                .hasMessageContaining("already registered as a trainer");
        verify(traineeService, never()).create(any());
    }

    @Test
    void getTraineeByUsername_whenExists_returnsResponse() {
        TraineeGetResponse expected = new TraineeGetResponse();

        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(traineeProfileResponse);
        when(traineeRestMapper.toGetResponse(traineeProfileResponse)).thenReturn(expected);

        TraineeGetResponse actual = facade.getTraineeByUsername(USERNAME);

        assertThat(actual).isEqualTo(expected);
        verify(traineeService).findByUsername(USERNAME);
        verify(traineeMapper).toProfileResponse(trainee);
        verify(traineeRestMapper).toGetResponse(traineeProfileResponse);
    }

    @Test
    void updateTrainee_whenValidRequest_returnsUpdatedTraineeResponse() {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        UpdateTraineeRequest internalRequest = UpdateTraineeRequest.builder()
                .username(USERNAME)
                .firstName("Abdul")
                .lastName("Hariton")
                .isActive(true)
                .build();
        TraineeUpdateResponse expected = new TraineeUpdateResponse();
        List<AssignedTrainerInfo> trainerInfos = List.of(buildAssignedTrainerInfo());

        when(traineeRestMapper.toUpdateRequest(USERNAME, request)).thenReturn(internalRequest);
        when(traineeMapper.toEntity(internalRequest)).thenReturn(trainee);
        when(traineeService.update(trainee)).thenReturn(trainee);
        when(traineeMapper.toAssignedTrainerInfoList(trainee.getTrainers())).thenReturn(trainerInfos);
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(traineeProfileResponse);
        when(traineeRestMapper.toUpdateResponse(any(TraineeProfileResponse.class))).thenReturn(expected);

        TraineeUpdateResponse actual = facade.updateTrainee(USERNAME, request);

        assertThat(actual).isEqualTo(expected);
        verify(traineeRestMapper).toUpdateRequest(USERNAME, request);
        verify(coreValidator).validate(internalRequest);
        verify(traineeService).update(trainee);
    }

    @Test
    void deleteTraineeByUsername_callsService() {
        facade.deleteTraineeByUsername(USERNAME);

        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void updateTraineeTrainers_callsService() {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest();
        request.setTrainerUsernames(List.of("Mike.Tyson", "John.Doe"));
        AssignedTrainerInfo info = buildAssignedTrainerInfo();
        TraineeAssignedTrainersUpdateResponse expected = new TraineeAssignedTrainersUpdateResponse();

        when(traineeService.updateTrainers(USERNAME, request.getTrainerUsernames())).thenReturn(List.of(trainer));
        when(traineeMapper.toAssignedTrainerInfo(trainer)).thenReturn(info);
        when(traineeRestMapper.toAssignedTrainersUpdateResponse(List.of(info))).thenReturn(expected);

        TraineeAssignedTrainersUpdateResponse actual = facade.updateTraineeTrainers(USERNAME, request);

        assertThat(actual).isEqualTo(expected);
        verify(traineeService).updateTrainers(USERNAME, request.getTrainerUsernames());
    }

    @Test
    void changeTraineeActivationStatus_whenActive_callsActivate() {
        ActivationStatusRequest body = new ActivationStatusRequest(true);
        ActivationRequest activation = ActivationRequest.builder()
                .username(USERNAME)
                .isActive(true)
                .build();

        when(traineeRestMapper.toActivationRequest(USERNAME, body)).thenReturn(activation);

        facade.changeTraineeActivationStatus(USERNAME, body);

        verify(traineeService).activate(activation);
    }

    @Test
    void changeTraineeActivationStatus_whenInactive_callsDeactivate() {
        ActivationStatusRequest body = new ActivationStatusRequest(false);
        ActivationRequest activation = ActivationRequest.builder()
                .username(USERNAME)
                .isActive(false)
                .build();

        when(traineeRestMapper.toActivationRequest(USERNAME, body)).thenReturn(activation);

        facade.changeTraineeActivationStatus(USERNAME, body);

        verify(traineeService).deactivate(activation);
    }

    @Test
    void createTrainer_whenValidRequest_returnsTrainerCreatedResponse() {
        TrainerCreateRequest restRequest = new TrainerCreateRequest();
        CreateTrainerRequest internalRequest = CreateTrainerRequest.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .specializationName("BOXING")
                .build();
        TrainerCreateResponse expected = new TrainerCreateResponse();

        when(trainerRestMapper.toCreateRequest(restRequest)).thenReturn(internalRequest);
        when(trainerMapper.toEntity(internalRequest)).thenReturn(trainer);
        when(trainerService.create(trainer, "BOXING")).thenReturn(trainer);
        when(trainerMapper.toCreatedResponse(trainer)).thenReturn(trainerCreatedResponse);
        when(trainerRestMapper.toCreateResponse(trainerCreatedResponse)).thenReturn(expected);

        TrainerCreateResponse actual = facade.createTrainer(restRequest);

        assertThat(actual).isEqualTo(expected);
        verify(trainerRestMapper).toCreateRequest(restRequest);
        verify(coreValidator).validate(internalRequest);
        verify(trainerMapper).toEntity(internalRequest);
        verify(trainerService).create(trainer, "BOXING");
        verify(trainerMapper).toCreatedResponse(trainer);
        verify(trainerRestMapper).toCreateResponse(trainerCreatedResponse);
    }

    @Test
    void createTrainer_whenUserAlreadyTrainee_shouldThrowEntityValidationException() {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setFirstName("Yordan");
        request.setLastName("Green");
        request.setSpecialization("BOXING");
        CreateTrainerRequest internalRequest = CreateTrainerRequest.builder()
                .firstName("Yordan")
                .lastName("Green")
                .specializationName("BOXING")
                .build();

        when(trainerRestMapper.toCreateRequest(request)).thenReturn(internalRequest);
        when(userProfileService.generateUsername("Yordan", "Green")).thenReturn("Yordan.Green");
        when(traineeService.findByUsername("Yordan.Green")).thenReturn(Optional.of(buildTrainee("Yordan.Green")));

        assertThatThrownBy(() -> facade.createTrainer(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Yordan.Green")
                .hasMessageContaining("already registered as a trainee");
        verify(trainerService, never()).create(any(), any());
    }

    @Test
    void getTrainerByUsername_whenExists_returnsRestResponse() {
        TrainerGetResponse expected = new TrainerGetResponse();

        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(trainerProfileResponse);
        when(trainerRestMapper.toGetResponse(trainerProfileResponse)).thenReturn(expected);

        TrainerGetResponse actual = facade.getTrainerByUsername(TRAINER_USERNAME);

        assertThat(actual).isEqualTo(expected);
        verify(trainerService).findByUsername(TRAINER_USERNAME);
        verify(trainerMapper).toProfileResponse(trainer);
        verify(trainerRestMapper).toGetResponse(trainerProfileResponse);
    }

    @Test
    void getTrainerByUsername_whenNotExists_throwsEntityNotFoundException() {
        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getTrainerByUsername(TRAINER_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(TRAINER_USERNAME);
        verify(trainerService).findByUsername(TRAINER_USERNAME);
    }

    @Test
    void updateTrainer_whenValidRequest_returnsUpdatedTrainerResponse() {
        TrainerUpdateRequest restRequest = new TrainerUpdateRequest();
        UpdateTrainerRequest internalRequest = UpdateTrainerRequest.builder()
                .username(TRAINER_USERNAME)
                .firstName("Mike")
                .lastName("Tyson")
                .isActive(true)
                .build();
        TrainerUpdateResponse expected = new TrainerUpdateResponse();

        when(trainerRestMapper.toUpdateRequest(TRAINER_USERNAME, restRequest)).thenReturn(internalRequest);
        when(trainerMapper.toEntity(internalRequest)).thenReturn(trainer);
        when(trainerService.update(trainer)).thenReturn(trainer);
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(trainerProfileResponse);
        when(trainerRestMapper.toUpdateResponse(trainerProfileResponse)).thenReturn(expected);

        TrainerUpdateResponse actual = facade.updateTrainer(TRAINER_USERNAME, restRequest);

        assertThat(actual).isEqualTo(expected);
        verify(trainerRestMapper).toUpdateRequest(TRAINER_USERNAME, restRequest);
        verify(coreValidator).validate(internalRequest);
        verify(trainerService).update(trainer);
        verify(trainerMapper).toProfileResponse(trainer);
        verify(trainerRestMapper).toUpdateResponse(trainerProfileResponse);
    }

    @Test
    void changeTrainerActivationStatus_whenActive_callsActivate() {
        ActivationStatusRequest body = new ActivationStatusRequest(true);
        ActivationRequest activation = ActivationRequest.builder()
                .username(TRAINER_USERNAME)
                .isActive(true)
                .build();

        when(trainerRestMapper.toActivationRequest(TRAINER_USERNAME, body)).thenReturn(activation);

        facade.changeTrainerActivationStatus(TRAINER_USERNAME, body);

        verify(trainerRestMapper).toActivationRequest(TRAINER_USERNAME, body);
        verify(trainerService).activate(activation);
    }

    @Test
    void changeTrainerActivationStatus_whenInactive_callsDeactivate() {
        ActivationStatusRequest body = new ActivationStatusRequest(false);
        ActivationRequest activation = ActivationRequest.builder()
                .username(TRAINER_USERNAME)
                .isActive(false)
                .build();

        when(trainerRestMapper.toActivationRequest(TRAINER_USERNAME, body)).thenReturn(activation);

        facade.changeTrainerActivationStatus(TRAINER_USERNAME, body);

        verify(trainerRestMapper).toActivationRequest(TRAINER_USERNAME, body);
        verify(trainerService).deactivate(activation);
    }

    @Test
    void findAllTrainersNotAssignedToTrainee_returnsFilteredList() {
        AssignedTrainerInfo info = buildAssignedTrainerInfo();
        AssignedTrainerResponse assigned = new AssignedTrainerResponse();

        when(trainerService.findAllNotAssignedToTrainee(USERNAME)).thenReturn(List.of(trainer));
        when(traineeMapper.toAssignedTrainerInfo(trainer)).thenReturn(info);
        when(traineeRestMapper.toAssignedTrainerResponse(info)).thenReturn(assigned);

        List<AssignedTrainerResponse> actual = facade.findAllTrainersNotAssignedToTrainee(USERNAME);

        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).isEqualTo(assigned);
        verify(trainerService).findAllNotAssignedToTrainee(USERNAME);
    }

    @Test
    void createTraining_whenValidRequest_callsService() {
        TrainingCreateRequest restRequest = new TrainingCreateRequest();
        CreateTrainingRequest internalRequest = mock(CreateTrainingRequest.class);

        when(trainingRestMapper.toCreateRequest(restRequest)).thenReturn(internalRequest);

        facade.createTraining(restRequest);

        verify(trainingRestMapper).toCreateRequest(restRequest);
        verify(coreValidator).validate(internalRequest);
        verify(trainingService).create(internalRequest);
    }

    @Test
    void getTraineeByUsername_whenNotExists_throwsEntityNotFoundException() {
        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getTraineeByUsername(USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(USERNAME);
    }

    @Test
    void findTrainingsByTraineeCriteria_returnsFilteredList() {
        GetTraineeTrainingResponse response = new GetTraineeTrainingResponse();

        when(trainingService.findByTraineeCriteria(any())).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);
        when(traineeRestMapper.toTraineeTrainingResponse(trainingResponse)).thenReturn(response);

        List<GetTraineeTrainingResponse> actual = facade.findTrainingsByTraineeCriteria(
                USERNAME, null, null, null, null);

        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).isEqualTo(response);
        verify(trainingService).findByTraineeCriteria(any());
    }

    @Test
    void findTrainingsByTrainerCriteria_returnsFilteredList() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String traineeName = "Abdul";
        GetTrainerTrainingResponse restResponse = new GetTrainerTrainingResponse();

        when(trainingService.findByTrainerCriteria(any(TrainerTrainingSearchFilter.class))).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);
        when(trainerRestMapper.toTrainerTrainingResponse(trainingResponse)).thenReturn(restResponse);

        List<GetTrainerTrainingResponse> actual = facade.findTrainingsByTrainerCriteria(TRAINER_USERNAME, from, to, traineeName);

        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).isEqualTo(restResponse);
        ArgumentCaptor<TrainerTrainingSearchFilter> captor = ArgumentCaptor.forClass(TrainerTrainingSearchFilter.class);
        verify(trainingService).findByTrainerCriteria(captor.capture());
        TrainerTrainingSearchFilter captured = captor.getValue();
        assertThat(captured.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(captured.getFromDate()).isEqualTo(from);
        assertThat(captured.getToDate()).isEqualTo(to);
        assertThat(captured.getTraineeFullName()).isEqualTo(traineeName);
    }

    @Test
    void findTrainingsByTrainerCriteria_withNullFilters_buildsFilterWithUsernameOnly() {
        GetTrainerTrainingResponse restResponse = new GetTrainerTrainingResponse();

        when(trainingService.findByTrainerCriteria(any(TrainerTrainingSearchFilter.class))).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);
        when(trainerRestMapper.toTrainerTrainingResponse(trainingResponse)).thenReturn(restResponse);

        List<GetTrainerTrainingResponse> actual =
                facade.findTrainingsByTrainerCriteria(TRAINER_USERNAME, null, null, null);

        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).isEqualTo(restResponse);
        ArgumentCaptor<TrainerTrainingSearchFilter> captor = ArgumentCaptor.forClass(TrainerTrainingSearchFilter.class);
        verify(trainingService).findByTrainerCriteria(captor.capture());
        assertThat(captor.getValue().getFromDate()).isNull();
        assertThat(captor.getValue().getToDate()).isNull();
        assertThat(captor.getValue().getTraineeFullName()).isNull();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(EXISTING_ID)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .isActive(true)
                .build();
        return Trainee.builder()
                .id(EXISTING_ID)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(EXISTING_ID)
                .firstName("Mike")
                .lastName("Tyson")
                .username("Mike.Tyson")
                .isActive(true)
                .build();
        return Trainer.builder()
                .id(EXISTING_ID)
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();
    }

    private Trainee buildTrainee(String username) {
        return Trainee.builder()
                .user(User.builder().username(username).build())
                .build();
    }

    private Trainer buildTrainer(String username) {
        return Trainer.builder()
                .user(User.builder().username(username).build())
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .id(EXISTING_ID)
                .name("Boxing basics")
                .trainee(buildTrainee())
                .trainer(buildTrainer())
                .trainingType(TrainingType.builder().trainingTypeName("BOXING").build())
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }

    private TraineeCreatedResponse buildTraineeCreatedResponse() {
        return TraineeCreatedResponse.builder()
                .username(USERNAME)
                .password("rawPass123")
                .build();
    }

    private TraineeProfileResponse buildTraineeProfileResponse() {
        return TraineeProfileResponse.builder()
                .id(EXISTING_ID)
                .username(USERNAME)
                .firstName("Abdul")
                .lastName("Hariton")
                .isActive(true)
                .build();
    }

    private TrainerCreatedResponse buildTrainerCreatedResponse() {
        return TrainerCreatedResponse.builder()
                .username(TRAINER_USERNAME)
                .password("rawPass123")
                .build();
    }

    private TrainerProfileResponse buildTrainerProfileResponse() {
        return TrainerProfileResponse.builder()
                .id(EXISTING_ID)
                .username(TRAINER_USERNAME)
                .firstName("Mike")
                .lastName("Tyson")
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .isActive(true)
                .build();
    }

    private TrainingResponse buildTrainingResponse() {
        return TrainingResponse.builder()
                .id(EXISTING_ID)
                .traineeId(EXISTING_ID)
                .trainerId(EXISTING_ID)
                .trainingName("Boxing basics")
                .build();
    }

    private AssignedTrainerInfo buildAssignedTrainerInfo() {
        return AssignedTrainerInfo.builder()
                .username(TRAINER_USERNAME)
                .firstName("Mike")
                .lastName("Tyson")
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();
    }
}