CREATE TABLE IF NOT EXISTS zandt.users (
    id INTEGER PRIMARY KEY,
    telegram_id INTEGER NOT NULL UNIQUE,
    username TEXT
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
    user_id INTEGER,
    frequency INTEGER,
    word TEXT NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS zandt.emojis (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    frequency INTEGER,
    emoji TEXT NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
