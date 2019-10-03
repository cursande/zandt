CREATE TABLE IF NOT EXISTS words (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    frequency INTEGER DEFAULT 0,
    word TEXT NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
