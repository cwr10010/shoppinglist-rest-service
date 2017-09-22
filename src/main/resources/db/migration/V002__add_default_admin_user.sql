INSERT INTO authority
(
    id,
    name,
    created
) values (
    '624f7f7a-f0a9-4262-8fa8-550357d5790a',
    'ROLE_ADMIN',
    CURRENT_TIMESTAMP
),
(
    '7c057475-82ca-4584-935a-c241ebbb1009',
    'ROLE_USER',
    CURRENT_TIMESTAMP
);

INSERT INTO user
(
    id,
    username,
    password,
    email_address,
    active,
    created
) values (
    'e3a7dc43-9ed3-4f9c-b5e4-8cb6493885a8',
    'admin',
    '$2a$10$xqXWmTFmKpOLJByx5qB3semrkBu/KrVrzM5/uYVC1vuYWdOlDSpBq',
    'test@example.com',
    true,
    CURRENT_TIMESTAMP
);

INSERT INTO user_authority
(
    user_id,
    authority_id
) values (
    'e3a7dc43-9ed3-4f9c-b5e4-8cb6493885a8',
    '624f7f7a-f0a9-4262-8fa8-550357d5790a'
);
