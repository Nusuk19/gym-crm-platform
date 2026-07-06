package com.gym.crm.workload.model;

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
    private String trainerUsername;

    @Field(name = "trainerFirstName", write = NON_NULL)
    private String trainerFirstName;

    @Field(name = "trainerLastName", write = NON_NULL)
    private String trainerLastName;

    @Field(name = "isActive", write = NON_NULL)
    private Boolean isActive;

    @Field(name = "years", write = NON_NULL)
    private List<YearSummary> years;
}