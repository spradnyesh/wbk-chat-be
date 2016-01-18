-- name: create-user!
-- creates a new user record
INSERT INTO users
(first_name, last_name, email, passwd)
VALUES (:firstname, :lastname, :email, :passwd)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :firstname, last_name = :lastname, email = :email
WHERE id = :id

-- name: get-all-users
-- retrieve all users
SELECT * FROM users

-- name: get-user
-- retrieve a user given the id
SELECT * FROM users
WHERE id = :id

-- name: find-user
-- retrieve a user given the email
SELECT * FROM users
WHERE email = :email

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id

-- name: create-message!
-- creates a new message record
INSERT INTO messages
(from_user_id, to_user_id, message)
VALUES (:from, :to, :message)

-- name: read-messages
-- retrieve messages for a user
SELECT * FROM messages
WHERE from_user_id = :id

-- name: read-user-messages
-- retrieve messages for a user >= msg_id
SELECT * FROM messages
WHERE id > :msgid
AND (to_user_id = :id
     OR from_user_id = :id)
