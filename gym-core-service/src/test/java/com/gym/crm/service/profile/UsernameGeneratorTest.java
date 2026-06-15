package com.gym.crm.service.profile;

import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generate_whenBaseUsernameExistsInTrainees_returnsUsernameWithSuffix1() {
        when(traineeRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(false);

        String actual = usernameGenerator.generate("Abdul", "Hariton");

        assertEquals("Abdul.Hariton1", actual);
    }

    @Test
    void generate_whenSuffix1AlsoExistsInTrainees_returnsUsernameWithSuffix2() {
        when(traineeRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton2")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton2")).thenReturn(false);

        String actual = usernameGenerator.generate("Abdul", "Hariton");

        assertEquals("Abdul.Hariton2", actual);
    }

    @Test
    void generate_whenBaseUsernameExistsInTrainers_returnsUsernameWithSuffix1() {
        when(traineeRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(false);

        String actual = usernameGenerator.generate("Abdul", "Hariton");

        assertEquals("Abdul.Hariton1", actual);
    }

    @Test
    void generate_whenBaseUsernameExistsInBoth_returnsUsernameWithSuffix2() {
        when(traineeRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton2")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton2")).thenReturn(false);

        String actual = usernameGenerator.generate("Abdul", "Hariton");

        assertEquals("Abdul.Hariton2", actual);
    }

    @Test
    void generate_whenMultipleSuffixesExistAcrossBoth_returnsNextAvailableSuffix() {
        when(traineeRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton1")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton2")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton3")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton3")).thenReturn(true);
        when(traineeRepository.existsByUserUsername("Abdul.Hariton4")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton4")).thenReturn(false);

        String actual = usernameGenerator.generate("Abdul", "Hariton");

        assertEquals("Abdul.Hariton4", actual);
    }

    @Test
    void generate_whenDifferentNameExists_returnsBaseUsernameWithoutSuffix() {
        when(traineeRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(false);
        when(trainerRepository.existsByUserUsername("Abdul.Hariton")).thenReturn(false);

        String actual = usernameGenerator.generate("Abdul", "Hariton");

        assertEquals("Abdul.Hariton", actual);
    }
}