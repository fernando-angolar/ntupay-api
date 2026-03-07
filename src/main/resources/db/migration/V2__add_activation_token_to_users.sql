ALTER TABLE users
    ADD COLUMN IF NOT EXISTS activation_token VARCHAR(120);

UPDATE users
SET activation_token = 'legacy_' || md5(random()::text || clock_timestamp()::text)
WHERE activation_token IS NULL;

ALTER TABLE users
    ALTER COLUMN activation_token SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_activation_token ON users(activation_token);