CREATE TABLE preliminary (
    as_of_year INTEGER,
    respondent_id VARCHAR(50),
    agency_name VARCHAR(255),
    agency_abbr VARCHAR(10),
    agency_code INTEGER,
    loan_type_name VARCHAR(255),
    loan_type INTEGER,
    property_type_name VARCHAR(255),
    property_type INTEGER,
    loan_purpose_name VARCHAR(255),
    loan_purpose INTEGER,
    owner_occupancy_name VARCHAR(255),
    owner_occupancy INTEGER,
    loan_amount_000s INTEGER,
    preapproval_name VARCHAR(255),
    preapproval INTEGER,
    action_taken_name VARCHAR(255),
    action_taken INTEGER,
    msamd_name VARCHAR(255),
    msamd INTEGER,
    state_name VARCHAR(255),
    state_abbr VARCHAR(10),
    state_code INTEGER,
    county_name VARCHAR(255),
    county_code INTEGER,
    census_tract_number NUMERIC,
    applicant_ethnicity_name VARCHAR(255),
    Applicant_ethnicity INTEGER,
    co_applicant_ethnicity_name VARCHAR(255),
    co_applicant_ethnicity INTEGER,
    applicant_race_name_1 VARCHAR(255),
    applicant_race_1 INTEGER,
    applicant_race_name_2 VARCHAR(255),
    applicant_race_2 INTEGER,
    applicant_race_name_3 VARCHAR(255),
    applicant_race_3 INTEGER,
    applicant_race_name_4 VARCHAR(255),
    applicant_race_4 INTEGER,
    applicant_race_name_5 VARCHAR(255),
    applicant_race_5 INTEGER,
    co_applicant_race_name_1 VARCHAR(255),
    co_applicant_race_1 INTEGER,
    co_applicant_race_name_2 VARCHAR(255),
    co_applicant_race_2 INTEGER,
    co_applicant_race_name_3 VARCHAR(255),
    co_applicant_race_3 INTEGER,
    co_applicant_race_name_4 VARCHAR(255),
    co_applicant_race_4 INTEGER,
    co_applicant_race_name_5 VARCHAR(255),
    co_applicant_race_5 INTEGER,
    applicant_sex_name VARCHAR(255),
    applicant_sex INTEGER,
    co_applicant_sex_name VARCHAR(255),
    co_applicant_sex INTEGER,
    applicant_income_000s INTEGER,
    purchaser_type_name VARCHAR(255),
    purchaser_type INTEGER,
    denial_reason_name_1 VARCHAR(255),
    denial_reason_1 INTEGER,
    denial_reason_name_2 VARCHAR(255),
    denial_reason_2 INTEGER,
    denial_reason_name_3 VARCHAR(255),
    denial_reason_3 INTEGER,
    rate_spread NUMERIC,
    hoepa_status_name VARCHAR(255),
    hoepa_status INTEGER,
    lien_status_name VARCHAR(255),
    lien_status INTEGER,
    edit_status_name VARCHAR(255),
    edit_status INTEGER,
    sequence_number INTEGER,
    population INTEGER,
    minority_population INTEGER,
    hud_median_family_income NUMERIC,
    tract_to_msamd_income NUMERIC,
    number_of_owner_occupied_units INTEGER,
    number_of_1_to_4_family_units INTEGER,
    application_date_indicator INTEGER
);
ALTER TABLE preliminary
    ALTER COLUMN minority_population TYPE numeric USING minority_population::numeric;

--- Load data from CSV file into the table--
COPY preliminary
FROM '/Users/dayya/Downloads/hmda_2017_nj_all-records_labels.csv'
DELIMITER ','
CSV HEADER
QUOTE '"'
--forces null for empty fields--
FORCE NULL
    applicant_race_1, applicant_race_2, applicant_race_3, applicant_race_4, applicant_race_5,
    co_applicant_race_1, co_applicant_race_2, co_applicant_race_3, co_applicant_race_4, co_applicant_race_5,
    applicant_ethnicity, co_applicant_ethnicity, applicant_sex, co_applicant_sex, denial_reason_1, denial_reason_2, denial_reason_3,
    applicant_income_000s, rate_spread, edit_status, sequence_number, minority_population,
    application_date_indicator, msamd, county_code, census_tract_number, population, hud_median_family_income,
    tract_to_msamd_income, number_of_owner_occupied_units, number_of_1_to_4_family_units,
    loan_amount_000s;
