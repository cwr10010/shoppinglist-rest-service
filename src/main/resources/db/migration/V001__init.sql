CREATE TABLE user (
 id VARCHAR(200) PRIMARY KEY,
 username VARCHAR(255) NOT NULL,
 password VARCHAR(255) NOT NULL,
 created DATE NOT NULL,
 modified DATE NOT NULL
)
ENGINE=InnoDB;
--
CREATE TABLE shopping_list (
 id VARCHAR(200) PRIMARY KEY,
 name VARCHAR(1000) NOT NULL,
 description VARCHAR(4000) NOT NULL,
 item_order INT NOT NULL,
 item_read VARCHAR(20) NOT NULL,
 user_id VARCHAR(200) NOT NULL,
 created DATE NOT NULL,
 modified DATE NOT NULL,
 FOREIGN KEY (user_id)
        REFERENCES user(id)
)
ENGINE=InnoDB;
--