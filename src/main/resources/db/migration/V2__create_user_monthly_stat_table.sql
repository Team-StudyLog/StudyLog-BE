CREATE TABLE user_monthly_stat (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    record_count INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_monthly_stat UNIQUE (user_id, year, month),
    CONSTRAINT fk_user_monthly_stat_user FOREIGN KEY (user_id)
        REFERENCES users(id)
);
