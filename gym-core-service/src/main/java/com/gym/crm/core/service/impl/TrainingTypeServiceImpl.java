package com.gym.crm.core.service.impl;

import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.service.TrainingTypeService;
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