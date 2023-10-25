CREATE TABLE IF NOT EXISTS authentication_challenge
(
    id              text      NOT NULL PRIMARY KEY,
    nonce           text      NOT NULL unique,
    state           text      NOT NULL,
    customer_id      text      ,
    expires_at      TIMESTAMP NOT NULL
);
