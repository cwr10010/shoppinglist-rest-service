-- User Table
CREATE TABLE user (
 id VARCHAR(200) PRIMARY KEY,
 username VARCHAR(255) NOT NULL,
 password VARCHAR(255) NOT NULL,
 created DATETIME NOT NULL,
 modified DATETIME NOT NULL,
 CONSTRAINT idx_unique_username UNIQUE (username)
)
ENGINE=InnoDB;
-- ShoppingListItem Table
CREATE TABLE shopping_list (
 id VARCHAR(200) PRIMARY KEY,
 name VARCHAR(1000) NOT NULL,
 description VARCHAR(4000) NOT NULL,
 item_order INT NOT NULL,
 item_read CHAR(1) NOT NULL,
 user_id VARCHAR(200) NOT NULL,
 created DATETIME NOT NULL,
 modified DATETIME,
 FOREIGN KEY (user_id)
        REFERENCES user(id)
)
ENGINE=InnoDB;