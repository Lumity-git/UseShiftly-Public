CREATE TABLE employee_availability (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    day VARCHAR(16) NOT NULL,
    start_time VARCHAR(8) NOT NULL,
    end_time VARCHAR(8) NOT NULL
);
