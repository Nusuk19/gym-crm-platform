INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Abdul', 'Hariton', 'Abdul.Hariton', 'hashedPassword', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Adam', 'Future', 'Adam.Future', 'hashedPassword31', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Mike', 'Tyson', 'Mike.Tyson', 'hashedPassword32', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Anna', 'Hural', 'Anna.Hural', 'hashedPassword33', true);

INSERT INTO trainees (user_id, address, date_of_birth)
VALUES ((SELECT id FROM users WHERE username = 'Abdul.Hariton'), '123 Wolfs St', '1994-05-06');

INSERT INTO trainers (user_id, specialization_id)
VALUES ((SELECT id FROM users WHERE username = 'Mike.Tyson'),
        (SELECT id FROM training_type WHERE training_type_name = 'Boxing'));

INSERT INTO trainers (user_id, specialization_id)
VALUES ((SELECT id FROM users WHERE username = 'Anna.Hural'),
        (SELECT id FROM training_type WHERE training_type_name = 'Yoga'));


INSERT INTO trainings (training_name, training_date, training_duration, trainee_id, trainer_id, specialization_id)
VALUES ('Boxing', '2025-06-06', 45,
        (SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'Abdul.Hariton')),
        (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'Mike.Tyson')),
        (SELECT id FROM training_type WHERE training_type_name = 'Boxing'));

INSERT INTO trainee_trainer (trainee_id, trainer_id)
VALUES ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'Abdul.Hariton')),
        (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'Mike.Tyson')));