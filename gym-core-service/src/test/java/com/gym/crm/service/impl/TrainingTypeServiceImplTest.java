package com.gym.crm.service.impl;

import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeServiceImpl service;

    @Test
    void findAll_whenTypesExist_returnsAllTypes() {
        List<TrainingType> types = List.of(
                TrainingType.builder().id(1L).trainingTypeName("BOXING").build(),
                TrainingType.builder().id(2L).trainingTypeName("CARDIO").build());

        when(trainingTypeRepository.findAll()).thenReturn(types);

        List<TrainingType> actual = service.findAll();

        assertEquals(types, actual);
        verify(trainingTypeRepository).findAll();
    }

    @Test
    void findAll_whenNoTypesExist_returnsEmptyList() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        List<TrainingType> actual = service.findAll();

        assertTrue(actual.isEmpty());
        verify(trainingTypeRepository).findAll();
    }
}