--checking to see if loaded correctly --
SELECT COUNT(*) FROM preliminary;
SELECT COUNT(*) FROM preliminary WHERE loan_amount_000s IS NULL;
SELECT * FROM preliminary LIMIT 10;
--define smaller normalized tables--
CREATE TABLE locations (
    location_id SERIAL PRIMARY KEY,
    county_code INTEGER,
    msamd INTEGER,
    state_code INTEGER,
    census_tract_number NUMERIC,
    population INTEGER,
    minority_population NUMERIC,
    hud_median_family_income NUMERIC,
    tract_to_msamd_income NUMERIC,
    number_of_owner_occupied_units INTEGER,
    number_of_1_to_4_family_units INTEGER
);
CREATE TABLE applications (
    application_id SERIAL PRIMARY KEY,
    respondent_id VARCHAR(50),
    agency_code INTEGER,
    loan_type INTEGER,
    property_type INTEGER,
    loan_purpose INTEGER,
    owner_occupancy INTEGER,
    loan_amount_000s INTEGER,
    preapproval INTEGER,
    action_taken INTEGER,
    msamd INTEGER,
    state_code INTEGER,
    county_code INTEGER,
    census_tract_number NUMERIC,
    applicant_ethnicity INTEGER,
    co_applicant_ethnicity INTEGER,
    applicant_sex INTEGER,
    co_applicant_sex INTEGER,
    applicant_income_000s INTEGER,
    purchaser_type INTEGER,
    denial_reason_1 INTEGER,
    denial_reason_2 INTEGER,
    denial_reason_3 INTEGER,
    rate_spread NUMERIC,
    hoepa_status INTEGER,
    lien_status INTEGER,
    edit_status INTEGER,
    sequence_number INTEGER,
    location_id INTEGER,
    FOREIGN KEY (location_id) REFERENCES locations (location_id)
);
CREATE TABLE application_races (
    application_id INTEGER,
    race INTEGER,
    race_number INTEGER,
    FOREIGN KEY (application_id) REFERENCES applications (application_id)
);
--now we populate the normalized tables--
INSERT INTO locations (county_code, msamd, state_code, census_tract_number, population, minority_population, hud_median_family_income, tract_to_msamd_income, number_of_owner_occupied_units, number_of_1_to_4_family_units)
SELECT DISTINCT county_code, msamd, state_code, census_tract_number, population, minority_population, hud_median_family_income, tract_to_msamd_income, number_of_owner_occupied_units, number_of_1_to_4_family_units
FROM preliminary;


INSERT INTO applications (respondent_id, agency_code, loan_type, property_type, loan_purpose, owner_occupancy, loan_amount_000s, preapproval, action_taken, msamd, state_code, county_code, census_tract_number, applicant_ethnicity, co_applicant_ethnicity, applicant_sex, co_applicant_sex, applicant_income_000s, purchaser_type, denial_reason_1, denial_reason_2, denial_reason_3, rate_spread, hoepa_status, lien_status, edit_status, sequence_number, location_id)
SELECT p.respondent_id, p.agency_code, p.loan_type, p.property_type, p.loan_purpose, p.owner_occupancy, p.loan_amount_000s, p.preapproval, p.action_taken, p.msamd, p.state_code, p.county_code, p.census_tract_number, p.applicant_ethnicity, p.co_applicant_ethnicity, p.applicant_sex, p.co_applicant_sex, p.applicant_income_000s, p.purchaser_type, p.denial_reason_1, p.denial_reason_2, p.denial_reason_3, p.rate_spread, p.hoepa_status, p.lien_status, p.edit_status, p.sequence_number, l.location_id
FROM preliminary p
JOIN locations l ON p.county_code = l.county_code AND p.msamd = l.msamd AND p.state_code = l.state_code AND p.census_tract_number = l.census_tract_number AND p.population = l.population AND p.minority_population = l.minority_population AND p.hud_median_family_income = l.hud_median_family_income AND p.tract_to_msamd_income = l.tract_to_msamd_income AND p.number_of_owner_occupied_units = l.number_of_owner_occupied_units AND p.number_of_1_to_4_family_units = l.number_of_1_to_4_family_units;

