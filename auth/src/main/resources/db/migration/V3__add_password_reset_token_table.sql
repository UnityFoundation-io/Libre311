CREATE TABLE password_reset_token
(
    id      bigint AUTO_INCREMENT PRIMARY KEY,
    token   varchar(255) NOT NULL,
    user_id bigint       NOT NULL,
    expiry  timestamp    NOT NULL,
    UNIQUE (token),
    CONSTRAINT password_reset_token_user_FK FOREIGN KEY (user_id) REFERENCES user (id)
);
