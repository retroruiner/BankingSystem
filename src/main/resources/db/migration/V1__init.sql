CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name TEXT NOT NULL,
                       email TEXT NOT NULL UNIQUE,
                       registered_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          number VARCHAR(34) NOT NULL UNIQUE,
                          balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          version BIGINT NOT NULL DEFAULT 0,
                          CONSTRAINT balance_non_negative CHECK (balance >= 0)
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
