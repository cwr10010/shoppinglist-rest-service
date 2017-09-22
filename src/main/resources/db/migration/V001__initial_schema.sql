
CREATE TABLE
    user (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the user',
        username VARCHAR(255) NOT NULL COMMENT 'The login name of the user',
        password VARCHAR(255) NOT NULL COMMENT 'Hashed password of the user',
        email_address VARCHAR(255) NOT NULL COMMENT 'Email address via which the user wants to be contacted',
        active CHAR(1) NOT NULL NOT NULL COMMENT 'Flag if user is active',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

CREATE TABLE
    authority (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the authority',
        name VARCHAR(50) NOT NULL COMMENT 'Identified name of the authority',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

CREATE TABLE
    user_authority (
        user_id VARCHAR(255) NOT NULL COMMENT 'Reference to the users Id',
        authority_id VARCHAR(255) NOT NULL COMMENT 'Reference to one of the users authorities')
ENGINE=InnoDB;

CREATE TABLE
    refresh_token (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the refresh jwt',
        expires DATETIME NOT NULL COMMENT 'Expiry date of the refresh jwt',
        valid CHAR(1) NOT NULL COMMENT 'Switch to revoke this token',
        user_id VARCHAR(255) NOT NULL COMMENT 'Reference to the user of this jwt',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

CREATE TABLE
    registration_data (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the registration data of a future user',
        username VARCHAR(255) NOT NULL COMMENT 'Login name',
        password VARCHAR(255) NOT NULL COMMENT 'Hashed password',
        email_address VARCHAR(255) NOT NULL COMMENT 'Email address that was used to send the registration mail',
        token VARCHAR(255) NOT NULL COMMENT 'Token in the registration link',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

CREATE TABLE
    shopping_list (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the users shopping list',
        name VARCHAR(1000) NOT NULL COMMENT 'Name of this list',
        owners_user_id VARCHAR(255) NOT NULL COMMENT 'User id of the owner of this list',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

CREATE TABLE
    shopping_list_item (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the item of a shopping list',
        name VARCHAR(1000) NOT NULL COMMENT 'Name of the shopping list item',
        description VARCHAR(4000) NOT NULL COMMENT 'A descriptive text for the shopping list item',
        item_order INT NOT NULL COMMENT 'Position of the item in the shopping list',
        item_checked CHAR(1) NOT NULL COMMENT 'Boolean if the item is checked',
        user_id VARCHAR(255) COMMENT 'Id of the user that created this item in the shopping list',
        shopping_list_id VARCHAR(255) NOT NULL COMMENT 'Reference to the shopping list this item belongs to',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

CREATE TABLE
    accessable_for_user_ids (
        user_id VARCHAR(255) NOT NULL COMMENT 'User id that the referenced shopping list is allowed to access',
        shoppinglist_id VARCHAR(255) NOT NULL COMMENT 'Id of Shopping list that the referenced user is allowed to access',
        PRIMARY KEY (user_id, shoppinglist_id))
ENGINE=InnoDB;

ALTER TABLE user ADD CONSTRAINT UK_user_username UNIQUE (username);

ALTER TABLE user_authority ADD CONSTRAINT FK_authority_user_id FOREIGN KEY (user_id) REFERENCES user (id);
ALTER TABLE user_authority ADD CONSTRAINT FK_user_authority_id FOREIGN KEY (authority_id) REFERENCES authority (id);

ALTER TABLE registration_data ADD CONSTRAINT UK_registration_data_username UNIQUE (username);

ALTER TABLE refresh_token ADD CONSTRAINT FK_refresh_token_user_id FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE shopping_list ADD CONSTRAINT FK_shopping_list_owners_user_id FOREIGN KEY (owners_user_id) REFERENCES user (id);

ALTER TABLE shopping_list_item ADD CONSTRAINT FK_shopping_list_id FOREIGN KEY (shopping_list_id) REFERENCES shopping_list (id);

ALTER TABLE accessable_for_user_ids ADD CONSTRAINT UK_shoppinglist_id UNIQUE (shoppinglist_id);
ALTER TABLE accessable_for_user_ids ADD CONSTRAINT FK_shoppinglist_user_id FOREIGN KEY (user_id) REFERENCES user (id);
ALTER TABLE accessable_for_user_ids ADD CONSTRAINT FK_user_shoppinglist_id FOREIGN KEY (shoppinglist_id) REFERENCES shopping_list (id);
