-- V2__seed_system_account.sql

INSERT INTO accounts (id, owner_ref, currency, type, created_at)
VALUES (
    '99999999-9999-9999-9999-999999999999',
    'EXTERNAL_FUNDING',
    'INR',
    'SYSTEM',
    now()
);