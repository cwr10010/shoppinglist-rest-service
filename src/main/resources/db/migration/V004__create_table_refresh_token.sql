CREATE TABLE refresh_token (
 id VARCHAR(200) PRIMARY KEY,
 valid CHAR(1) NOT NULL,
 user_id VARCHAR(200) NOT NULL,
 expires DATETIME NOT NULL,
 created DATETIME NOT NULL,
 modified DATETIME,
 FOREIGN KEY (user_id)
        REFERENCES user(id)
)
