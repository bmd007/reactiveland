CREATE TABLE IF NOT EXISTS authentication_challenge
(
    nonce           text      NOT NULL PRIMARY KEY,
    state           text      NOT NULL,
    expires_at      TIMESTAMP NOT NULL
);
