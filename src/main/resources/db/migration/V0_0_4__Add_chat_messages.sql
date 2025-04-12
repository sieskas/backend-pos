CREATE TABLE chat_sessions (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT,
                               started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE chat_messages (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               session_id BIGINT NOT NULL,
                               sender_type VARCHAR(10) NOT NULL, -- 'USER' ou 'BOT'
                               sender_id BIGINT,                 -- NULL si BOT, sinon FK vers users
                               content TEXT NOT NULL,
                               timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (session_id) REFERENCES chat_sessions(id),
                               FOREIGN KEY (sender_id) REFERENCES users(id)
);
