CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY,
    telegram_id INTEGER,
    user_id INTEGER,
    text TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
