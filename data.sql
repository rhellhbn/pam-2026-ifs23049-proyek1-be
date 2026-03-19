-- ─── Users ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
                                     id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    photo      VARCHAR(255) NULL,
    bio        TEXT         NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
    );

-- ─── Refresh Tokens ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL,
    refresh_token TEXT NOT NULL,
    auth_token    TEXT NOT NULL,
    created_at    TIMESTAMP NOT NULL
    );

-- ─── Books ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS books (
                                     id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    title       VARCHAR(200) NOT NULL,
    author      VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    genre       VARCHAR(50)  NOT NULL DEFAULT 'Umum',
    isbn        VARCHAR(20)  NULL,
    publisher   VARCHAR(150) NULL,
    year        INTEGER      NULL,
    cover       TEXT         NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
    );