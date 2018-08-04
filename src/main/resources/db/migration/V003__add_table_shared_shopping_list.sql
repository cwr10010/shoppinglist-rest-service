CREATE TABLE
    shared_shopping_list (
        id VARCHAR(255) NOT NULL COMMENT 'Id of the share request of a shopping list',
        from_user_id VARCHAR(255) NOT NULL COMMENT 'The shoppinglist is owned by this user',
        for_user_id VARCHAR(255) NOT NULL COMMENT 'The shoppinglist is shared for this user',
        shared_list_id VARCHAR(255) NOT NULL COMMENT 'Reference to the shopping list that is shared',
        connected CHAR(1) NOT NULL COMMENT 'Boolean if the item is checked',
        created DATETIME NOT NULL COMMENT 'Date and time at which the entry was created, will never change',
        modified DATETIME COMMENT 'Date and time at which the entry was modified last, is null initially',
        PRIMARY KEY (id))
ENGINE=InnoDB;

ALTER TABLE shared_shopping_list ADD CONSTRAINT FK_from_user_id FOREIGN KEY (from_user_id) REFERENCES user (id);
ALTER TABLE shared_shopping_list ADD CONSTRAINT FK_for_user_id FOREIGN KEY (for_user_id) REFERENCES user (id);
ALTER TABLE shared_shopping_list ADD CONSTRAINT FK_shared_list_id FOREIGN KEY (shared_list_id) REFERENCES shopping_list (id);

ALTER TABLE accessable_for_user_ids DROP CONSTRAINT IF EXISTS UK_shoppinglist_id;
ALTER TABLE accessable_for_user_ids ADD CONSTRAINT UK_shoppinglist_user_id UNIQUE (shoppinglist_id, user_id);