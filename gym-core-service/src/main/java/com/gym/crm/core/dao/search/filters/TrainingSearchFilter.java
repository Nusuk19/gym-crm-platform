package com.gym.crm.core.dao.search.filters;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
public class TrainingSearchFilter {

    protected String username;

    protected LocalDate fromDate;

    protected LocalDate toDate;
}