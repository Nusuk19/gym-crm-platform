package com.gym.crm.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.facade.GymFacade;
import com.gym.crm.core.security.GymUserDetailsService;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.security.TokenBlacklistService;
import com.gym.crm.core.util.JsonResourceReader;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainingControllerTest {

    private static final String BASE_URL = "/api/v1/trainings";

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
    void addTraining_shouldReturnOk_whenRequestIsValid() throws Exception {
        String request = JsonResourceReader.readResource("/json/training/training-create-request.json");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(facade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenTraineeUsernameIsMissing() throws Exception {
        TrainingCreateRequest request = buildCreateRequest();
        request.setTraineeUsername(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenTrainerUsernameIsMissing() throws Exception {
        TrainingCreateRequest request = buildCreateRequest();
        request.setTrainerUsername(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenTrainingNameIsMissing() throws Exception {
        TrainingCreateRequest request = buildCreateRequest();
        request.setTrainingName(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenTrainingDateIsMissing() throws Exception {
        TrainingCreateRequest request = buildCreateRequest();
        request.setTrainingDate(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenTrainingDurationIsMissing() throws Exception {
        TrainingCreateRequest request = buildCreateRequest();
        request.setTrainingDuration(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void getTrainingTypes_shouldReturnList_whenTypesExist() throws Exception {
        String expectedResponse = JsonResourceReader.readResource("/json/training/training-types-response.json");
        List<TrainingTypeResponse> response = List.of(
                buildTrainingTypeResponse(1L, "BOXING"),
                buildTrainingTypeResponse(2L, "CARDIO"));
        when(facade.findAllTrainingTypes()).thenReturn(response);

        String actualResponse = mockMvc.perform(get(BASE_URL + "/types"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.STRICT);
        verify(facade).findAllTrainingTypes();
    }

    @Test
    void getTrainingTypes_shouldReturnEmptyList_whenNoTypesExist() throws Exception {
        when(facade.findAllTrainingTypes()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(facade).findAllTrainingTypes();
    }

    private TrainingCreateRequest buildCreateRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername("Abdul.Hariton");
        request.setTrainerUsername("Mike.Tyson");
        request.setTrainingName("Boxing basics");
        request.setTrainingDate(LocalDate.of(2024, 5, 1));
        request.setTrainingDuration(60);

        return request;
    }

    private TrainingTypeResponse buildTrainingTypeResponse(long id, String name) {
        TrainingTypeResponse response = new TrainingTypeResponse();
        response.setId(id);
        response.setName(name);

        return response;
    }
}