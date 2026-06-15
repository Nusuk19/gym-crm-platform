package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTraineeResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.security.GymUserDetailsService;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.TokenBlacklistService;
import com.gym.crm.util.JsonResourceReader;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerControllerTest {

    private static final String USERNAME = "Mike.Tyson";
    private static final String TRAINEE_USERNAME = "Abdul.Hariton";
    private static final String BASE_URL = "/api/v1/trainers";
    private static final String FIRST_NAME = "Mike";
    private static final String LAST_NAME = "Tyson";
    private static final String PASSWORD = "password123";
    private static final String SPECIALIZATION = "BOXING";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade facade;

    @MockBean
    private GymUserDetailsService gymUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void register_shouldReturnCredentials_whenRequestIsValid() throws Exception {
        String request = JsonResourceReader.readResource("/json/trainer/trainer-create-request.json");
        String expectedResponse = JsonResourceReader.readResource("/json/trainer/trainer-create-response.json");
        TrainerCreateResponse response = new TrainerCreateResponse();
        response.setUsername(USERNAME);
        response.setPassword(PASSWORD);
        when(facade.createTrainer(any(TrainerCreateRequest.class))).thenReturn(response);

        String actualResponse = mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.STRICT);
        verify(facade).createTrainer(any(TrainerCreateRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenFirstNameIsMissing() throws Exception {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setLastName(LAST_NAME);
        request.setSpecialization(SPECIALIZATION);

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void register_shouldReturnBadRequest_whenLastNameIsMissing() throws Exception {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setFirstName(FIRST_NAME);
        request.setSpecialization(SPECIALIZATION);

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void getTrainerProfile_shouldReturnTrainer_whenExists() throws Exception {
        String expectedResponse = JsonResourceReader.readResource("/json/trainer/trainer-get-response.json");
        TrainerGetResponse response = buildGetResponse();
        when(facade.getTrainerByUsername(USERNAME)).thenReturn(response);

        String actualResponse = mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.STRICT);
        verify(facade).getTrainerByUsername(USERNAME);
    }

    @Test
    void getTrainerProfile_shouldReturnBadRequest_whenUsernameFormatIsInvalid() throws Exception {
        mockMvc.perform(get(BASE_URL + "/invalid_format"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void updateTrainerProfile_shouldReturnUpdatedTrainer_whenRequestIsValid() throws Exception {
        TrainerUpdateRequest request = buildUpdateRequest();
        TrainerUpdateResponse response = buildUpdateResponse();

        when(facade.updateTrainer(eq(USERNAME), any(TrainerUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.specialization").value(SPECIALIZATION))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainees").isArray());

        verify(facade).updateTrainer(eq(USERNAME), any(TrainerUpdateRequest.class));
    }

    @Test
    void updateTrainerProfile_shouldReturnBadRequest_whenFirstNameIsMissing() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setLastName(LAST_NAME);
        request.isActive(true);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void getTrainerTrainings_shouldReturnList_whenNoFilters() throws Exception {
        List<GetTrainerTrainingResponse> response = List.of(buildTrainingResponse());

        when(facade.findTrainingsByTrainerCriteria(USERNAME, null, null, null))
                .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Evening Boxing"))
                .andExpect(jsonPath("$[0].trainingType").value(SPECIALIZATION))
                .andExpect(jsonPath("$[0].traineeName").value(TRAINEE_USERNAME));

        verify(facade).findTrainingsByTrainerCriteria(USERNAME, null, null, null);
    }

    @Test
    void getTrainerTrainings_shouldPassFilters_whenProvided() throws Exception {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        when(facade.findTrainingsByTrainerCriteria(USERNAME, from, to, TRAINEE_USERNAME))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-12-31")
                        .param("traineeName", TRAINEE_USERNAME))
                .andExpect(status().isOk());

        verify(facade).findTrainingsByTrainerCriteria(USERNAME, from, to, TRAINEE_USERNAME);
    }

    @Test
    void changeActivationStatus_shouldReturnOk_whenActivating() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest(true);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).changeTrainerActivationStatus(eq(USERNAME), any(ActivationStatusRequest.class));
    }

    @Test
    void changeActivationStatus_shouldReturnOk_whenDeactivating() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest(false);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).changeTrainerActivationStatus(eq(USERNAME), any(ActivationStatusRequest.class));
    }

    @Test
    void changeActivationStatus_shouldReturnBadRequest_whenIsActiveIsMissing() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    private TrainerUpdateRequest buildUpdateRequest() {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);
        request.isActive(true);

        return request;
    }

    private TrainerGetResponse buildGetResponse() {
        TrainerGetResponse response = new TrainerGetResponse();
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setSpecialization(SPECIALIZATION);
        response.isActive(true);
        response.setTrainees(List.of(buildAssignedTraineeResponse()));

        return response;
    }

    private TrainerUpdateResponse buildUpdateResponse() {
        TrainerUpdateResponse response = new TrainerUpdateResponse();
        response.setUsername(USERNAME);
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setSpecialization(SPECIALIZATION);
        response.isActive(true);
        response.setTrainees(List.of(buildAssignedTraineeResponse()));

        return response;
    }

    private AssignedTraineeResponse buildAssignedTraineeResponse() {
        AssignedTraineeResponse response = new AssignedTraineeResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setFirstName("Abdul");
        response.setLastName("Hariton");

        return response;
    }

    private GetTrainerTrainingResponse buildTrainingResponse() {
        GetTrainerTrainingResponse response = new GetTrainerTrainingResponse();
        response.setTrainingName("Evening Boxing");
        response.setTrainingDate(LocalDate.of(2024, 5, 1));
        response.setTrainingType(SPECIALIZATION);
        response.setTrainingDuration(60);
        response.setTraineeName(TRAINEE_USERNAME);

        return response;
    }
}