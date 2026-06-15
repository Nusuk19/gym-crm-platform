-- USERS
INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Mike', 'Tyson', 'Mike.Tyson', 'hashedPassword32', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Abdul', 'Hariton', 'Abdul.Hariton', 'hashedPassword33', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Adam', 'Future', 'Adam.Future', 'hashedPassword34', true);

-- TRAINEES
INSERT INTO trainees (user_id, address, date_of_birth)
VALUES (
           (SELECT id FROM users WHERE username = 'Abdul.Hariton'),
           '123 Wolfs St',
           '1994-05-06'
       );

-- TRAINERS
INSERT INTO trainers (user_id, specialization_id)
VALUES (
           (SELECT id FROM users WHERE username = 'Mike.Tyson'),
           (SELECT id FROM training_type WHERE training_type_name = 'Boxing')
       );

INSERT INTO trainers (user_id, specialization_id)
VALUES (
           (SELECT id FROM users WHERE username = 'Adam.Future'),
           (SELECT id FROM training_type WHERE training_type_name = 'Cardio')
       );

-- TRAINEE_TRAINER
INSERT INTO trainee_trainer (trainee_id, trainer_id)
VALUES (
           (SELECT id FROM trainees WHERE user_id =
                                          (SELECT id FROM users WHERE username = 'Abdul.Hariton')
           ),
           (SELECT id FROM trainers WHERE user_id =
                                          (SELECT id FROM users WHERE username = 'Mike.Tyson')
           )
       );