package com.gym.crm.workload.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import static org.springframework.data.mongodb.core.mapping.Field.Write.NON_NULL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {

    @Field(name = "month", write = NON_NULL)
    @NotNull(message = "Month is required")
    private Integer month;

    @Field(name = "trainingSummaryDuration", write = NON_NULL)
    @NotNull(message = "Training summary duration is required")
    @PositiveOrZero(message = "Training summary duration must be positive")
    private Integer trainingSummaryDuration;
}