--for applicant races we need to do a union for each race--
INSERT INTO application_races (application_id, race, race_number)
SELECT a.application_id, p.applicant_race_1, 1
FROM preliminary p
JOIN applications a ON p.sequence_number = a.sequence_number
WHERE p.applicant_race_1 IS NOT NULL

UNION

SELECT a.application_id, p.applicant_race_2, 2
FROM preliminary p
JOIN applications a ON p.sequence_number = a.sequence_number
WHERE p.applicant_race_2 IS NOT NULL

UNION

SELECT a.application_id, p.applicant_race_3, 3
FROM preliminary p
JOIN applications a ON p.sequence_number = a.sequence_number
WHERE p.applicant_race_3 IS NOT NULL

UNION

SELECT a.application_id, p.applicant_race_4, 4
FROM preliminary p
JOIN applications a ON p.sequence_number = a.sequence_number
WHERE p.applicant_race_4 IS NOT NULL

UNION

SELECT a.application_id, p.applicant_race_5, 5
FROM preliminary p
JOIN applications a ON p.sequence_number = a.sequence_number
WHERE p.applicant_race_5 IS NOT NULL;


--check for errors--
ALTER TABLE applications ADD CONSTRAINT valid_property_type CHECK (property_type IN (1, 2, 3, 4));

--rejoin normailzed tables and export to csv--
COPY (
    SELECT * FROM (
        SELECT a.*, l.*, ar.*
        FROM applications a
        JOIN locations l ON a.location_id = l.location_id
        LEFT JOIN application_races ar ON a.application_id = ar.application_id
    ) AS combined
) TO 'C:\Program Files\PostgreSQL\17\data\output.csv' WITH CSV HEADER;


ALTER TABLE applications DROP COLUMN IF EXISTS edit_status; 


ALTER TABLE applications DROP COLUMN IF EXISTS sequence_number; 


ALTER TABLE locations ADD COLUMN location_key VARCHAR(255) UNIQUE; 


UPDATE locations SET location_key=CONCAT(county_code, '_', msamd, '_', state_code, '_', census_tract_number, '_', population, '_', minority_population, '_', hud_median_family_income, '_', tract_to_msamd_income, '_', number_of_owner_occupied_units, '_', number_of_1_to_4_family_units); 


ALTER TABLE applications ADD COLUMN location_conCAt VARCHAR(255); 
UPDATE applications a SET location_conCAt = l.location_key FROM locations l WHERE a.location_id = l.location_id; 


copy ( SELECT * FROM ( SELECT a.*, l.*, ar.* FROM applications a JOIN locations l ON a.location_id = l.location_id LEFT JOIN application_races ar ON a.application_id = ar.application_id ) AS combined ) TO '/Users/theguy/Downloads/pdm/project1/hmda_2017_nj_all-records_labels.csv' WITH CSV HEADER; 


ALTER TABLE locations DROP CONSTRAINT locations_location_key_key; 


copy ( SELECT * FROM ( SELECT a.*, ar.* FROM applications a JOIN locations l ON a.location_id = l.location_id LEFT JOIN application_races ar ON a.application_id = ar.application_id ) AS combined ) TO '/Users/theguy/Downloads/pdm/project1/hmda_2017_nj_all-records_labels.csv' WITH CSV HEADER;

-- our applications table is empty becuase we did not include the sequence number in the select statement when we created the table--