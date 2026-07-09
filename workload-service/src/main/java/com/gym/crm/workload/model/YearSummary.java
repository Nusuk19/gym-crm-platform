package com.gym.crm.workload.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import static org.springframework.data.mongodb.core.mapping.Field.Write.NON_NULL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    @Field(name = "year", write = NON_NULL)
    @NotNull(message = "Year is required")
    private Integer year;

    @Field(name = "months", write = NON_NULL)
    @NotNull(message = "Months list is required")
    @Valid
    private List<MonthSummary> months;
}