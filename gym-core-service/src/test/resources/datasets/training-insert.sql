DELETE FROM trainee_trainer;
DELETE FROM trainings;
DELETE FROM trainees;
DELETE FROM trainers;
DELETE FROM users;

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Abdul', 'Hariton', 'Abdul.Hariton', 'hashedPassword', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Mike', 'Tyson', 'Mike.Tyson', 'hashedPassword2', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('John', 'Smith', 'John.Smith', 'hashedPassword3', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Anna', 'Jones', 'Anna.Jones', 'hashedPassword4', true);

INSERT INTO trainees (user_id, address, date_of_birth)
VALUES ((SELECT id FROM users WHERE username = 'Abdul.Hariton'), '123 Wolfs St', '1994-05-06');

INSERT INTO trainees (user_id, address, date_of_birth)
VALUES ((SELECT id FROM users WHERE username = 'John.Smith'), '456 Oak Ave', '1990-03-15');

INSERT INTO trainers (user_id, specialization_id)
VALUES ((SELECT id FROM users WHERE username = 'Mike.Tyson'),
        (SELECT id FROM training_type WHERE training_type_name = 'Boxing'));

INSERT INTO trainers (user_id, specialization_id)
VALUES ((SELECT id FROM users WHERE username = 'Anna.Jones'),
        (SELECT id FROM training_type WHERE training_type_name = 'Yoga'));

INSERT INTO trainings (training_name, training_date, training_duration, trainee_id, trainer_id, specialization_id)
VALUES ('Boxing Basics', '2024-06-01', 60,
        (SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'Abdul.Hariton')),
        (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'Mike.Tyson')),
        (SELECT id FROM training_type WHERE training_type_name = 'Boxing'));

INSERT INTO trainings (training_name, training_date, training_duration, trainee_id, trainer_id, specialization_id)
VALUES ('Advanced Boxing', '2024-08-15', 90,
        (SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'Abdul.Hariton')),
        (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'Mike.Tyson')),
        (SELECT id FROM training_type WHERE training_type_name = 'Boxing'));

INSERT INTO trainings (training_name, training_date, training_duration, trainee_id, trainer_id, specialization_id)
VALUES ('Morning Yoga', '2024-07-10', 45,
        (SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'John.Smith')),
        (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'Anna.Jones')),
        (SELECT id FROM training_type WHERE training_type_name = 'Yoga'));