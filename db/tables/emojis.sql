CREATE TABLE IF NOT EXISTS emojis (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    frequency INTEGER DEFAULT 0,
    emoji TEXT NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
