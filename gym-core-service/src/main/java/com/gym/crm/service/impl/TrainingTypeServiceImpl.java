package com.gym.crm.service.impl;

import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> findAll() {
        log.info("Loading all training types");

        return trainingTypeRepository.findAll();
    }
}