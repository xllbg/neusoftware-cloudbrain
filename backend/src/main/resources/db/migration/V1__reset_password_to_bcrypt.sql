-- Reset all passwords to BCrypt hash of "123456"
-- Run this script against your cloudbrain database

UPDATE patient SET password = '$2a$10$SFAmZmEAhyMrlgdfyeC7uOs7URQkjThQJzEj6nsXgFkrjvJeNpTXK';
UPDATE doctor SET password = '$2a$10$SFAmZmEAhyMrlgdfyeC7uOs7URQkjThQJzEj6nsXgFkrjvJeNpTXK';
