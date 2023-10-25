CREATE TABLE IF NOT EXISTS musics
(
    name            text            NOT NULL PRIMARY KEY,
    singer_name     text            NOT NULL,
    released_at     TIMESTAMP       NOT NULL
);
