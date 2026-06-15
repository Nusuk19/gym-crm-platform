DELETE FROM trainee_trainer;
DELETE FROM trainings;
DELETE FROM trainees;
DELETE FROM trainers;
DELETE FROM users;

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Abdul', 'Hariton', 'Abdul.Hariton', 'hashedPassword', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Mike', 'Tyson', 'Mike.Tyson', 'hashedPassword2', false);