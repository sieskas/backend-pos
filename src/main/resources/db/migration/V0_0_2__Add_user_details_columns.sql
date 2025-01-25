-- V0_0_2__Add_user_details_columns.sql
ALTER TABLE users
    ADD COLUMN account_non_expired BOOLEAN DEFAULT true,
    ADD COLUMN account_non_locked BOOLEAN DEFAULT true,
    ADD COLUMN credentials_non_expired BOOLEAN DEFAULT true,
    ADD COLUMN enabled BOOLEAN DEFAULT true;

-- Mettre à jour les utilisateurs existants
UPDATE users
SET account_non_expired = true,
    account_non_locked = true,
    credentials_non_expired = true,
    enabled = true;