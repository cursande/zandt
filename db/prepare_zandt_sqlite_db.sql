CREATE TABLE IF NOT EXISTS zandt.users (
    id INTEGER PRIMARY KEY,
    telegram_id INTEGER NOT NULL UNIQUE,
    first_name TEXT,
    last_name TEXT
);

CREATE TABLE IF NOT EXISTS zandt.messages (
    id INTEGER PRIMARY KEY,
    telegram_id INTEGER,
    user_id INTEGER,
    message TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS zandt.words (
    id INTEGER PRIMARY KEY,
    message_id INTEGER,
    user_id INTEGER,
    frequency INTEGER,
    word TEXT NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS zandt.emojis (
    id INTEGER PRIMARY KEY,
    message_id INTEGER,
    user_id INTEGER,
    emoji TEXT NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
