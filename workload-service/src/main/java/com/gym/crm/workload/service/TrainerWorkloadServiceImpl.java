package com.gym.crm.workload.service;

import com.gym.crm.workload.logging.TransactionIdFilter;
import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import com.gym.crm.workload.openapi.TrainerMonthlyWorkloadResponse;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;
    private final Validator validator;

    @Override
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        log.debug("Processing workload update request. username={}, action={}, transactionId={}",
                request.getTrainerUsername(), request.getActionType(),
                MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY));

        TrainerWorkload workload = repository.findByUsername(request.getTrainerUsername())
                .map(existing -> {
                    log.debug("Existing trainer workload found. username={}", request.getTrainerUsername());
                    return refreshTrainer(existing, request);
                })
                .orElseGet(() -> {
                    log.debug("Trainer workload not found, creating new. username={}", request.getTrainerUsername());
                    return createWorkload(request);
                });

        updateMonthlySummary(workload, request);
        validate(workload);
        repository.save(workload);

        log.info("Trainer workload updated. username={}, actionType={}, date={}, duration={}",
                request.getTrainerUsername(),
                request.getActionType(),
                request.getTrainingDate(),
                request.getTrainingDuration());
    }

    @Override
    public TrainerMonthlyWorkloadResponse getMonthlyWorkload(String username, int year, int month) {
        log.debug("Fetching monthly workload. username={}, year={}, month={}, transactionId={}",
                username, year, month, MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY));
        TrainerWorkload workload = findWorkload(username);

        int duration = workload.getYears().stream()
                .filter(summary -> summary.getYear().equals(year))
                .flatMap(summary -> summary.getMonths().stream())
                .filter(summary -> summary.getMonth().equals(month))
                .mapToInt(MonthSummary::getTrainingSummaryDuration)
                .findFirst()
                .orElse(0);
        log.info("Monthly workload fetched. username={}, year={}, month={}, duration={}", username, year, month, duration);

        return new TrainerMonthlyWorkloadResponse(username, year, month, duration);
    }

    private void validate(TrainerWorkload workload) {
        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private TrainerWorkload findWorkload(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException(String.format("Trainer workload not found: %s", username)));
    }

    private TrainerWorkload createWorkload(TrainerWorkloadRequest request) {
        return new TrainerWorkload(
                request.getTrainerUsername(),
                request.getTrainerFirstName(),
                request.getTrainerLastName(),
                request.getIsActive(),
                new ArrayList<>());
    }

    private TrainerWorkload refreshTrainer(TrainerWorkload workload, TrainerWorkloadRequest request) {
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setIsActive(request.getIsActive());

        return workload;
    }

    private void updateMonthlySummary(TrainerWorkload workload, TrainerWorkloadRequest request) {
        LocalDate date = request.getTrainingDate();
        YearSummary yearSummary = getOrCreateYearSummary(workload.getYears(), date.getYear());
        MonthSummary monthSummary = getOrCreateMonthSummary(yearSummary.getMonths(), date.getMonthValue());

        int delta = switch (request.getActionType()) {
            case ADD -> request.getTrainingDuration();
            case DELETE -> -request.getTrainingDuration();
        };

        monthSummary.setTrainingSummaryDuration(Math.max(0, monthSummary.getTrainingSummaryDuration() + delta));
    }

    private YearSummary getOrCreateYearSummary(List<YearSummary> years, int year) {
        return years.stream()
                .filter(summary -> summary.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> createYearSummary(years, year));
    }

    private MonthSummary getOrCreateMonthSummary(List<MonthSummary> months, int month) {
        return months.stream()
                .filter(summary -> summary.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> createMonthSummary(months, month));
    }

    private MonthSummary createMonthSummary(List<MonthSummary> months, int month) {
        MonthSummary summary = MonthSummary.builder()
                .month(month)
                .trainingSummaryDuration(0)
                .build();
        months.add(summary);

        return summary;
    }

    private YearSummary createYearSummary(List<YearSummary> years, int year) {
        YearSummary summary = YearSummary.builder()
                .year(year)
                .months(new ArrayList<>())
                .build();
        years.add(summary);

        return summary;
    }
}