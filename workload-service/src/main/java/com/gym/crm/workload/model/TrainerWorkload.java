package com.gym.crm.workload.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import static org.springframework.data.mongodb.core.mapping.Field.Write.NON_NULL;

@Getter
@Setter
@Builder
@Document(collection = "trainer_workloads")
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(
        name = "trainer_name_idx",
        def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
public class TrainerWorkload {

    @Id
    @Field(name = "_id", write = NON_NULL)
    @NotBlank(message = "Trainer username is required")
    private String username;

    @Field(name = "trainerFirstName", write = NON_NULL)
    @NotBlank(message = "Trainer first name is required")
    private String trainerFirstName;

    @Field(name = "trainerLastName", write = NON_NULL)
    @NotBlank(message = "Trainer last name is required")
    private String trainerLastName;

    @Field(name = "isActive", write = NON_NULL)
    @NotNull(message = "Trainer status is required")
    private Boolean isActive;

    @Field(name = "years", write = NON_NULL)
    @NotNull(message = "Years list is required")
    @Valid
    private List<YearSummary> years;
}