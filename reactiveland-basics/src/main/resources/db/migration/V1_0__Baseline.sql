CREATE TABLE IF NOT EXISTS nonce
(
    value           INTEGER PRIMARY KEY,
    user_identifier text      NOT NULL,
    created_at      TIMESTAMP NOT NULL
);
