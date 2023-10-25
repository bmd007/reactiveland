CREATE TABLE IF NOT EXISTS dancers
(
    id                  text        NOT NULL PRIMARY KEY,
    dance_type_competency text        NOT NULL,
    last_danced_at      TIMESTAMP     NOT NULL
);
